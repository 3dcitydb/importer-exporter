/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.operation.importer.controller;

import org.apache.tika.exception.TikaException;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.global.CacheMode;
import org.citydb.config.project.importer.ImportList;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.adapter.AbstractUtilAdapter;
import org.citydb.core.database.adapter.IndexStatusInfo;
import org.citydb.core.database.adapter.IndexStatusInfo.IndexInfoObject;
import org.citydb.core.database.adapter.IndexStatusInfo.IndexStatus;
import org.citydb.core.database.adapter.IndexStatusInfo.IndexType;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.file.FileType;
import org.citydb.core.file.InputFile;
import org.citydb.core.file.input.AbstractArchiveInputFile;
import org.citydb.core.file.input.DirectoryScanner;
import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.operation.common.cache.CacheTableManager;
import org.citydb.core.operation.common.cache.IdCacheManager;
import org.citydb.core.operation.common.cache.IdCacheType;
import org.citydb.core.operation.common.cache.model.CacheTableModel;
import org.citydb.core.operation.common.csv.IdListException;
import org.citydb.core.operation.common.csv.IdListImporter;
import org.citydb.core.operation.common.csv.IdListParser;
import org.citydb.core.operation.common.util.AffineTransformer;
import org.citydb.core.operation.common.xlink.DBXlink;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.operation.importer.cache.GeometryGmlIdCache;
import org.citydb.core.operation.importer.cache.ObjectGmlIdCache;
import org.citydb.core.operation.importer.cache.TextureImageCache;
import org.citydb.core.operation.importer.concurrent.DBImportWorkerFactory;
import org.citydb.core.operation.importer.concurrent.DBImportXlinkResolverWorkerFactory;
import org.citydb.core.operation.importer.concurrent.DBImportXlinkWorkerFactory;
import org.citydb.core.operation.importer.database.xlink.resolver.DBXlinkSplitter;
import org.citydb.core.operation.importer.filter.CityGMLFilter;
import org.citydb.core.operation.importer.filter.CityGMLFilterBuilder;
import org.citydb.core.operation.importer.reader.FeatureReadException;
import org.citydb.core.operation.importer.reader.FeatureReader;
import org.citydb.core.operation.importer.reader.FeatureReaderFactory;
import org.citydb.core.operation.importer.reader.FeatureReaderFactoryBuilder;
import org.citydb.core.operation.importer.util.ImportLogger;
import org.citydb.core.operation.importer.util.InternalConfig;
import org.citydb.core.plugin.PluginException;
import org.citydb.core.plugin.PluginManager;
import org.citydb.core.plugin.extension.importer.FeatureImportExtension;
import org.citydb.core.plugin.extension.importer.ImportStatus;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.Util;
import org.citydb.util.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.*;
import org.citydb.util.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.gml.GMLClass;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class Importer implements EventHandler {
    private final Logger log = Logger.getInstance();
    private final CityGMLBuilder cityGMLBuilder;
    private final AbstractDatabaseAdapter databaseAdapter;
    private final SchemaMapping schemaMapping;
    private final PluginManager pluginManager;
    private final Config config;
    private final EventDispatcher eventDispatcher;

    private final AtomicBoolean isInterrupted = new AtomicBoolean(false);
    private final HashMap<Integer, Long> objectCounter;
    private final EnumMap<GMLClass, Long> geometryCounter;

    private volatile boolean shouldRun = true;
    private CityGMLImportException exception;
    private DirectoryScanner directoryScanner;
    private ImportLogger importLogger;
    private CacheTableManager cacheTableManager;
    private CacheTable importListCacheTable;

    public Importer() {
        cityGMLBuilder = ObjectRegistry.getInstance().getCityGMLBuilder();
        schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
        pluginManager = PluginManager.getInstance();
        config = ObjectRegistry.getInstance().getConfig();
        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
        databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();

        objectCounter = new HashMap<>();
        geometryCounter = new EnumMap<>(GMLClass.class);
    }

    public boolean doImport(List<Path> inputFiles) throws CityGMLImportException {
        if (inputFiles == null || inputFiles.isEmpty()) {
            throw new CityGMLImportException("No input file(s) provided.");
        }

        eventDispatcher.addEventHandler(EventType.OBJECT_COUNTER, this);
        eventDispatcher.addEventHandler(EventType.GEOMETRY_COUNTER, this);
        eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

        boolean success = true;
        try {
            success = process(inputFiles);
        } catch (CityGMLImportException e) {
            success = false;
            throw e;
        } catch (Throwable e) {
            success = false;
            throw new CityGMLImportException("An unexpected error occurred.", e);
        } finally {
            eventDispatcher.removeEventHandler(this);

            // shutdown import plugins
            for (FeatureImportExtension plugin : pluginManager.getEnabledExternalPlugins(FeatureImportExtension.class)) {
                plugin.afterImport(success ? ImportStatus.SUCCESS : ImportStatus.ABORTED);
            }

            if (importLogger != null) {
                try {
                    log.debug("Closing import log.");
                    importLogger.close(shouldRun);
                } catch (IOException e) {
                    log.error("Failed to close the feature import log. It is most likely corrupt.", e);
                }
            }

            if (cacheTableManager != null) {
                try {
                    log.debug("Closing temporary cache.");
                    cacheTableManager.close();
                } catch (SQLException e) {
                    log.error("Failed to close the temporary cache.", e);
                }
            }
        }

        return success;
    }

    private boolean process(List<Path> inputFiles) throws CityGMLImportException {
        // worker pool settings
        int minThreads = config.getImportConfig().getResources().getThreadPool().getMinThreads();
        int maxThreads = config.getImportConfig().getResources().getThreadPool().getMaxThreads();
        int queueSize = maxThreads * 2;

        // gml:id lookup cache update
        int lookupCacheBatchSize = config.getDatabaseConfig().getImportBatching().getGmlIdCacheBatchSize();

        // log workspace
        if (databaseAdapter.hasVersioningSupport() && databaseAdapter.getConnectionDetails().isSetWorkspace()) {
            Workspace workspace = databaseAdapter.getConnectionDetails().getWorkspace();
            if (!databaseAdapter.getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getName())) {
                log.info("Importing into workspace " + databaseAdapter.getConnectionDetails().getWorkspace() + ".");
            }
        }

        // deactivate database indexes
        if (shouldRun && (config.getImportConfig().getIndexes().isSpatialIndexModeDeactivate()
                || config.getImportConfig().getIndexes().isSpatialIndexModeDeactivateActivate()
                || config.getImportConfig().getIndexes().isNormalIndexModeDeactivate()
                || config.getImportConfig().getIndexes().isNormalIndexModeDeactivateActivate())) {
            try {
                if (shouldRun && (config.getImportConfig().getIndexes().isSpatialIndexModeDeactivate()
                        || config.getImportConfig().getIndexes().isSpatialIndexModeDeactivateActivate())) {
                    manageIndexes(false, true);
                } else {
                    databaseAdapter.getUtil().getIndexStatus(IndexType.SPATIAL).printStatusToConsole();
                }

                if (shouldRun && (config.getImportConfig().getIndexes().isNormalIndexModeDeactivate()
                        || config.getImportConfig().getIndexes().isNormalIndexModeDeactivateActivate())) {
                    manageIndexes(false, false);
                } else {
                    databaseAdapter.getUtil().getIndexStatus(IndexType.NORMAL).printStatusToConsole();
                }
            } catch (SQLException e) {
                throw new CityGMLImportException("Failed to deactivate indexes.", e);
            }
        } else {
            try {
                for (IndexType type : IndexType.values()) {
                    databaseAdapter.getUtil().getIndexStatus(type).printStatusToConsole();
                }
            } catch (SQLException e) {
                throw new CityGMLImportException("Failed to query index status.", e);
            }
        }

        // create import logger
        if (config.getImportConfig().getImportLog().isSetLogImportedFeatures()) {
            try {
                importLogger = new ImportLogger(
                        config.getImportConfig().getImportLog(),
                        config.getDatabaseConfig().getActiveConnection());
                log.info("Log file of imported top-level features: " + importLogger.getLogFilePath().toString());
            } catch (IOException e) {
                throw new CityGMLImportException("Failed to create log file for imported top-level features.", e);
            }
        }

        // initialize import plugins
        for (FeatureImportExtension plugin : pluginManager.getEnabledExternalPlugins(FeatureImportExtension.class)) {
            try {
                plugin.beforeImport();
            } catch (PluginException e) {
                throw new CityGMLImportException("Failed to initialize import plugin " + plugin.getClass().getName() + ".", e);
            }
        }

        // build list of import files
        List<InputFile> files;
        try {
            log.info("Creating list of files to be imported...");
            directoryScanner = new DirectoryScanner(true);
            files = directoryScanner.listFiles(inputFiles);
            if (files.isEmpty()) {
                throw new CityGMLImportException("Failed to find files at the specified locations.");
            }
        } catch (TikaException | IOException e) {
            throw new CityGMLImportException("Fatal error while searching for files.", e);
        }

        if (!shouldRun)
            return false;

        int fileCounter = 0;
        int remainingFiles = files.size();
        log.info("List of import files successfully created.");
        log.info(remainingFiles + " file(s) will be imported.");

        // affine transformation
        AffineTransformer affineTransformer = null;
        if (config.getImportConfig().getAffineTransformation().isEnabled()) {
            try {
                log.info("Applying affine coordinates transformation.");
                affineTransformer = new AffineTransformer(config.getImportConfig().getAffineTransformation());
            } catch (Exception e) {
                throw new CityGMLImportException("Failed to create affine transformer.", e);
            }
        }

        // create instance of the cache table manager
        try {
            cacheTableManager = new CacheTableManager(config.getGlobalConfig().getCache());
        } catch (SQLException | IOException e) {
            throw new CityGMLImportException("Failed to initialize internal cache manager.", e);
        }

        // load import list into local cache
        importListCacheTable = null;
        if (config.getImportConfig().getFilter().isUseImportListFilter()
                && config.getImportConfig().getFilter().isSetImportList()
                && config.getImportConfig().getFilter().getImportList().hasFiles()) {
            log.info("Loading import list into local cache...");

            // create local cache manager
            try {
                importListCacheTable = cacheTableManager.createCacheTable(CacheTableModel.ID_LIST, CacheMode.LOCAL);
            } catch (SQLException e) {
                throw new CityGMLImportException("Failed to create import list cache.", e);
            }

            ImportList importList = config.getImportConfig().getFilter().getImportList();
            int maxBatchSize = config.getDatabaseConfig().getImportBatching().getTempBatchSize();
            IdListImporter importer = new IdListImporter(importListCacheTable, maxBatchSize);

            try {
                for (String file : importList.getFiles()) {
                    log.debug("Loading CSV file '" + file + "' into local cache.");
                    try (IdListParser parser = new IdListParser(file, importList)) {
                        try {
                            importer.doImport(parser);
                        } catch (IdListException e) {
                            throw new CityGMLImportException("Failed to parse CSV file '" + file + "'.", e);
                        }
                    } catch (IdListException e) {
                        throw new CityGMLImportException("Failed to create import list parser.", e);
                    }
                }

                importListCacheTable.createIndexes();
            } catch (SQLException e) {
                throw new CityGMLImportException("Failed to load import list into cache.", e);
            }
        }

        // build CityGML filter
        CityGMLFilter filter;
        try {
            CityGMLFilterBuilder builder = new CityGMLFilterBuilder(schemaMapping, databaseAdapter);
            filter = builder.buildCityGMLFilter(config.getImportConfig().getFilter(), importListCacheTable);
        } catch (FilterException e) {
            throw new CityGMLImportException("Failed to build the import filter.", e);
        }

        // create reader factory builder
        FeatureReaderFactoryBuilder builder = new FeatureReaderFactoryBuilder();

        IdCacheManager idCacheManager = null;
        WorkerPool<CityGML> dbWorkerPool = null;
        WorkerPool<DBXlink> tmpXlinkPool = null;
        WorkerPool<DBXlink> xlinkResolverPool = null;
        DBXlinkSplitter splitter;

        long start = System.currentTimeMillis();

        while (shouldRun && fileCounter < files.size()) {
            // check whether we reached the counter limit
            if (filter.isSetCounterFilter() && !filter.getCounterFilter().isCountSatisfied()) {
            	break;
			}

            InternalConfig internalConfig = new InternalConfig();

            try (InputFile file = files.get(fileCounter++)) {
                internalConfig.setInputFile(file);
                Path contentFile = file.getType() != FileType.ARCHIVE ?
                        file.getFile() :
						Paths.get(file.getFile().toString(), ((AbstractArchiveInputFile) file).getContentFile());

                eventDispatcher.triggerEvent(new StatusDialogTitle(contentFile.getFileName().toString()));
                eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.cityObj.msg")));
                eventDispatcher.triggerEvent(new StatusDialogProgressBar(true));
                eventDispatcher.triggerEvent(new CounterEvent(CounterType.FILE, --remainingFiles));

                // update import log
                if (importLogger != null) {
                    importLogger.setInputFile(contentFile);
                }

                // set metadata
                internalConfig.setMetadata(config.getImportConfig().getContinuation());

                // set gml:id codespace starting from version 3.1
                if (databaseAdapter.getConnectionMetaData().getCityDBVersion().compareTo(3, 1, 0) >= 0) {
                    if (config.getImportConfig().getResourceId().isSetNoneCodeSpaceMode()) {
                    	internalConfig.setCurrentGmlIdCodespace(null);
					} else if (config.getImportConfig().getResourceId().isSetRelativeCodeSpaceMode()) {
                    	internalConfig.setCurrentGmlIdCodespace(file.getFile().getFileName().toString());
					} else if (config.getImportConfig().getResourceId().isSetAbsoluteCodeSpaceMode()) {
                    	internalConfig.setCurrentGmlIdCodespace(file.getFile().toString());
					} else if (config.getImportConfig().getResourceId().isSetUserCodeSpaceMode()) {
                        String codespace = config.getImportConfig().getResourceId().getCodeSpace();
                        if (codespace != null && !codespace.isEmpty()) {
                        	internalConfig.setCurrentGmlIdCodespace(codespace);
						}
                    }
                }

                // create instance of gml:id lookup server manager...
                idCacheManager = new IdCacheManager();

                // ...and start servers
                try {
                    idCacheManager.initCache(
                            IdCacheType.GEOMETRY,
                            new GeometryGmlIdCache(cacheTableManager,
                                    config.getImportConfig().getResources().getIdCache().getGeometry().getPartitions(),
                                    lookupCacheBatchSize),
                            config.getImportConfig().getResources().getIdCache().getGeometry().getCacheSize(),
                            config.getImportConfig().getResources().getIdCache().getGeometry().getPageFactor(),
                            maxThreads);

                    idCacheManager.initCache(
                            IdCacheType.OBJECT,
                            new ObjectGmlIdCache(cacheTableManager,
                                    config.getImportConfig().getResources().getIdCache().getFeature().getPartitions(),
                                    lookupCacheBatchSize),
                            config.getImportConfig().getResources().getIdCache().getFeature().getCacheSize(),
                            config.getImportConfig().getResources().getIdCache().getFeature().getPageFactor(),
                            maxThreads);

                    if (config.getImportConfig().getAppearances().isSetImportAppearance() &&
                            config.getImportConfig().getAppearances().isSetImportTextureFiles()) {
                        idCacheManager.initCache(
                                IdCacheType.TEXTURE_IMAGE,
                                new TextureImageCache(cacheTableManager,
                                        config.getImportConfig().getResources().getTexImageCache().getPartitions(),
                                        lookupCacheBatchSize),
                                config.getImportConfig().getResources().getTexImageCache().getCacheSize(),
                                config.getImportConfig().getResources().getTexImageCache().getPageFactor(),
                                maxThreads);
                    }
                } catch (SQLException e) {
                    throw new CityGMLImportException("Failed to initialize internal gml:id caches.", e);
                }

                // creating worker pools needed for data import
                // this pool is for registering xlinks
                tmpXlinkPool = new WorkerPool<>(
                        "xlink_importer_pool",
                        minThreads,
                        maxThreads,
                        PoolSizeAdaptationStrategy.AGGRESSIVE,
                        new DBImportXlinkWorkerFactory(cacheTableManager, config, eventDispatcher),
                        queueSize,
                        false);

                // this pool basically works on the data import
                dbWorkerPool = new WorkerPool<>(
                        "db_importer_pool",
                        minThreads,
                        maxThreads,
                        PoolSizeAdaptationStrategy.AGGRESSIVE,
                        new DBImportWorkerFactory(schemaMapping,
                                cityGMLBuilder,
                                tmpXlinkPool,
                                idCacheManager,
                                filter,
                                affineTransformer,
                                importLogger,
                                internalConfig,
                                config,
                                eventDispatcher),
                        queueSize,
                        false);

                // prestart threads
                tmpXlinkPool.prestartCoreWorkers();
                dbWorkerPool.prestartCoreWorkers();

                // fail if we could not start a single import worker
                if (dbWorkerPool.getPoolSize() == 0) {
                    throw new CityGMLImportException("Failed to start database import worker pool. Check the database connection pool settings.");
                }

                FeatureReaderFactory factory;
                try {
                    factory = builder.buildFactory(file, filter, config);
                } catch (FeatureReadException e) {
                    throw new CityGMLImportException("Failed to read input file '" + contentFile + "'.", e);
                }

                log.info("Importing file: " + contentFile);

                try (FeatureReader reader = factory.createFeatureReader()) {
                    reader.read(file, dbWorkerPool);

                    // show XML validation errors
                    if (reader.getValidationErrors() > 0) {
                    	log.warn(reader.getValidationErrors() + " error(s) encountered while validating the document.");
					}
                } catch (FeatureReadException e) {
                    throw new CityGMLImportException("Failed to read input file.", e);
                }

                // we are done with parsing the file. so shutdown the workers.
                // the xlink pool is not shutdown because we need it afterwards
                try {
                    dbWorkerPool.shutdownAndWait();
                    tmpXlinkPool.join();
                } catch (InterruptedException e) {
                    throw new CityGMLImportException("Failed to shutdown worker pools.", e);
                }

                if (shouldRun) {
                    // get an xlink resolver pool
                    log.info("Resolving XLink references.");
                    xlinkResolverPool = new WorkerPool<>(
                            "xlink_resolver_pool",
                            minThreads,
                            maxThreads,
                            PoolSizeAdaptationStrategy.AGGRESSIVE,
                            new DBImportXlinkResolverWorkerFactory(file,
                                    tmpXlinkPool,
                                    idCacheManager,
                                    cacheTableManager,
                                    config,
                                    eventDispatcher),
                            queueSize,
                            false);

                    // prestart its workers
                    xlinkResolverPool.prestartCoreWorkers();

                    // resolve xlinks based on temp tables
                    if (shouldRun) {
                        splitter = new DBXlinkSplitter(cacheTableManager,
                                xlinkResolverPool,
                                tmpXlinkPool,
                                Event.GLOBAL_CHANNEL,
                                eventDispatcher);

                        splitter.startQuery();
                    }

                    // shutdown worker pools
                    try {
                        xlinkResolverPool.shutdownAndWait();
                    } catch (InterruptedException e) {
                        throw new CityGMLImportException("Failed to shutdown worker pools.", e);
                    }
                }

                // shutdown tmp xlink pool
                try {
                    tmpXlinkPool.shutdownAndWait();
                } catch (InterruptedException e) {
                    throw new CityGMLImportException("Failed to shutdown worker pools.", e);
                }

                eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.finish.msg")));
                eventDispatcher.triggerEvent(new StatusDialogProgressBar(true));
            } catch (IOException e) {
                throw new CityGMLImportException("Failed to process import file.", e);
            } finally {
                if (dbWorkerPool != null && !dbWorkerPool.isTerminated()) {
                	dbWorkerPool.shutdownNow();
				}

                if (xlinkResolverPool != null && !xlinkResolverPool.isTerminated()) {
                	xlinkResolverPool.shutdownNow();
				}

                if (tmpXlinkPool != null && !tmpXlinkPool.isTerminated()) {
                	tmpXlinkPool.shutdownNow();
				}

                try {
                    eventDispatcher.flushEvents();
                } catch (InterruptedException e) {
                    //
                }

                if (idCacheManager != null) {
                    try {
                        idCacheManager.shutdownAll();
                    } catch (SQLException e) {
                        setException("Failed to clean the gml:id caches.", e);
                        shouldRun = false;
                    }
                }

                if (cacheTableManager != null) {
                    try {
                        log.info("Cleaning temporary cache.");
                        cacheTableManager.dropIf(table -> table != importListCacheTable);
                    } catch (SQLException e) {
                        setException("Failed to clean the temporary cache.", e);
                        shouldRun = false;
                    }
                }
            }
        }

        // reactivate database indexes
        if (shouldRun) {
            if (config.getImportConfig().getIndexes().isSpatialIndexModeDeactivateActivate()
                    || config.getImportConfig().getIndexes().isNormalIndexModeDeactivateActivate()) {
                try {
                    if (config.getImportConfig().getIndexes().isSpatialIndexModeDeactivateActivate()) {
                    	manageIndexes(true, true);
					}

                    if (config.getImportConfig().getIndexes().isNormalIndexModeDeactivateActivate()) {
                    	manageIndexes(true, false);
					}
                } catch (SQLException e) {
                    log.warn("Failed to activate indexes.", e);
                }
            }
        }

        // show imported features
        if (!objectCounter.isEmpty()) {
            log.info("Imported city objects:");
            Map<String, Long> typeNames = Util.mapObjectCounter(objectCounter, schemaMapping);
            typeNames.keySet().forEach(object -> log.info(object + ": " + typeNames.get(object)));
        }

        // show processed geometries
        if (!geometryCounter.isEmpty()) {
            log.info("Processed geometry objects: " + geometryCounter.values().stream().reduce(0L, Long::sum));
        }

        if (shouldRun) {
            log.info("Total import time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");
        } else if (exception != null) {
            throw exception;
        }

        return shouldRun;
    }

    private void manageIndexes(boolean enable, boolean workOnSpatialIndexes) throws SQLException {
        AbstractUtilAdapter utilAdapter = databaseAdapter.getUtil();
        log.info((enable ? "Activating " : "Deactivating ") + (workOnSpatialIndexes ? "spatial" : "normal") + " indexes...");

        IndexStatusInfo indexStatus;
        if (enable) {
            indexStatus = workOnSpatialIndexes ? utilAdapter.createSpatialIndexes() : utilAdapter.createNormalIndexes();
        } else {
            indexStatus = workOnSpatialIndexes ? utilAdapter.dropSpatialIndexes() : utilAdapter.dropNormalIndexes();
        }

        if (indexStatus != null) {
            IndexStatus expectedStatus = enable ? IndexStatus.VALID : IndexStatus.DROPPED;
            for (IndexInfoObject indexObj : indexStatus.getIndexObjects()) {
                if (indexObj.getStatus() != expectedStatus) {
                    log.error("FAILED: " + indexObj);
                    if (indexObj.hasErrorMessage())
                        log.error("Error cause: " + indexObj.getErrorMessage());
                }
            }
        }
    }

	private void setException(String message, Throwable cause) {
		if (exception == null) {
			exception = new CityGMLImportException(message, cause);
		}
	}

    @Override
    public void handleEvent(Event e) throws Exception {
        if (e.getEventType() == EventType.OBJECT_COUNTER) {
            Map<Integer, Long> counter = ((ObjectCounterEvent) e).getCounter();
            for (Entry<Integer, Long> entry : counter.entrySet()) {
                Long tmp = objectCounter.get(entry.getKey());
                objectCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());
            }
        } else if (e.getEventType() == EventType.GEOMETRY_COUNTER) {
            Map<GMLClass, Long> counter = ((GeometryCounterEvent) e).getCounter();
            for (Entry<GMLClass, Long> entry : counter.entrySet()) {
                Long tmp = geometryCounter.get(entry.getKey());
                geometryCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());
            }
        } else if (e.getEventType() == EventType.INTERRUPT) {
            if (isInterrupted.compareAndSet(false, true)) {
                shouldRun = false;
                InterruptEvent event = (InterruptEvent) e;

                log.log(event.getLogLevelType(), event.getLogMessage());
                if (event.getCause() != null) {
                    setException("Aborting import due to errors.", event.getCause());
                }

                if (directoryScanner != null) {
                    directoryScanner.cancel();
                }
            }
        }
    }
}
