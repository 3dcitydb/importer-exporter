package de.tub.citydb.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.CityGMLModule;
import org.citygml4j.model.citygml.CityGMLModuleType;
import org.citygml4j.model.citygml.appearance.AppearanceModule;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.citygml.core.CoreModule;
import org.citygml4j.model.citygml.generics.GenericsModule;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.util.CityGMLModules;
import org.xml.sax.SAXException;

import de.tub.citydb.concurrent.DBExportWorkerFactory;
import de.tub.citydb.concurrent.DBExportXlinkWorkerFactory;
import de.tub.citydb.concurrent.IOWriterWorkerFactory;
import de.tub.citydb.concurrent.SingleWorkerPool;
import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.exporter.ExpAppearance;
import de.tub.citydb.config.project.exporter.ExpModuleVersion;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.cache.CacheManager;
import de.tub.citydb.db.cache.model.CacheTableModelEnum;
import de.tub.citydb.db.exporter.DBSplitter;
import de.tub.citydb.db.exporter.DBSplittingResult;
import de.tub.citydb.db.gmlId.DBExportCache;
import de.tub.citydb.db.gmlId.DBGmlIdLookupServerEnum;
import de.tub.citydb.db.gmlId.DBGmlIdLookupServerManager;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.event.statistic.FeatureCounterEvent;
import de.tub.citydb.event.statistic.GeometryCounterEvent;
import de.tub.citydb.event.statistic.StatusDialogMessage;
import de.tub.citydb.event.statistic.StatusDialogTitle;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.sax.SAXWriter;
import de.tub.citydb.sax.XMLHeaderWriter;
import de.tub.citydb.sax.events.SAXEvent;
import de.tub.citydb.util.DBUtil;
import de.tub.citydb.util.Util;

public class Exporter implements EventListener {
	private final Logger LOG = Logger.getInstance();

	private final JAXBContext jaxbContext;
	private final DBConnectionPool dbPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private DBSplitter dbSplitter;
	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);

	private WorkerPool<DBSplittingResult> dbWorkerPool;
	private SingleWorkerPool<Vector<SAXEvent>> ioWriterPool;
	private WorkerPool<DBXlink> xlinkExporterPool;
	private CacheManager cacheManager;
	private DBGmlIdLookupServerManager lookupServerManager;
	private ExportFilter exportFilter;
	private CityGMLFactory cityGMLFactory;

	private HashMap<CityGMLClass, Long> featureCounterMap;
	private HashMap<GMLClass, Long> geometryCounterMap;

	public Exporter(JAXBContext jaxbContext, DBConnectionPool dbPool, Config config, EventDispatcher eventDispatcher) {
		this.jaxbContext = jaxbContext;
		this.dbPool = dbPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		featureCounterMap = new HashMap<CityGMLClass, Long>();
		geometryCounterMap = new HashMap<GMLClass, Long>();
	}

	public boolean doProcess() {
		// get config shortcuts
		de.tub.citydb.config.project.system.System system = config.getProject().getExporter().getSystem();
		Database database = config.getProject().getDatabase();
		
		// worker pool settings
		int minThreads = system.getThreadPool().getDefaultPool().getMinThreads();
		int maxThreads = system.getThreadPool().getDefaultPool().getMaxThreads();

		// calc queueSize
		// how to properly calculate?
		int dbQueueSize = maxThreads * 20;
		int writerQueueSize = maxThreads * 100;
		
		// gml:id lookup cache update
		int lookupCacheBatchSize = database.getUpdateBatching().getGmlIdLookupServerBatchValue();		

		// checking workspace... this should be improved in future...
		String workspace = database.getWorkspace().getExportWorkspace();
		String date = database.getWorkspace().getExportDate();

		if (workspace != null) {
			workspace = workspace.trim();

			if (!workspace.toUpperCase().equals("LIVE")) {
				boolean workspaceExists = dbPool.checkWorkspace(workspace);

				if (!workspaceExists) {
					LOG.error("Database workspace '" + workspace + "' is not available.");
					return false;
				} else {
					String message = "Switching to database workspace '" + workspace + "'";

					if (date != null && date.trim().length() != 0) {
						date = date.trim();
						message += " at timestamp " + date;
					}

					LOG.info(message);
				}
			}
		}

		// getting export filter
		exportFilter = new ExportFilter(config, DBUtil.getInstance(dbPool));

		// CityGML object factory
		cityGMLFactory = new CityGMLFactory();

		try {
			// checking file
			Internal intConfig = config.getInternal();
			String fileName = intConfig.getExportFileName();
			File file = new File(fileName);
			File path = new File(file.getAbsolutePath());
			intConfig.setExportPath(path.getParent());

			eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("export.dialog.cityObj.msg")));
			eventDispatcher.triggerEvent(new StatusDialogTitle(file.getName()));

			// open file for writing
			OutputStreamWriter fileWriter = null;
			try {
				Charset charset = Charset.forName("UTF-8");
				fileWriter = new OutputStreamWriter(new FileOutputStream(file), charset);
			} catch (IOException ioE) {
				LOG.error("Failed to open file '" + fileName + "' for writing: " + ioE.getMessage());
				return false;
			}

			// checking export path for texture images
			ExpAppearance appearances = config.getProject().getExporter().getAppearances();
			if (appearances.isSetExportAppearance()) {
				// read user input
				String textureExportPath = null;
				boolean isRelative = appearances.isTexturePathRealtive();

				if (isRelative)
					textureExportPath = appearances.getRelativeTexturePath();
				else
					textureExportPath = appearances.getAbsoluteTexturePath();

				if (textureExportPath != null && textureExportPath.length() > 0) {
					// convert into system readable path name
					File tmp = new File(textureExportPath);
					textureExportPath = tmp.getPath();

					if (isRelative) {
						File exportPath = new File(path.getParent() + File.separator + textureExportPath);

						if (exportPath.isFile() || (exportPath.isDirectory() && !exportPath.canWrite())) {
							LOG.error("Failed to open texture files subfolder '" + exportPath.toString() + "' for writing.");
							return false;
						} else if (!exportPath.isDirectory()) {
							boolean success = exportPath.mkdirs();

							if (!success) {
								LOG.error("Failed to create texture files subfolder '" + exportPath.toString() + "'.");
								return false;
							} else
								LOG.info("Created texture files subfolder '" + textureExportPath + "'.");
						}

						intConfig.setExportTextureFilePath(textureExportPath);
					} else {
						File exportPath = new File(tmp.getAbsolutePath());

						if (!exportPath.exists() || !exportPath.isDirectory() || !exportPath.canWrite()) {
							LOG.error("Failed to open texture files folder '" + exportPath.toString() + "' for writing.");
							return false;
						}

						intConfig.setExportTextureFilePath(exportPath.toString());
					}
				}
			}

			// adding listeners
			eventDispatcher.addListener(EventType.FeatureCounter, this);
			eventDispatcher.addListener(EventType.GeometryCounter, this);
			eventDispatcher.addListener(EventType.Interrupt, this);

			// create instance of temp table manager
			cacheManager = new CacheManager(dbPool, maxThreads);

			// create instance of gml:id lookup server manager...
			lookupServerManager = new DBGmlIdLookupServerManager();

			// ...and start servers
			try {		
				lookupServerManager.initServer(
						DBGmlIdLookupServerEnum.GEOMETRY,
						new DBExportCache(cacheManager, 
								CacheTableModelEnum.GMLID_GEOMETRY, 
								system.getGmlIdLookupServer().getGeometry().getPartitions(),
								lookupCacheBatchSize),
						system.getGmlIdLookupServer().getGeometry().getCacheSize(),
						system.getGmlIdLookupServer().getGeometry().getPageFactor(),
						maxThreads);

				lookupServerManager.initServer(
						DBGmlIdLookupServerEnum.FEATURE,
						new DBExportCache(cacheManager, 
								CacheTableModelEnum.GMLID_FEATURE, 
								system.getGmlIdLookupServer().getFeature().getPartitions(), 
								lookupCacheBatchSize),
						system.getGmlIdLookupServer().getFeature().getCacheSize(),
						system.getGmlIdLookupServer().getFeature().getPageFactor(),
						maxThreads);
			} catch (SQLException sqlEx) {
				LOG.error("SQL error while initializing database export: " + sqlEx.getMessage());
				return false;
			}

			// create a saxWriter instance. define indent for xml output
			// and namespace mappings
			SAXWriter saxWriter = new SAXWriter(fileWriter);
			saxWriter.setIndentString("  ");
			saxWriter.forceNSDecl("http://www.opengis.net/gml", "gml");
			saxWriter.forceNSDecl("http://www.w3.org/1999/xlink", "xlink");
			saxWriter.forceNSDecl("http://www.w3.org/2001/SMIL20/", "smil20");
			saxWriter.forceNSDecl("http://www.w3.org/2001/SMIL20/Language", "smil20lang");
			saxWriter.forceNSDecl("urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", "xAL");
			saxWriter.forceNSDecl("http://www.w3.org/2001/XMLSchema-instance", "xsi");

			for (CityGMLModule module : CityGMLModules.getModules())
				saxWriter.suppressNSDecl(module.getNamespaceUri());

			// prepare namespace prefix and schemalocation statements...
			ExpModuleVersion moduleVersion = config.getProject().getExporter().getModuleVersion();
			List<CityGMLModule> moduleList = moduleVersion.getModules();
			HashMap<String, String> schemaLocationMap = new HashMap<String, String>();

			for (CityGMLModule module : moduleList) {
				String namespaceUri = module.getNamespaceUri();
				String schemaLocation = module.getSchemaLocation();
				
				saxWriter.forceNSDecl(namespaceUri, module.getNamespacePrefix());
				switch (module.getModuleType()) {
				case BUILDING:
				case CITYFURNITURE:
				case CITYOBJECTGROUP:
				case GENERICS:
				case LANDUSE:
				case RELIEF:
				case TRANSPORTATION:
				case VEGETATION:
				case WATERBODY:
					CoreModule core = (CoreModule)module.getModuleDependencies().getModule(CityGMLModuleType.CORE);
					GenericsModule gen = (GenericsModule)CityGMLModules.getModuleByTypeAndVersion(CityGMLModuleType.GENERICS, core.getModuleVersion());
					
					saxWriter.forceNSDecl(core.getNamespaceUri(), core.getNamespacePrefix());
					saxWriter.forceNSDecl(gen.getNamespaceUri(), gen.getNamespacePrefix());

					if (core.getSchemaLocation() != null)
						schemaLocationMap.put(core.getNamespaceUri(), core.getNamespaceUri() + " " + core.getSchemaLocation());
					if (gen.getSchemaLocation() != null)
						schemaLocationMap.put(gen.getNamespaceUri(), gen.getNamespaceUri() + " " + gen.getSchemaLocation());

					if (config.getProject().getExporter().getAppearances().isSetExportAppearance()) {
						AppearanceModule app = (AppearanceModule)CityGMLModules.getModuleByTypeAndVersion(CityGMLModuleType.APPEARANCE, core.getModuleVersion());
						saxWriter.forceNSDecl(app.getNamespaceUri(), app.getNamespacePrefix());

						if (app.getSchemaLocation() != null)
							schemaLocationMap.put(app.getNamespaceUri(), app.getNamespaceUri() + " " + app.getSchemaLocation());
					}
				}

				if (schemaLocation != null)
					schemaLocationMap.put(namespaceUri, namespaceUri + " " + module.getSchemaLocation());
			}

			if (moduleList.contains(CoreModule.v0_4_0))
				saxWriter.forceNSDecl(CoreModule.v0_4_0.getNamespaceUri(), "");
			else if (moduleList.contains(CoreModule.v1_0_0))
				saxWriter.forceNSDecl(CoreModule.v1_0_0.getNamespaceUri(), "");

			// create worker pools
			// here we have an open issue: queue sizes are fix...
			xlinkExporterPool = new WorkerPool<DBXlink>(
					minThreads,
					maxThreads,
					new DBExportXlinkWorkerFactory(dbPool, config, eventDispatcher),
					300,
					false);

			ioWriterPool = new SingleWorkerPool<Vector<SAXEvent>>(
					new IOWriterWorkerFactory(saxWriter),
					100,
					true);

			dbWorkerPool = new WorkerPool<DBSplittingResult>(
					minThreads,
					maxThreads,
					new DBExportWorkerFactory(
							jaxbContext,
							dbPool,
							ioWriterPool,
							xlinkExporterPool,
							lookupServerManager,
							cityGMLFactory,
							exportFilter,
							config,
							eventDispatcher),
					300,
					false);

			// prestart pool workers
			xlinkExporterPool.prestartCoreWorkers();
			ioWriterPool.prestartCoreWorkers();
			dbWorkerPool.prestartCoreWorkers();

			// ok, preparations done. inform user...
			LOG.info("Exporting to file: " + fileName);

			XMLHeaderWriter xmlHeader = new XMLHeaderWriter(saxWriter);
			CityModel cm = cityGMLFactory.createCityModel(moduleVersion.getCore().getModule());
			JAXBElement<?> cityModel = cityGMLFactory.cityGML2jaxb(cm);

			Properties props = new Properties();
			props.put(Marshaller.JAXB_FRAGMENT, new Boolean(true));
			props.put(Marshaller.JAXB_SCHEMA_LOCATION, Util.collection2string(schemaLocationMap.values(), " "));
			try {
				xmlHeader.setRootElement(cityModel, jaxbContext, props);
				xmlHeader.startRootElement();
			} catch (JAXBException jaxBE) {
				LOG.error("I/O error: " + jaxBE.getMessage());
				return false;
			} catch (SAXException saxE) {
				LOG.error("I/O error: " + saxE.getMessage());
				return false;
			}

			// flush writer to make sure header has been written
			try {
				saxWriter.flush();
			} catch (IOException ioE) {
				LOG.error("I/O error: " + ioE.getMessage());
				return false;
			}

			// get database splitter and start query
			dbSplitter = null;
			try {
				dbSplitter = new DBSplitter(
						dbPool,
						dbWorkerPool,
						exportFilter,
						eventDispatcher,
						config);

				if (shouldRun)
					dbSplitter.startQuery();
			} catch (SQLException sqlE) {
				LOG.error("SQL error: " + sqlE.getMessage());
				return false;
			}

			try {
				if (shouldRun)
					dbWorkerPool.shutdownAndWait();

				ioWriterPool.shutdownAndWait();
				xlinkExporterPool.shutdownAndWait();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
			}

			// write footer element
			try {
				xmlHeader.endRootElement();
			} catch (SAXException saxE) {
				LOG.error("XML error: " + saxE.getMessage());
				return false;
			}

			// flush sax writer and close file
			try {
				saxWriter.flush();
				fileWriter.close();
			} catch (IOException ioE) {
				LOG.error("I/O error: " + ioE.getMessage());
				return false;
			}

			eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("export.dialog.finish.msg")));
			
			// cleaning up...
			try {
				LOG.info("Cleaning temporary cache.");
				cacheManager.dropAll();
			} catch (SQLException sqlE) {
				LOG.error("SQL error: " + sqlE.getMessage());
				return false;
			}
			
			try {
				lookupServerManager.shutdownAll();
			} catch (SQLException e) {
				LOG.error("SQL error: " + e.getMessage());
			}
			
			try {
				dbPool.refresh();
			} catch (SQLException e) {
				//
			}
			
			// finally join eventDispatcher
			try {
				eventDispatcher.join();
			} catch (InterruptedException iE) {
				LOG.error("Internal error: " + iE.getMessage());
				return false;
			}

			// set null
			cacheManager = null;
			xlinkExporterPool = null;
			ioWriterPool = null;
			dbWorkerPool = null;
			dbSplitter = null;

			// show exported features
			if (!featureCounterMap.isEmpty()) {
				LOG.info("Exported CityGML features:");
				for (CityGMLClass type : featureCounterMap.keySet())
					LOG.info(type + ": " + featureCounterMap.get(type));
			}

			long geometryObjects = 0;
			for (GMLClass type : geometryCounterMap.keySet())
				geometryObjects += geometryCounterMap.get(type);

			if (geometryObjects != 0)
				LOG.info("Processed geometry objects: " + geometryObjects);

			return shouldRun;

		} finally {

			if (cacheManager != null) {
				try {
					LOG.info("Cleaning temporary cache.");
					cacheManager.dropAll();
					cacheManager = null;
				} catch (SQLException sqlEx) {
					LOG.error("SQL error while finishing database export: " + sqlEx.getMessage());
				}
			}
		}
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.FeatureCounter) {
			HashMap<CityGMLClass, Long> counterMap = ((FeatureCounterEvent)e).getCounter();

			for (CityGMLClass type : counterMap.keySet()) {
				Long counter = featureCounterMap.get(type);
				Long update = counterMap.get(type);

				if (counter == null)
					featureCounterMap.put(type, update);
				else
					featureCounterMap.put(type, counter + update);
			}
		}

		else if (e.getEventType() == EventType.GeometryCounter) {
			HashMap<GMLClass, Long> counterMap = ((GeometryCounterEvent)e).getCounter();

			for (GMLClass type : counterMap.keySet()) {
				Long counter = geometryCounterMap.get(type);
				Long update = counterMap.get(type);

				if (counter == null)
					geometryCounterMap.put(type, update);
				else
					geometryCounterMap.put(type, counter + update);
			}
		}
		
		else if (e.getEventType() == EventType.Interrupt) {
			if (isInterrupted.compareAndSet(false, true)) {
				shouldRun = false;

				String log = ((InterruptEvent)e).getLogMessage();
				if (log != null)
					LOG.log(((InterruptEvent)e).getLogLevelType(), log);
				
				if (dbSplitter != null)
					dbSplitter.shutdown();

				if (dbWorkerPool != null) {
					dbWorkerPool.shutdownNow();
				}
			}
		}
	}
}
