/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.exporter.controller;

import org.citydb.citygml.common.database.cache.CacheTableManager;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.uid.UIDCacheType;
import org.citydb.citygml.common.database.xlink.DBXlink;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.concurrent.DBExportWorkerFactory;
import org.citydb.citygml.exporter.concurrent.DBExportXlinkWorkerFactory;
import org.citydb.citygml.exporter.database.content.DBSplitter;
import org.citydb.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.citygml.exporter.database.uid.FeatureGmlIdCache;
import org.citydb.citygml.exporter.database.uid.GeometryGmlIdCache;
import org.citydb.citygml.exporter.file.OutputFileFactory;
import org.citydb.citygml.exporter.writer.FeatureWriteException;
import org.citydb.citygml.exporter.writer.FeatureWriter;
import org.citydb.citygml.exporter.writer.FeatureWriterFactory;
import org.citydb.citygml.exporter.writer.FeatureWriterFactoryBuilder;
import org.citydb.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.internal.FileType;
import org.citydb.config.internal.OutputFile;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.exporter.SimpleTilingOptions;
import org.citydb.config.project.exporter.TileNameSuffixMode;
import org.citydb.config.project.exporter.TileSuffixMode;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.IndexStatusInfo.IndexType;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.CounterEvent;
import org.citydb.event.global.CounterType;
import org.citydb.event.global.EventType;
import org.citydb.event.global.GeometryCounterEvent;
import org.citydb.event.global.InterruptEvent;
import org.citydb.event.global.ObjectCounterEvent;
import org.citydb.event.global.StatusDialogMessage;
import org.citydb.event.global.StatusDialogProgressBar;
import org.citydb.event.global.StatusDialogTitle;
import org.citydb.log.Logger;
import org.citydb.plugin.PluginManager;
import org.citydb.plugin.extension.export.CityGMLExportExtension;
import org.citydb.plugin.extension.export.MetadataProvider;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.config.ConfigQueryBuilder;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.SelectionFilter;
import org.citydb.query.filter.selection.operator.logical.LogicalOperationFactory;
import org.citydb.query.filter.tiling.Tile;
import org.citydb.query.filter.tiling.Tiling;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.module.citygml.CityGMLModuleType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class Exporter implements EventHandler {
	private final Logger log = Logger.getInstance();

	private final CityGMLBuilder cityGMLBuilder;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final SchemaMapping schemaMapping;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private DBSplitter dbSplitter;

	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);

	private WorkerPool<DBSplittingResult> dbWorkerPool;
	private WorkerPool<DBXlink> xlinkExporterPool;
	private CacheTableManager cacheTableManager;
	private UIDCacheManager uidCacheManager;
	private boolean useTiling;

	private HashMap<Integer, Long> objectCounter;
	private EnumMap<GMLClass, Long> geometryCounter;
	private HashMap<Integer, Long> totalObjectCounter;
	private EnumMap<GMLClass, Long> totalGeometryCounter;	

	public Exporter(CityGMLBuilder cityGMLBuilder, 
			SchemaMapping schemaMapping, 
			Config config, 
			EventDispatcher eventDispatcher) {
		this.cityGMLBuilder = cityGMLBuilder;
		this.schemaMapping = schemaMapping;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		objectCounter = new HashMap<>();
		geometryCounter = new EnumMap<>(GMLClass.class);
		totalObjectCounter = new HashMap<>();
		totalGeometryCounter = new EnumMap<>(GMLClass.class);
	}

	public void cleanup() {
		eventDispatcher.removeEventHandler(this);
	}

	public boolean doProcess() throws CityGMLExportException {
		// adding listeners
		eventDispatcher.addEventHandler(EventType.OBJECT_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.GEOMETRY_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		// checking workspace
		Workspace workspace = config.getProject().getDatabase().getWorkspaces().getExportWorkspace();
		if (shouldRun && databaseAdapter.hasVersioningSupport() && 
				!databaseAdapter.getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getName()) &&
				!databaseAdapter.getWorkspaceManager().existsWorkspace(workspace, true))
			return false;

		// build query from filter settings
		Query query;
		try {
			ConfigQueryBuilder queryBuilder = new ConfigQueryBuilder(schemaMapping, databaseAdapter);
			if (config.getProject().getExporter().isUseSimpleQuery())
				query = queryBuilder.buildQuery(config.getProject().getExporter().getSimpleQuery(), config.getProject().getNamespaceFilter());
			else
				query = queryBuilder.buildQuery(config.getProject().getExporter().getQuery(), config.getProject().getNamespaceFilter());

		} catch (QueryBuildException e) {
			throw new CityGMLExportException("Failed to build the export query expression.", e);
		}

		// create feature writer factory
		FeatureWriterFactory writerFactory;
		try {
			writerFactory = FeatureWriterFactoryBuilder.buildFactory(query, schemaMapping, config);
		} catch (FeatureWriteException e) {
			throw new CityGMLExportException("Failed to build the feature writer factory.", e);
		}

		// get metadata provider
		MetadataProvider metadataProvider = null;
		if (config.getProject().getExporter().isSetMetadataProvider()) {
			for (CityGMLExportExtension plugin : PluginManager.getInstance().getExternalPlugins(CityGMLExportExtension.class)) {
				if (plugin instanceof MetadataProvider
						&& plugin.getClass().getCanonicalName().equals(config.getProject().getExporter().getMetadataProvider())) {
					metadataProvider = (MetadataProvider) plugin;
					break;
				}
			}

			if (metadataProvider == null)
				throw new CityGMLExportException("Failed to load metadata provider '" + config.getProject().getExporter().getMetadataProvider() + "'.");
		}

		// set target reference system for export
		DatabaseSrs targetSrs = query.getTargetSrs();
		config.getInternal().setTransformCoordinates(targetSrs.isSupported() &&
				targetSrs.getSrid() != databaseAdapter.getConnectionMetaData().getReferenceSystem().getSrid());

		if (config.getInternal().isTransformCoordinates()) {
			if (targetSrs.is3D() == databaseAdapter.getConnectionMetaData().getReferenceSystem().is3D()) {
				log.info("Transforming geometry representation to reference system '" + targetSrs.getDescription() + "' (SRID: " + targetSrs.getSrid() + ").");
				log.warn("Transformation is NOT applied to height reference system.");
			} else {
				throw new CityGMLExportException("Dimensionality of reference system for geometry transformation does not match.");
			}
		}

		// check and log index status
		try {
			if ((query.isSetTiling() || (query.isSetSelection() && query.getSelection().containsSpatialOperators()))
					&& !databaseAdapter.getUtil().isIndexEnabled("CITYOBJECT", "ENVELOPE")) {
				log.error("Spatial indexes are not activated.");
				log.error("Please use the database tab to activate the spatial indexes.");
				return false;
			}

			for (IndexType type : IndexType.values())
				databaseAdapter.getUtil().getIndexStatus(type).printStatusToConsole();
		} catch (SQLException e) {
			throw new CityGMLExportException("Database error while querying index status.", e);
		}

		// check whether database contains global appearances and set internal flag
		try {
			config.getInternal().setExportGlobalAppearances(config.getProject().getExporter().getAppearances().isSetExportAppearance() && 
					databaseAdapter.getUtil().containsGlobalAppearances(workspace));
		} catch (SQLException e) {
			throw new CityGMLExportException("Database error while querying the number of global appearances.", e);
		}

		// cache gml:ids of city objects in case we have to export groups
		config.getInternal().setRegisterGmlIdInCache(!config.getProject().getExporter().getCityObjectGroup().isExportMemberAsXLinks()
				&& query.getFeatureTypeFilter().containsFeatureType(schemaMapping.getFeatureType(query.getTargetVersion().getCityGMLModule(CityGMLModuleType.CITY_OBJECT_GROUP).getFeatureName(CityObjectGroup.class))));

		// tiling
		Tiling tiling = query.getTiling();
		SimpleTilingOptions tilingOptions = null;
		Predicate predicate = null;
		useTiling = query.isSetTiling();
		int rows = useTiling ? tiling.getRows() : 1;  
		int columns = useTiling ? tiling.getColumns() : 1;

		if (useTiling) {
			try {
				// transform tiling extent to database srs
				tiling.transformExtent(databaseAdapter.getConnectionMetaData().getReferenceSystem(), databaseAdapter);
				predicate = query.isSetSelection() ? query.getSelection().getPredicate() : null;
				tilingOptions = tiling.getTilingOptions() instanceof SimpleTilingOptions ? (SimpleTilingOptions)tiling.getTilingOptions() : new SimpleTilingOptions();
			} catch (FilterException e) {
				throw new CityGMLExportException("Failed to transform tiling extent.", e);
			}
		}

		// create output file factory
		OutputFileFactory fileFactory = new OutputFileFactory(config, eventDispatcher);
		Path exportFile = config.getInternal().getExportFile();
		if (exportFile.getFileName() == null)
			throw new CityGMLExportException("The export file '" + exportFile + "' is invalid.");

		// process export folder for texture files
		String textureFolder = null;
		boolean textureFolderIsAbsolute = false;
		boolean exportAppearance = config.getProject().getExporter().getAppearances().isSetExportAppearance();

		if (exportAppearance) {
			textureFolder = config.getProject().getExporter().getAppearances().getTexturePath().getPath();
			if (textureFolder == null || textureFolder.isEmpty())
				textureFolder = "appearance";

			textureFolderIsAbsolute = new File(textureFolder).isAbsolute();
			if (!textureFolderIsAbsolute)
				textureFolder = textureFolder.replace("\\", "/");

			if (textureFolderIsAbsolute) {
				try {
					Path path = Paths.get(textureFolder).toAbsolutePath().normalize();
					textureFolder = path.toString();
					if (!Files.isDirectory(path)) {
						Files.createDirectories(path);
						log.info("Created texture files folder '" + textureFolder + "'.");
					}
				} catch (IOException | InvalidPathException e) {
					throw new CityGMLExportException("Failed to create texture files folder '" + textureFolder + "'.", e);
				}
			}

			config.getInternal().setExportTextureURI(textureFolder);

			// check for unique texture filenames when exporting an archiv
			if (!config.getProject().getExporter().getAppearances().isSetUniqueTextureFileNames()
				&& fileFactory.getFileType(exportFile.getFileName()) == FileType.ARCHIVE) {
				log.warn("Using unique texture filenames because of writing to an archive file.");
				config.getProject().getExporter().getAppearances().setUniqueTextureFileNames(true);
			}
		}

		int remainingTiles = rows * columns;
		long start = System.currentTimeMillis();

		for (int i = 0; shouldRun && i < rows; i++) {
			for (int j = 0; shouldRun && j < columns; j++) {
				Path folder = exportFile.getParent();
				String fileName = exportFile.getFileName().toString();

				if (useTiling) {
					Tile tile;
					try {
						tile = tiling.getTileAt(i, j);
						tiling.setActiveTile(tile);

						Predicate bboxFilter = tile.getFilterPredicate(databaseAdapter);
						if (predicate != null)
							query.setSelection(new SelectionFilter(LogicalOperationFactory.AND(predicate, bboxFilter)));
						else
							query.setSelection(new SelectionFilter(bboxFilter));

					} catch (FilterException e) {
						throw new CityGMLExportException("Failed to get tile at [" + i + "," + j + "].", e);
					}

					// create suffix for folderName and fileName
					TileSuffixMode suffixMode = tilingOptions.getTilePathSuffix();
					String suffix;

					double minX = tile.getExtent().getLowerCorner().getX();
					double minY = tile.getExtent().getLowerCorner().getY();
					double maxX = tile.getExtent().getUpperCorner().getX();
					double maxY = tile.getExtent().getUpperCorner().getY();

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

					folder = folder.resolve(tilingOptions.getTilePath() + '_' + suffix);
					if (tilingOptions.getTileNameSuffix() == TileNameSuffixMode.SAME_AS_PATH) {
						int index = fileName.indexOf('.');
						fileName = index > 0 ?
								fileName.substring(0, index) + '_' + suffix + fileName.substring(index) :
								fileName + '_' + suffix;
					}
				}

				FeatureWriter writer = null;
				OutputFile file = null;
				try {
					eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("export.dialog.cityObj.msg"), this));
					eventDispatcher.triggerEvent(new StatusDialogTitle(fileName, this));
					eventDispatcher.triggerEvent(new CounterEvent(CounterType.REMAINING_TILES, --remainingTiles, this));

					try {
						file = fileFactory.createOutputFile(folder.resolve(fileName));
						config.getInternal().setCurrentExportFile(file);
					} catch (IOException e) {
						throw new CityGMLExportException("Failed to create output file '" + folder.resolve(fileName) + "'.", e);
					}

					// create relative folder for texture files
					if (exportAppearance && !textureFolderIsAbsolute &&
							(file.getType() == FileType.ARCHIVE || !Files.isDirectory(Paths.get(file.resolve(textureFolder))))) {
						try {
							file.createDirectories(textureFolder);
							log.info("Created texture files folder '" + textureFolder + "'.");
						} catch (IOException e) {
							throw new CityGMLExportException("Failed to create texture files folder '" + textureFolder + "'.", e);
						}
					}

					// create output writer
					try {
						writer = writerFactory.createFeatureWriter(new OutputStreamWriter(file.openStream(), StandardCharsets.UTF_8));
						writer.useIndentation(file.getType() == FileType.REGULAR);
					} catch (FeatureWriteException | IOException e) {
						throw new CityGMLExportException("Failed to open file '" + file.getFile() + "' for writing.", e);
					}

					// create instance of temp table manager
					try {
						cacheTableManager = new CacheTableManager(
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
								UIDCacheType.OBJECT,
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
					xlinkExporterPool = new WorkerPool<>(
							"xlink_exporter_pool",
							1,
							Math.max(1, config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads() / 2),
							PoolSizeAdaptationStrategy.AGGRESSIVE,
							new DBExportXlinkWorkerFactory(config, eventDispatcher),
							300,
							false);

					dbWorkerPool = new WorkerPool<>(
							"db_exporter_pool",
							config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMinThreads(),
							config.getProject().getExporter().getResources().getThreadPool().getDefaultPool().getMaxThreads(),
							PoolSizeAdaptationStrategy.AGGRESSIVE,
							new DBExportWorkerFactory(
									schemaMapping,
									cityGMLBuilder,
									writer,
									xlinkExporterPool,
									uidCacheManager,
									cacheTableManager,
									query,
									config,
									eventDispatcher),
							300,
							false);

					// prestart pool workers
					xlinkExporterPool.prestartCoreWorkers();
					dbWorkerPool.prestartCoreWorkers();

					// fail if we could not start a single import worker
					if (dbWorkerPool.getPoolSize() == 0)
						throw new CityGMLExportException("Failed to start database export worker pool. Check the database connection pool settings.");

					// ok, preparations done. inform user...
					log.info("Exporting to file: " + file.getFile());

					// get database splitter and start query
					try {
						dbSplitter = new DBSplitter(
								writer,
								schemaMapping,
								dbWorkerPool,
								query,
								uidCacheManager.getCache(UIDCacheType.OBJECT),
								cacheTableManager,
								eventDispatcher,
								config);

						if (shouldRun) {
							dbSplitter.setMetadataProvider(metadataProvider);
							dbSplitter.setCalculateNumberMatched(config.getInternal().isGUIMode());
							dbSplitter.startQuery();
						}
					} catch (SQLException | QueryBuildException | FilterException e) {
						throw new CityGMLExportException("Failed to query the database.", e);
					} catch (FeatureWriteException e) {
						throw new CityGMLExportException("Failed to write to output file.", e);
					}

					try {
						dbWorkerPool.shutdownAndWait();
						xlinkExporterPool.shutdownAndWait();
					} catch (InterruptedException e) {
						throw new CityGMLExportException("Failed to shutdown worker pools.", e);
					}

					eventDispatcher.triggerEvent(new StatusDialogProgressBar(true, this));
					eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("export.dialog.finish.msg"), this));
				} catch (CityGMLExportException e) {
					throw e;
				} catch (Throwable e) {
					throw new CityGMLExportException("An unexpected error occurred.", e);
				} finally {
					// close writer before closing output file
					if (writer != null) {
						try {
							writer.close();
						} catch (FeatureWriteException e) {
							log.error("Failed to close output writer: " + e.getMessage());
							shouldRun = false;
						}
					}

					if (file != null) {
						try {
							file.close();
						} catch (IOException e) {
							log.error("Failed to close output file: " + e.getMessage());
							shouldRun = false;
						}
					}
					
					// clean up
					if (xlinkExporterPool != null && !xlinkExporterPool.isTerminated())
						xlinkExporterPool.shutdownNow();

					if (dbWorkerPool != null && !dbWorkerPool.isTerminated())
						dbWorkerPool.shutdownNow();

					try {
						eventDispatcher.flushEvents();
					} catch (InterruptedException e) {
						//
					}

					if (uidCacheManager != null) {
						try {
							uidCacheManager.shutdownAll();
						} catch (SQLException e) {
							log.error("Failed to clean gml:id caches: " + e.getMessage());
							shouldRun = false;
						}
					}

					if (cacheTableManager != null) {
						try {
							log.info("Cleaning temporary cache.");
							cacheTableManager.dropAll();
							cacheTableManager = null;
						} catch (SQLException e) {
							log.error("Failed to clean temporary cache: " + e.getMessage());
							shouldRun = false;
						}					
					}
				}

				// show exported features
				if (!objectCounter.isEmpty()) {
					log.info("Exported city objects:");
					Map<String, Long> typeNames = Util.mapObjectCounter(objectCounter, schemaMapping);					
					typeNames.keySet().stream().sorted().forEach(object -> log.info(object + ": " + typeNames.get(object)));			
				}

				// show processed geometries
				if (!geometryCounter.isEmpty())
					log.info("Processed geometry objects: " + geometryCounter.values().stream().reduce(0L, Long::sum));

				objectCounter.clear();
				geometryCounter.clear();
			}
		}

		// show totally exported features
		if (useTiling && (rows > 1 || columns > 1)) {
			if (!totalObjectCounter.isEmpty()) {
				log.info("Total exported CityGML features:");
				Map<String, Long> typeNames = Util.mapObjectCounter(totalObjectCounter, schemaMapping);
				typeNames.keySet().forEach(object -> log.info(object + ": " + typeNames.get(object)));	
			}

			if (!totalGeometryCounter.isEmpty())
				log.info("Total processed objects: " + totalGeometryCounter.values().stream().reduce(0L, Long::sum));
		}

		if (shouldRun)
			log.info("Total export time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");

		return shouldRun;
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.OBJECT_COUNTER) {
			Map<Integer, Long> counter = ((ObjectCounterEvent)e).getCounter();

			for (Entry<Integer, Long> entry : counter.entrySet()) {
				Long tmp = objectCounter.get(entry.getKey());
				objectCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());

				if (useTiling) {
					tmp = totalObjectCounter.get(entry.getKey());
					totalObjectCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());
				}
			}
		}

		else if (e.getEventType() == EventType.GEOMETRY_COUNTER) {
			Map<GMLClass, Long> counter = ((GeometryCounterEvent)e).getCounter();

			for (Entry<GMLClass, Long> entry : counter.entrySet()) {
				Long tmp = geometryCounter.get(entry.getKey());
				geometryCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());

				if (useTiling) {
					tmp = totalGeometryCounter.get(entry.getKey());
					totalGeometryCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());
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
						log.error("A SQL error occurred: " + iter.next().getMessage());
						while (iter.hasNext())
							log.error("Cause: " + iter.next().getMessage());
					} else {
						log.error("An error occurred: " + cause.getMessage());
						while ((cause = cause.getCause()) != null)
							log.error(cause.getClass().getTypeName() + ": " + cause.getMessage());
					}
				}

				String msg = interruptEvent.getLogMessage();
				if (msg != null)
					log.log(interruptEvent.getLogLevelType(), msg);

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
