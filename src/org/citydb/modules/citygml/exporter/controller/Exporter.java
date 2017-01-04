/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.modules.citygml.exporter.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import org.citydb.api.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.api.concurrent.SingleWorkerPool;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.event.EventHandler;
import org.citydb.api.geometry.BoundingBox;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.filter.TileNameSuffixMode;
import org.citydb.config.project.filter.TileSuffixMode;
import org.citydb.config.project.filter.Tiling;
import org.citydb.config.project.filter.TilingMode;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.database.IndexStatusInfo.IndexType;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.common.database.uid.UIDCacheType;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.exporter.concurrent.DBExportWorkerFactory;
import org.citydb.modules.citygml.exporter.concurrent.DBExportXlinkWorkerFactory;
import org.citydb.modules.citygml.exporter.database.content.DBSplitter;
import org.citydb.modules.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.modules.citygml.exporter.database.uid.FeatureGmlIdCache;
import org.citydb.modules.citygml.exporter.database.uid.GeometryGmlIdCache;
import org.citydb.modules.citygml.exporter.util.FeatureWriterFactory;
import org.citydb.modules.common.concurrent.IOWriterWorkerFactory;
import org.citydb.modules.common.event.CounterEvent;
import org.citydb.modules.common.event.CounterType;
import org.citydb.modules.common.event.EventType;
import org.citydb.modules.common.event.FeatureCounterEvent;
import org.citydb.modules.common.event.GeometryCounterEvent;
import org.citydb.modules.common.event.InterruptEvent;
import org.citydb.modules.common.event.StatusDialogMessage;
import org.citydb.modules.common.event.StatusDialogTitle;
import org.citydb.modules.common.filter.ExportFilter;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.builder.jaxb.xml.io.writer.JAXBModelWriter;
import org.citygml4j.builder.jaxb.xml.io.writer.JAXBOutputFactory;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.ModuleContext;
import org.citygml4j.model.module.citygml.CityGMLModule;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.model.module.citygml.CityGMLVersion;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXWriter;
import org.citygml4j.xml.io.writer.CityGMLWriteException;
import org.citygml4j.xml.io.writer.CityModelInfo;
import org.xml.sax.SAXException;

public class Exporter implements EventHandler {
	private final Logger LOG = Logger.getInstance();

	private final JAXBBuilder jaxbBuilder;
	private final DatabaseConnectionPool dbPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private DBSplitter dbSplitter;

	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);

	private WorkerPool<DBSplittingResult> dbWorkerPool;
	private SingleWorkerPool<SAXEventBuffer> ioWriterPool;
	private WorkerPool<DBXlink> xlinkExporterPool;
	private CacheTableManager cacheTableManager;
	private UIDCacheManager uidCacheManager;
	private ExportFilter exportFilter;
	private boolean useTiling;

	private EnumMap<CityGMLClass, Long> totalFeatureCounterMap;
	private EnumMap<GMLClass, Long> totalGeometryCounterMap;
	private EnumMap<CityGMLClass, Long> featureCounterMap;
	private EnumMap<GMLClass, Long> geometryCounterMap;

	public Exporter(JAXBBuilder jaxbBuilder, DatabaseConnectionPool dbPool, Config config, EventDispatcher eventDispatcher) {
		this.jaxbBuilder = jaxbBuilder;
		this.dbPool = dbPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		featureCounterMap = new EnumMap<CityGMLClass, Long>(CityGMLClass.class);
		geometryCounterMap = new EnumMap<GMLClass, Long>(GMLClass.class);
		totalFeatureCounterMap = new EnumMap<CityGMLClass, Long>(CityGMLClass.class);
		totalGeometryCounterMap = new EnumMap<GMLClass, Long>(GMLClass.class);
	}

	public void cleanup() {
		eventDispatcher.removeEventHandler(this);
	}

	public boolean doProcess() throws CityGMLExportException {
		// adding listeners
		eventDispatcher.addEventHandler(EventType.FEATURE_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.GEOMETRY_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		// checking workspace
		Workspace workspace = config.getProject().getDatabase().getWorkspaces().getExportWorkspace();
		if (shouldRun && dbPool.getActiveDatabaseAdapter().hasVersioningSupport() && 
				!dbPool.getActiveDatabaseAdapter().getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getName()) &&
				!dbPool.getActiveDatabaseAdapter().getWorkspaceManager().existsWorkspace(workspace, true))
			return false;

		// prepare SAX writer
		SAXWriter saxWriter = new SAXWriter();
		saxWriter.setWriteEncoding(true);
		saxWriter.setIndentString("  ");
		saxWriter.setHeaderComment("Written by " + this.getClass().getPackage().getImplementationTitle() + ", version \"" +
				this.getClass().getPackage().getImplementationVersion() + '"', 
				this.getClass().getPackage().getImplementationVendor());

		// set CityGML prefixes and schema locations
		CityGMLVersion version = Util.toCityGMLVersion(config.getProject().getExporter().getCityGMLVersion());
		ModuleContext moduleContext = new ModuleContext(version);
		saxWriter.setDefaultNamespace(moduleContext.getModule(CityGMLModuleType.CORE).getNamespaceURI());

		for (Module module : moduleContext.getModules()) {
			if (module.getType() == CityGMLModuleType.CORE)
				continue;

			if (!config.getProject().getExporter().getAppearances().isSetExportAppearance() 
					&& module.getType() == CityGMLModuleType.APPEARANCE)
				continue;

			saxWriter.setPrefix(module.getNamespacePrefix(), module.getNamespaceURI());
			if (module instanceof CityGMLModule)
				saxWriter.setSchemaLocation(module.getNamespaceURI(), module.getSchemaLocation());
		}

		// set target reference system for export
		DatabaseSrs targetSRS = config.getProject().getExporter().getTargetSRS();
		if (!targetSRS.isSupported())
			targetSRS = dbPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem();

		config.getInternal().setExportTargetSRS(targetSRS);
		config.getInternal().setTransformCoordinates(targetSRS.isSupported() && 
				targetSRS.getSrid() != dbPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().getSrid());

		if (config.getInternal().isTransformCoordinates()) {
			if (targetSRS.is3D() == dbPool.getActiveDatabaseAdapter().getConnectionMetaData().getReferenceSystem().is3D()) {
				LOG.info("Transforming geometry representation to reference system '" + targetSRS.getDescription() + "' (SRID: " + targetSRS.getSrid() + ").");
				LOG.warn("Transformation is NOT applied to height reference system.");
			} else {
				throw new CityGMLExportException("Dimensionality of reference system for geometry transformation does not match.");
			}
		}

		// check and log index status
		try {
			if (!dbPool.getActiveDatabaseAdapter().getUtil().isIndexEnabled("CITYOBJECT", "ENVELOPE")) {
				LOG.error("Spatial indexes are not activated.");
				LOG.error("Please use the database tab to activate the spatial indexes.");
				return false;
			}
			
			for (IndexType type : IndexType.values())
				dbPool.getActiveDatabaseAdapter().getUtil().getIndexStatus(type).printStatusToConsole();
		} catch (SQLException e) {
			throw new CityGMLExportException("Database error while querying index status.", e);
		}

		// check whether database contains global appearances and set internal flag
		try {
			config.getInternal().setExportGlobalAppearances(config.getProject().getExporter().getAppearances().isSetExportAppearance() && 
					dbPool.getActiveDatabaseAdapter().getUtil().getNumGlobalAppearances(workspace) > 0);
		} catch (SQLException e) {
			throw new CityGMLExportException("Database error while querying the number of global appearances.", e);
		}

		// getting export filter
		exportFilter = new ExportFilter(config);

		// cache gml:ids of city objects in case we have to export groups
		config.getInternal().setRegisterGmlIdInCache((!exportFilter.getFeatureClassFilter().isActive() 
				|| !exportFilter.getFeatureClassFilter().filter(CityGMLClass.CITY_OBJECT_GROUP))
				&& !config.getProject().getExporter().getCityObjectGroup().isExportMemberAsXLinks());

		// bounding box config
		Tiling tiling = config.getProject().getExporter().getFilter().getComplexFilter().getTiledBoundingBox().getTiling();
		useTiling = exportFilter.getBoundingBoxFilter().isActive() && tiling.getMode() != TilingMode.NO_TILING;
		int rows = useTiling ? tiling.getRows() : 1;  
		int columns = useTiling ? tiling.getColumns() : 1;
		
		// prepare files and folders
		File exportFile = new File(config.getInternal().getExportFileName());
		String fileName = exportFile.getName();
		String folderName = exportFile.getAbsoluteFile().getParent();

		String fileExtension = Util.getFileExtension(fileName);		
		if (fileExtension == null)
			fileExtension = "gml";
		else
			fileName = Util.stripFileExtension(fileName);

		File folder = new File(folderName);
		if (!folder.exists() && !folder.mkdirs())
			throw new CityGMLExportException("Failed to create folder '" + folderName + "'.");

		int remainingTiles = rows * columns;
		long start = System.currentTimeMillis();

		for (int i = 0; shouldRun && i < rows; i++) {
			for (int j = 0; shouldRun && j < columns; j++) {

				try {
					File file = null;

					if (useTiling) {
						exportFilter.getBoundingBoxFilter().setActiveTile(i, j);

						// create suffix for folderName and fileName
						TileSuffixMode suffixMode = tiling.getTilePathSuffix();
						String suffix = "";

						BoundingBox bbox = exportFilter.getBoundingBoxFilter().getFilterState();
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

						File subfolder = new File(folderName, tiling.getTilePath() + '_'  + suffix);
						if (!subfolder.exists() && !subfolder.mkdirs())
							throw new CityGMLExportException("Failed to create tiling subfolder '" + subfolder + "'.");

						if (tiling.getTileNameSuffix() == TileNameSuffixMode.SAME_AS_PATH)
							file = new File(subfolder, fileName + '_'  + suffix + '.' + fileExtension);
						else // no suffix for filename
							file = new File(subfolder, fileName + '.' + fileExtension);
					}

					else // no tiling
						file = new File(folderName, fileName + '.' + fileExtension);

					config.getInternal().setExportFileName(file.getAbsolutePath());
					File path = new File(file.getAbsolutePath());
					config.getInternal().setExportPath(path.getParent());

					eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("export.dialog.cityObj.msg"), this));
					eventDispatcher.triggerEvent(new StatusDialogTitle(file.getName(), this));
					eventDispatcher.triggerEvent(new CounterEvent(CounterType.REMAINING_TILES, --remainingTiles, this));

					// checking export path for texture images
					if (config.getProject().getExporter().getAppearances().isSetExportAppearance()) {
						String textureExportPath = null;
						boolean isRelative = config.getProject().getExporter().getAppearances().getTexturePath().isRelative();

						if (isRelative)
							textureExportPath = config.getProject().getExporter().getAppearances().getTexturePath().getRelativePath();
						else
							textureExportPath = config.getProject().getExporter().getAppearances().getTexturePath().getAbsolutePath();

						if (textureExportPath != null && textureExportPath.length() > 0) {
							File tmp = new File(textureExportPath);
							textureExportPath = tmp.getPath();

							if (isRelative) {
								File exportPath = new File(path.getParent(), textureExportPath);

								if (exportPath.isFile() || (exportPath.isDirectory() && !exportPath.canWrite())) {
									throw new CityGMLExportException("Failed to open texture files subfolder '" + exportPath.toString() + "' for writing.");
								} else if (!exportPath.isDirectory()) {
									boolean success = exportPath.mkdirs();

									if (!success)
										throw new CityGMLExportException("Failed to create texture files subfolder '" + exportPath.toString() + "'.");
									else
										LOG.info("Created texture files subfolder '" + textureExportPath + "'.");
								}

								config.getInternal().setExportTextureFilePath(textureExportPath);
							} else {
								File exportPath = new File(tmp.getAbsolutePath());
								if (!exportPath.exists() || !exportPath.isDirectory() || !exportPath.canWrite())
									throw new CityGMLExportException("Failed to open texture files folder '" + exportPath.toString() + "' for writing.");

								config.getInternal().setExportTextureFilePath(exportPath.toString());
							}
						}
					}

					// open file for writing
					try {
						saxWriter.setOutput(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
					} catch (IOException e) {
						throw new CityGMLExportException("Failed to open file '" + fileName + "' for writing.", e);
					}					

					// create instance of temp table manager
					try {
						cacheTableManager = new CacheTableManager(dbPool, 
								config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads(), 
								config);
					} catch (SQLException | IOException e) {
						throw new CityGMLExportException("Failed to initialize internal cache manager.", e);
					}

					// create instance of gml:id lookup server manager...
					uidCacheManager = new UIDCacheManager();

					// ...and start servers
					try {		
						uidCacheManager.initCache(
								UIDCacheType.GEOMETRY,
								new GeometryGmlIdCache(cacheTableManager, 
										config.getProject().getExporter().getResources().getGmlIdCache().getGeometry().getPartitions(),
										config.getProject().getDatabase().getUpdateBatching().getGmlIdCacheBatchValue()),
								config.getProject().getExporter().getResources().getGmlIdCache().getGeometry().getCacheSize(),
								config.getProject().getExporter().getResources().getGmlIdCache().getGeometry().getPageFactor(),
								config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads());

						uidCacheManager.initCache(
								UIDCacheType.FEATURE,
								new FeatureGmlIdCache(cacheTableManager, 
										config.getProject().getExporter().getResources().getGmlIdCache().getFeature().getPartitions(), 
										config.getProject().getDatabase().getUpdateBatching().getGmlIdCacheBatchValue()),
								config.getProject().getExporter().getResources().getGmlIdCache().getFeature().getCacheSize(),
								config.getProject().getExporter().getResources().getGmlIdCache().getFeature().getPageFactor(),
								config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads());
					} catch (SQLException e) {
						throw new CityGMLExportException("Failed to initialize internal gml:id caches.", e);
					}	

					// create worker pools
					// here we have an open issue: queue sizes are fix...
					xlinkExporterPool = new WorkerPool<DBXlink>(
							"xlink_exporter_pool",
							1,
							Math.max(1, config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads() / 2),
							PoolSizeAdaptationStrategy.AGGRESSIVE,
							new DBExportXlinkWorkerFactory(dbPool, config, eventDispatcher),
							300,
							false);

					ioWriterPool = new SingleWorkerPool<SAXEventBuffer>(
							"citygml_writer_pool",
							new IOWriterWorkerFactory(saxWriter),
							100,
							false);

					dbWorkerPool = new WorkerPool<DBSplittingResult>(
							"db_exporter_pool",
							config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMinThreads(),
							config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads(),
							PoolSizeAdaptationStrategy.AGGRESSIVE,
							new DBExportWorkerFactory(
									dbPool,
									jaxbBuilder,
									new FeatureWriterFactory(ioWriterPool, jaxbBuilder, config),
									xlinkExporterPool,
									uidCacheManager,
									cacheTableManager,
									exportFilter,
									config,
									eventDispatcher),
							300,
							false);

					// prestart pool workers
					xlinkExporterPool.prestartCoreWorkers();
					ioWriterPool.prestartCoreWorkers();
					dbWorkerPool.prestartCoreWorkers();

					// fail if we could not start a single import worker
					if (dbWorkerPool.getPoolSize() == 0)
						throw new CityGMLExportException("Failed to start database export worker pool. Check the database connection pool settings.");

					// ok, preparations done. inform user...
					LOG.info("Exporting to file: " + file.getAbsolutePath());

					// write CityModel header element
					JAXBModelWriter writer = null;
					try {
						writer = new JAXBModelWriter(
								saxWriter, 
								(JAXBOutputFactory)jaxbBuilder.createCityGMLOutputFactory(moduleContext), 
								moduleContext, 
								new CityModelInfo());

						writer.writeStartDocument();
						saxWriter.flush();
					} catch (CityGMLWriteException | SAXException e) {
						throw new CityGMLExportException("Failed to write CityGML file.", e);
					}

					// get database splitter and start query
					dbSplitter = null;
					try {
						dbSplitter = new DBSplitter(
								dbPool,
								dbWorkerPool,
								exportFilter,
								uidCacheManager.getCache(CityGMLClass.ABSTRACT_CITY_OBJECT),
								cacheTableManager,
								eventDispatcher,
								config);

						if (shouldRun)
							dbSplitter.startQuery();
					} catch (SQLException e) {
						throw new CityGMLExportException("Failed to query the database.", e);
					}

					try {
						dbWorkerPool.shutdownAndWait();
						xlinkExporterPool.shutdownAndWait();
						ioWriterPool.shutdownAndWait();
					} catch (InterruptedException e) {
						throw new CityGMLExportException("Failed to shutdown worker pools.", e);
					}

					// write footer element and flush sax writer
					try {
						writer.writeEndDocument();						
						saxWriter.flush();
						saxWriter.getOutputWriter().close();
					} catch (CityGMLWriteException | SAXException | IOException e) {
						throw new CityGMLExportException("Failed to write CityGML file.", e);
					}

					eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("export.dialog.finish.msg"), this));
				} finally {
					// clean up
					if (xlinkExporterPool != null && !xlinkExporterPool.isTerminated())
						xlinkExporterPool.shutdownNow();

					if (dbWorkerPool != null && !dbWorkerPool.isTerminated())
						dbWorkerPool.shutdownNow();

					if (ioWriterPool != null && !ioWriterPool.isTerminated())
						ioWriterPool.shutdownNow();

					try {
						eventDispatcher.flushEvents();
					} catch (InterruptedException e) {
						//
					}

					try {
						uidCacheManager.shutdownAll();
					} catch (SQLException e) {
						throw new CityGMLExportException("Failed to clean gml:id caches.", e);
					}

					try {
						LOG.info("Cleaning temporary cache.");
						cacheTableManager.dropAll();
						cacheTableManager = null;
					} catch (SQLException e) {
						throw new CityGMLExportException("Failed to clean temporary cache.", e);
					}					
				}

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
			}
		}

		// show totally exported features
		if (useTiling && (rows > 1 || columns > 1)) {
			if (!totalFeatureCounterMap.isEmpty()) {
				LOG.info("Totally exported CityGML features:");
				for (CityGMLClass type : totalFeatureCounterMap.keySet())
					LOG.info(type + ": " + totalFeatureCounterMap.get(type));
			}

			long geometryObjects = 0;
			for (GMLClass type : totalGeometryCounterMap.keySet())
				geometryObjects += totalGeometryCounterMap.get(type);

			if (geometryObjects != 0)
				LOG.info("Total processed geometry objects: " + geometryObjects);
		}

		if (shouldRun)
			LOG.info("Total export time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");

		return shouldRun;
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.FEATURE_COUNTER) {
			HashMap<CityGMLClass, Long> counterMap = ((FeatureCounterEvent)e).getCounter();

			for (CityGMLClass type : counterMap.keySet()) {
				Long update = counterMap.get(type);

				Long counter = featureCounterMap.get(type);
				featureCounterMap.put(type, counter == null ? update : counter + update);

				if (useTiling) {
					counter = totalFeatureCounterMap.get(type);
					totalFeatureCounterMap.put(type, counter == null ? update : counter + update);
				}
			}
		}

		else if (e.getEventType() == EventType.GEOMETRY_COUNTER) {
			HashMap<GMLClass, Long> counterMap = ((GeometryCounterEvent)e).getCounter();

			for (GMLClass type : counterMap.keySet()) {
				Long update = counterMap.get(type);

				Long counter = geometryCounterMap.get(type);
				geometryCounterMap.put(type, counter == null ? update : counter + update);

				if (useTiling) {
					counter = totalGeometryCounterMap.get(type);
					totalGeometryCounterMap.put(type, counter == null ? update : counter + update);
				}
			}
		}

		else if (e.getEventType() == EventType.INTERRUPT) {
			if (isInterrupted.compareAndSet(false, true)) {
				shouldRun = false;
				InterruptEvent interruptEvent = (InterruptEvent)e;

				if (interruptEvent.getCause() != null) {
					Throwable cause = interruptEvent.getCause();

					if (cause instanceof SQLException) {
						Iterator<Throwable> iter = ((SQLException)cause).iterator();
						LOG.error("A SQL error occured: " + iter.next().getMessage().trim());
						while (iter.hasNext())
							LOG.error("Cause: " + iter.next().getMessage().trim());
					} else {
						LOG.error("An error occured: " + cause.getMessage().trim());
						while ((cause = cause.getCause()) != null)
							LOG.error("Cause: " + cause.getMessage().trim());
					}
				}

				String log = interruptEvent.getLogMessage();
				if (log != null)
					LOG.log(interruptEvent.getLogLevelType(), log);

				if (dbSplitter != null)
					dbSplitter.shutdown();

				if (dbWorkerPool != null)
					dbWorkerPool.drainWorkQueue();

				if (xlinkExporterPool != null)
					xlinkExporterPool.drainWorkQueue();
			}
		}
	}
}
