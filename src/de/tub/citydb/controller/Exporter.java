/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 *
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.geometry.BoundingVolume;
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
import de.tub.citydb.config.project.database.ReferenceSystem;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.exporter.ExportAppearance;
import de.tub.citydb.config.project.exporter.ModuleVersion;
import de.tub.citydb.config.project.filter.TileNameSuffixMode;
import de.tub.citydb.config.project.filter.TileSuffixMode;
import de.tub.citydb.config.project.filter.Tiling;
import de.tub.citydb.config.project.filter.TilingMode;
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
import de.tub.citydb.sax.SAXBuffer;
import de.tub.citydb.sax.SAXWriter;
import de.tub.citydb.sax.XMLHeaderWriter;
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
	private SingleWorkerPool<SAXBuffer> ioWriterPool;
	private WorkerPool<DBXlink> xlinkExporterPool;
	private CacheManager cacheManager;
	private DBGmlIdLookupServerManager lookupServerManager;
	private ExportFilter exportFilter;
	private CityGMLFactory cityGMLFactory;
	private boolean useTiling;

	private EnumMap<CityGMLClass, Long> totalFeatureCounterMap;
	private EnumMap<GMLClass, Long> totalGeometryCounterMap;
	private EnumMap<CityGMLClass, Long> featureCounterMap;
	private EnumMap<GMLClass, Long> geometryCounterMap;

	public Exporter(JAXBContext jaxbContext, DBConnectionPool dbPool, Config config, EventDispatcher eventDispatcher) {
		this.jaxbContext = jaxbContext;
		this.dbPool = dbPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		featureCounterMap = new EnumMap<CityGMLClass, Long>(CityGMLClass.class);
		geometryCounterMap = new EnumMap<GMLClass, Long>(GMLClass.class);
		totalFeatureCounterMap = new EnumMap<CityGMLClass, Long>(CityGMLClass.class);
		totalGeometryCounterMap = new EnumMap<GMLClass, Long>(GMLClass.class);
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

		// adding listeners
		eventDispatcher.addListener(EventType.FeatureCounter, this);
		eventDispatcher.addListener(EventType.GeometryCounter, this);
		eventDispatcher.addListener(EventType.Interrupt, this);

		// checking workspace... this should be improved in future...
		Workspace workspace = database.getWorkspaces().getExportWorkspace();
		if (shouldRun && !workspace.getName().toUpperCase().equals("LIVE")) {
			boolean workspaceExists = dbPool.checkWorkspace(workspace);

			String name = "'" + workspace.getName().trim() + "'";
			String timestamp = workspace.getTimestamp().trim();
			if (timestamp.trim().length() > 0)
				name += " at timestamp " + timestamp;
			
			if (!workspaceExists) {
				LOG.error("Database workspace " + name + " is not available.");
				return false;
			} else 
				LOG.info("Switching to database workspace " + name + '.');
		}

		// create a saxWriter instance 
		// define indent for xml output and namespace mappings
		SAXWriter saxWriter = new SAXWriter();
		saxWriter.setIndentString("  ");
		saxWriter.forceNSDecl("http://www.opengis.net/gml", "gml");
		saxWriter.forceNSDecl("http://www.w3.org/1999/xlink", "xlink");
		saxWriter.forceNSDecl("http://www.w3.org/2001/SMIL20/", "smil20");
		saxWriter.forceNSDecl("http://www.w3.org/2001/SMIL20/Language", "smil20lang");
		saxWriter.forceNSDecl("urn:oasis:names:tc:ciq:xsdschema:xAL:2.0", "xAL");
		saxWriter.forceNSDecl("http://www.w3.org/2001/XMLSchema-instance", "xsi");

		for (CityGMLModule module : CityGMLModules.getModules())
			saxWriter.suppressNSDecl(module.getNamespaceUri());

		// prepare namespace prefixes and schemaLocation attribute value...
		ModuleVersion moduleVersion = config.getProject().getExporter().getModuleVersion();
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

		if (moduleList.contains(CoreModule.v1_0_0))
			saxWriter.forceNSDecl(CoreModule.v1_0_0.getNamespaceUri(), "");
		else if (moduleList.contains(CoreModule.v0_4_0))
			saxWriter.forceNSDecl(CoreModule.v0_4_0.getNamespaceUri(), "");

		// create CityModel root element
		cityGMLFactory = new CityGMLFactory();		
		CityModel cm = cityGMLFactory.createCityModel(moduleVersion.getCore().getModule());
		JAXBElement<?> cityModel = cityGMLFactory.cityGML2jaxb(cm);

		Properties props = new Properties();
		props.put(Marshaller.JAXB_FRAGMENT, new Boolean(true));
		props.put(Marshaller.JAXB_SCHEMA_LOCATION, Util.collection2string(schemaLocationMap.values(), " "));

		// checking file
		Internal internalConfig = config.getInternal();
		File exportFile = new File(internalConfig.getExportFileName());
		String fileName = exportFile.getName();
		String folderName = new File(exportFile.getAbsolutePath()).getParent();

		String fileExtension = Util.getFileExtension(fileName);		
		if (fileExtension == null)
			fileExtension = "gml";
		else
			fileName = Util.stripFileExtension(fileName);

		// create export folder
		File folder = new File(folderName);
		if (!folder.exists() && !folder.mkdirs()) {
			LOG.error("Failed to create folder '" + folderName + "'.");
			return false;
		}
		
		// set target reference system for export
		ReferenceSystem targetSRS = config.getProject().getExporter().getTargetSRS();
		internalConfig.setTransformCoordinates(targetSRS.isSupported() && 
				targetSRS.getSrid() != config.getInternal().getOpenConnection().getMetaData().getSrid());
		if (internalConfig.isTransformCoordinates()) {
			internalConfig.setExportTargetSRS(targetSRS);
			LOG.info("Transforming geometry representation to reference system '" + targetSRS.getDescription() + "' (SRID: " + targetSRS.getSrid() + ").");
			LOG.warn("Transformation is NOT applied to height reference system.");
		}

		// getting export filter
		exportFilter = new ExportFilter(config, DBUtil.getInstance(dbPool));

		// bounding box config
		Tiling tiling = config.getProject().getExporter().getFilter().getComplexFilter().getTiledBoundingBox().getTiling();
		useTiling = exportFilter.getBoundingBoxFilter().isActive() && tiling.getMode() != TilingMode.NO_TILING;

		int rows = useTiling ? tiling.getRows() : 1;  
		int columns = useTiling ? tiling.getColumns() : 1;

		for (int i = 0; shouldRun && i < rows; i++) {
			for (int j = 0; shouldRun && j < columns; j++) {

				try {
					File file = null;

					if (useTiling) {
						exportFilter.getBoundingBoxFilter().setActiveTile(i, j);

						// create suffix for folderName and fileName
						TileSuffixMode suffixMode = tiling.getTilePathSuffix();
						String suffix = "";

						BoundingVolume bbox = exportFilter.getBoundingBoxFilter().getFilterState();
						double minX = bbox.getLowerCorner().getX();
						double minY = bbox.getLowerCorner().getY();
						double maxX = bbox.getUpperCorner().getX();
						double maxY = bbox.getUpperCorner().getY();

						switch (suffixMode) {
						case XMIN_YMIN:
							suffix = String.valueOf(minX) + '_' + String.valueOf(minY);
							break;
						case XMAX_YMIN:
							suffix = String.valueOf(maxX) + '_' + String.valueOf(minY);
							break;
						case XMIN_YMAX:
							suffix = String.valueOf(minX) + '_' + String.valueOf(maxY);
							break;
						case XMAX_YMAX:
							suffix = String.valueOf(maxX) + '_' + String.valueOf(maxY);
							break;
						case XMIN_YMIN_XMAX_YMAX:
							suffix = String.valueOf(minX) + '_' + String.valueOf(minY) + '_' + String.valueOf(maxX) + '_' + String.valueOf(maxY);
							break;
						default:
							suffix = String.valueOf(i) + '_' + String.valueOf(j);
						}

						String subfolderName = folderName + File.separator + tiling.getTilePath() + '_'  + suffix;
						File subfolder = new File(subfolderName);
						if (!subfolder.exists() && !subfolder.mkdirs()) {
							LOG.error("Failed to create tiling subfolder '" + subfolderName + "'.");
							return false;
						}

						if (tiling.getTileNameSuffix() == TileNameSuffixMode.SAME_AS_PATH)
							file = new File(subfolderName + File.separator + fileName + '_'  + suffix + '.' + fileExtension);
						else // no suffix for filename
							file = new File(subfolderName + File.separator + fileName + '.' + fileExtension);
					}

					else // no tiling
						file = new File(folderName + File.separator + fileName + '.' + fileExtension);

					config.getInternal().setExportFileName(file.getAbsolutePath());
					File path = new File(file.getAbsolutePath());
					internalConfig.setExportPath(path.getParent());

					eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("export.dialog.cityObj.msg")));
					eventDispatcher.triggerEvent(new StatusDialogTitle(file.getName()));

					// checking export path for texture images
					ExportAppearance appearances = config.getProject().getExporter().getAppearances();
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

								internalConfig.setExportTextureFilePath(textureExportPath);
							} else {
								File exportPath = new File(tmp.getAbsolutePath());

								if (!exportPath.exists() || !exportPath.isDirectory() || !exportPath.canWrite()) {
									LOG.error("Failed to open texture files folder '" + exportPath.toString() + "' for writing.");
									return false;
								}

								internalConfig.setExportTextureFilePath(exportPath.toString());
							}
						}
					}

					// open file for writing
					OutputStreamWriter fileWriter = null;
					try {
						Charset charset = Charset.forName("UTF-8");
						fileWriter = new OutputStreamWriter(new FileOutputStream(file), charset);
					} catch (IOException ioE) {
						LOG.error("Failed to open file '" + fileName + "' for writing: " + ioE.getMessage());
						return false;
					}

					// reset SAXWriter
					saxWriter.reset();
					saxWriter.setWriter(fileWriter);				

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

					// create worker pools
					// here we have an open issue: queue sizes are fix...
					xlinkExporterPool = new WorkerPool<DBXlink>(
							minThreads,
							maxThreads,
							new DBExportXlinkWorkerFactory(dbPool, config, eventDispatcher),
							300,
							false);

					ioWriterPool = new SingleWorkerPool<SAXBuffer>(
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
					LOG.info("Exporting to file: " + file.getAbsolutePath());

					// create file header
					XMLHeaderWriter xmlHeader = new XMLHeaderWriter(saxWriter);

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
					lookupServerManager = null;
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

					featureCounterMap.clear();
					geometryCounterMap.clear();

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
		}

		if (useTiling && (rows > 1 || columns > 1)) { // show total exported features
			if (!totalFeatureCounterMap.isEmpty()) {
				LOG.info("Total exported CityGML features:");
				for (CityGMLClass type : totalFeatureCounterMap.keySet())
					LOG.info(type + ": " + totalFeatureCounterMap.get(type));
			}

			long geometryObjects = 0;
			for (GMLClass type : totalGeometryCounterMap.keySet())
				geometryObjects += totalGeometryCounterMap.get(type);

			if (geometryObjects != 0)
				LOG.info("Total processed geometry objects: " + geometryObjects);
		}

		return shouldRun;
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

				if (useTiling) {
					counter = totalFeatureCounterMap.get(type);

					if (counter == null)
						totalFeatureCounterMap.put(type, update);
					else
						totalFeatureCounterMap.put(type, counter + update);
				}
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

				if (useTiling) {
					counter = totalGeometryCounterMap.get(type);

					if (counter == null)
						totalGeometryCounterMap.put(type, update);
					else
						totalGeometryCounterMap.put(type, counter + update);
				}
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
