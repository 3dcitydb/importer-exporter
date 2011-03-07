package de.tub.citydb.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

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
import de.tub.citydb.db.cache.DBExportCache;
import de.tub.citydb.db.cache.DBGmlIdLookupServerEnum;
import de.tub.citydb.db.cache.DBGmlIdLookupServerManager;
import de.tub.citydb.db.exporter.DBSplitter;
import de.tub.citydb.db.exporter.DBSplittingResult;
import de.tub.citydb.db.temp.DBTempTableManager;
import de.tub.citydb.db.temp.model.DBTempTableModelEnum;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.info.LogMessageEvent;
import de.tub.citydb.event.statistic.FeatureCounterEvent;
import de.tub.citydb.event.statistic.GeometryCounterEvent;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.io.XMLHeaderWriter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.sax.SAXWriter;
import de.tub.citydb.sax.SAXBuffer.SAXEvent;
import de.tub.citydb.util.Util;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.CityGMLModule;
import de.tub.citygml4j.model.citygml.CityGMLModuleType;
import de.tub.citygml4j.model.citygml.core.CityModel;
import de.tub.citygml4j.model.citygml.core.CoreModule;
import de.tub.citygml4j.model.gml.GMLClass;

public class Exporter implements EventListener {
	private Logger LOG = Logger.getInstance();

	private final JAXBContext jaxbContext;
	private final DBConnectionPool dbPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private DBSplitter dbSplitter;
	private volatile boolean shouldRun = true;

	private ReentrantLock mainLock = new ReentrantLock();
	private WorkerPool<DBSplittingResult> dbWorkerPool;
	private SingleWorkerPool<Vector<SAXEvent>> ioWriterPool;
	private WorkerPool<DBXlink> xlinkExporterPool;
	private DBTempTableManager dbTempTableManager;
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
		// determine pool sizes
		int coreSize = 2;
		int maxSize = 2;

		de.tub.citydb.config.project.system.System system = config.getProject().getExporter().getSystem();
		Database database = config.getProject().getDatabase();

		Integer coreSizeProp = system.getThreadPool().getDefaultPool().getMinThreads();
		if (coreSizeProp != null && coreSizeProp > 0)
			coreSize = coreSizeProp;

		Integer maxSizeProp = system.getThreadPool().getDefaultPool().getMaxThreads();
		if (maxSizeProp != null && maxSizeProp > 0)
			maxSize = maxSizeProp;

		// check thread input
		if (coreSize > maxSize)
			coreSize = maxSize;

		// refill values
		system.getThreadPool().getDefaultPool().setMinThreads(coreSize);
		system.getThreadPool().getDefaultPool().setMaxThreads(maxSize);

		// check lookup cache
		Integer geometryCache = system.getGmlIdLookupServer().getGeometry().getCacheSize();
		Integer featureCache = system.getGmlIdLookupServer().getFeature().getCacheSize();
		Float geometryCacheDrainFactor = system.getGmlIdLookupServer().getGeometry().getPageFactor();
		Float featureCacheDrainFactor = system.getGmlIdLookupServer().getFeature().getPageFactor();
		Integer geometryPartitions = system.getGmlIdLookupServer().getGeometry().getPartitions();
		Integer featurePartitions = system.getGmlIdLookupServer().getFeature().getPartitions();
		Integer batchSize = database.getUpdateBatching().getGmlIdLookupServerBatchValue();
		
		if (geometryCache <= 0)
			geometryCache = 200000;

		if (featureCache <= 0)
			featureCache = 200000;

		if (geometryCacheDrainFactor <= 0 || geometryCacheDrainFactor > 1)
			geometryCacheDrainFactor = .85f;

		if (featureCacheDrainFactor <= 0 || featureCacheDrainFactor > 1)
			featureCacheDrainFactor = .85f;

		if (geometryPartitions <= 0 || geometryPartitions >= 100)
			geometryPartitions = 10;
		
		if (featurePartitions <= 0 || featurePartitions >= 100)
			featurePartitions = 10;
		
		if (batchSize == null || batchSize <= 0 || batchSize > Internal.ORACLE_MAX_BATCH_SIZE)
			batchSize = Internal.ORACLE_MAX_BATCH_SIZE;
		
		// refill values
		system.getGmlIdLookupServer().getGeometry().setCacheSize(geometryCache);
		system.getGmlIdLookupServer().getFeature().setCacheSize(featureCache);
		system.getGmlIdLookupServer().getGeometry().setPageFactor(geometryCacheDrainFactor);
		system.getGmlIdLookupServer().getFeature().setPageFactor(featureCacheDrainFactor);
		system.getGmlIdLookupServer().getGeometry().setPartitions(geometryPartitions);
		system.getGmlIdLookupServer().getFeature().setPartitions(featurePartitions);
		database.getUpdateBatching().setGmlIdLookupServerBatchValue(batchSize);
		
		// calc queueSize
		// how to properly calculate?
		int dbQueueSize = maxSize * 20;
		int writerQueueSize = maxSize * 100;

		// checking workspace... this should be improved in future...
		String workspace = database.getWorkspace().getExportWorkspace();
		String date = database.getWorkspace().getExportDate();

		if (workspace != null) {
			workspace = workspace.trim();

			if (!workspace.toUpperCase().equals("LIVE")) {
				boolean workspaceExists = dbPool.checkWorkspace(workspace);

				if (!workspaceExists) {
					LOG.error("Der Datenbank-Workspace '" + workspace + "' ist nicht verfügbar.");
					return false;
				} else {
					String message = "Datenbank-Workspace '" + workspace + "'";

					if (date != null && date.trim().length() != 0) {
						date = date.trim();
						message += " zum Zeitpunkt " + date;
					}

					message += " wird gewählt.";
					LOG.info(message);
				}
			}
		}

		// getting export filter
		exportFilter = new ExportFilter(config);

		// CityGML object factory
		cityGMLFactory = new CityGMLFactory();

		try {
			// checking file
			Internal intConfig = config.getInternal();
			String fileName = intConfig.getExportFileName();
			File file = new File(fileName);
			File path = new File(file.getAbsolutePath());
			intConfig.setExportPath(path.getParent());

			// open file for writing
			FileWriter fileWriter = null;
			try {
				fileWriter = new FileWriter(fileName);
			} catch (IOException ioE) {
				LOG.error("Datei '" + fileName + "' kann nicht zum Schreiben geöffnet werden: " + ioE.getMessage());
				return false;
			}

			// checking export path for texture images
			ExpAppearance appearances = config.getProject().getExporter().getAppearances();
			if (appearances.isSetExportTextureFiles()) {
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
							LOG.error("Verzeichnis für Texturbilder '" + exportPath.toString() + "' kann nicht zum Schreiben geöffnet werden.");
							return false;
						} else if (!exportPath.isDirectory()) {
							boolean success = exportPath.mkdirs();

							if (!success) {
								LOG.error("Verzeichnis für Texturbilder '" + exportPath.toString() + "' kann nicht angelegt werden.");
								return false;
							} else
								LOG.info("Unterverzeichnis '" + textureExportPath + "' für Texturbilder angelegt.");
						}

						intConfig.setExportTextureFilePath(textureExportPath);
					} else {
						File exportPath = new File(tmp.getAbsolutePath());

						if (!exportPath.exists() || !exportPath.isDirectory() || !exportPath.canWrite()) {
							LOG.error("Verzeichnis für Texturbilder '" + exportPath.toString() + "' kann nicht zum Schreiben geöffnet werden.");
							return false;
						}

						intConfig.setExportTextureFilePath(exportPath.toString());
					}
				}
			}

			// adding listeners
			eventDispatcher.addListener(EventType.LogMessage, this);
			eventDispatcher.addListener(EventType.FeatureCounter, this);
			eventDispatcher.addListener(EventType.GeometryCounter, this);

			// create instance of temp table manager
			dbTempTableManager = new DBTempTableManager(dbPool, maxSize);

			// create instance of gml:id lookup server manager...
			lookupServerManager = new DBGmlIdLookupServerManager(eventDispatcher);

			// ...and start servers
			try {		
				lookupServerManager.initServer(
						DBGmlIdLookupServerEnum.GEOMETRY,
						new DBExportCache(dbTempTableManager, DBTempTableModelEnum.GMLID_GEOMETRY, geometryPartitions, batchSize),
						geometryCache,
						geometryCacheDrainFactor,
						maxSize);

				lookupServerManager.initServer(
						DBGmlIdLookupServerEnum.FEATURE,
						new DBExportCache(dbTempTableManager, DBTempTableModelEnum.GMLID_FEATURE, featurePartitions, batchSize),
						featureCache,
						featureCacheDrainFactor,
						maxSize);
			} catch (SQLException sqlEx) {
				LOG.error("SQL-Fehler bei der Initialisierung des Exports: " + sqlEx.getMessage());
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

			for (CityGMLModule module : CityGMLModuleType.getModules())
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
					saxWriter.forceNSDecl(module.getCoreDependency().getNamespaceUri(), module.getCoreDependency().getNamespacePrefix());
					saxWriter.forceNSDecl(module.getGenericsDependency().getNamespaceUri(), module.getGenericsDependency().getNamespacePrefix());

					if (module.getCoreDependency().getSchemaLocation() != null)
						schemaLocationMap.put(module.getCoreDependency().getNamespaceUri(), module.getCoreDependency().getNamespaceUri() + " " + module.getCoreDependency().getSchemaLocation());
					if (module.getGenericsDependency().getSchemaLocation() != null)
						schemaLocationMap.put(module.getGenericsDependency().getNamespaceUri(), module.getGenericsDependency().getNamespaceUri() + " " + module.getGenericsDependency().getSchemaLocation());

					if (config.getProject().getExporter().getAppearances().isSetExportAppearance()) {
						saxWriter.forceNSDecl(module.getAppearanceDependency().getNamespaceUri(), module.getAppearanceDependency().getNamespacePrefix());

						if (module.getAppearanceDependency().getSchemaLocation() != null)
							schemaLocationMap.put(module.getAppearanceDependency().getNamespaceUri(), module.getAppearanceDependency().getNamespaceUri() + " " + module.getAppearanceDependency().getSchemaLocation());
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
					coreSize,
					maxSize,
					new DBExportXlinkWorkerFactory(dbPool, config, eventDispatcher),
					300,
					false);

			ioWriterPool = new SingleWorkerPool<Vector<SAXEvent>>(
					new IOWriterWorkerFactory(saxWriter),
					100,
					true);

			dbWorkerPool = new WorkerPool<DBSplittingResult>(
					coreSize,
					maxSize,
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
			LOG.info("Exportiere in Datei: " + fileName);

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
				LOG.error("I/O-Fehler: " + jaxBE.getMessage());
				return false;
			} catch (SAXException saxE) {
				LOG.error("I/O-Fehler: " + saxE.getMessage());
				return false;
			}

			// flush writer to make sure header has been written
			try {
				saxWriter.flush();
			} catch (IOException ioE) {
				LOG.error("I/O-Fehler: " + ioE.getMessage());
				return false;
			}

			// get database splitter and start query
			dbSplitter = null;
			try {
				dbSplitter = new DBSplitter(
						dbPool,
						dbWorkerPool,
						exportFilter,
						config,
						eventDispatcher);

				if (shouldRun)
					dbSplitter.startQuery();
			} catch (SQLException sqlE) {
				LOG.error("SQL-Fehler: " + sqlE.getMessage());
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
				LOG.error("I/O-Fehler: " + saxE.getMessage());
				return false;
			}

			// flush sax writer and close file
			try {
				saxWriter.flush();
				fileWriter.close();
			} catch (IOException ioE) {
				LOG.error("I/O-Fehler: " + ioE.getMessage());
				return false;
			}

			// cleaning up...
			try {
				LOG.info("Temporärer Cache wird bereinigt.");
				dbTempTableManager.dropAll();
				dbTempTableManager = null;
			} catch (SQLException sqlE) {
				LOG.error("I/O-Fehler: " + sqlE.getMessage());
				return false;
			}

			// finally shutdown eventDispatcher
			try {
				eventDispatcher.join();
			} catch (InterruptedException iE) {
				LOG.error("Interner Fehler: " + iE.getMessage());
				return false;
			}

			// set null
			dbTempTableManager = null;
			xlinkExporterPool = null;
			ioWriterPool = null;
			dbWorkerPool = null;
			dbSplitter = null;

			// show exported features
			if (!featureCounterMap.isEmpty()) {
				LOG.info("Exportierte CityGML Objekte:");
				for (CityGMLClass type : featureCounterMap.keySet())
					LOG.info(type + ": " + featureCounterMap.get(type));
			}

			long geometryObjects = 0;
			for (GMLClass type : geometryCounterMap.keySet())
				geometryObjects += geometryCounterMap.get(type);

			if (geometryObjects != 0)
				LOG.info("Verarbeitete Geometrie-Objekte: " + geometryObjects);

			return shouldRun;

		} finally {

			if (dbTempTableManager != null) {
				try {
					LOG.info("Temporärer Cache wird bereinigt.");
					dbTempTableManager.dropAll();
					dbTempTableManager = null;
				} catch (SQLException sqlEx) {
					LOG.error("SQL-Fehler bei der Beendigung des Exports: " + sqlEx.getMessage());
				}
			}
		}
	}

	public void shutdown() {
		if (!shouldRun)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();
		try {
			shouldRun = false;

			if (dbSplitter != null)
				dbSplitter.shutdown();

			if (dbWorkerPool != null) {
				dbWorkerPool.shutdownNow();
			}

		} finally {
			lock.unlock();
		}
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.LogMessage) {
			String msg = ((LogMessageEvent)e).getMessage();
			msg = msg.replaceAll("\n*$", "");

			switch (((LogMessageEvent)e).getMessageType()) {
			case DEBUG:
				LOG.debug(msg);
				break;
			case INFO:
				LOG.info(msg);
				break;
			case WARN:
				LOG.warn(msg);
				break;
			case ERROR:
				LOG.error(msg);
				break;
			}
		}

		else if (e.getEventType() == EventType.FeatureCounter) {
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
	}
}
