/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2022
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

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.common.IdColumnType;
import org.citydb.config.project.common.IdList;
import org.citydb.config.project.deleter.DeleteConfig;
import org.citydb.config.project.deleter.DeleteMode;
import org.citydb.config.project.global.CacheMode;
import org.citydb.config.project.importer.OverwriteMode;
import org.citydb.core.file.FileType;
import org.citydb.core.file.InputFile;
import org.citydb.core.file.input.AbstractArchiveInputFile;
import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.operation.common.cache.CacheTableManager;
import org.citydb.core.operation.common.cache.model.CacheTableModel;
import org.citydb.core.operation.common.csv.IdListException;
import org.citydb.core.operation.common.csv.IdListImporter;
import org.citydb.core.operation.common.csv.IdListParser;
import org.citydb.core.operation.deleter.DeleteException;
import org.citydb.core.operation.deleter.controller.Deleter;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.operation.importer.concurrent.DuplicateCheckerWorker;
import org.citydb.core.operation.importer.filter.CityGMLFilter;
import org.citydb.core.operation.importer.reader.FeatureReadException;
import org.citydb.core.operation.importer.reader.FeatureReader;
import org.citydb.core.operation.importer.reader.FeatureReaderFactory;
import org.citydb.core.operation.importer.reader.FeatureReaderFactoryBuilder;
import org.citydb.core.operation.importer.util.DuplicateLogger;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.util.concurrent.SingleWorkerPool;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citydb.util.event.global.GenericEvent;
import org.citydb.util.event.global.StatusDialogMessage;
import org.citydb.util.event.global.StatusDialogTitle;
import org.citydb.util.log.Logger;
import org.citygml4j.model.citygml.CityGML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

public class DuplicateController implements EventHandler {
    public static final String DELETE_DUPLICATES_COMPLETE = "import.deleteDuplicatesComplete";
    private final Logger log = Logger.getInstance();
    private final Object eventChannel;
    private final Config config;
    private final EventDispatcher eventDispatcher;

    private volatile boolean shouldRun = true;
    private DuplicateLogger duplicateLogger;

    public DuplicateController(Object eventChannel) {
        this.eventChannel = eventChannel;
        config = ObjectRegistry.getInstance().getConfig();
        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
    }

    public boolean doCheck(List<InputFile> inputFiles, CityGMLFilter filter) throws CityGMLImportException {
        eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

        boolean validate = config.getImportConfig().getCityGMLOptions().getXMLValidation().isSetUseXMLValidation();
        config.getImportConfig().getCityGMLOptions().getXMLValidation().setUseXMLValidation(false);

        CityGMLImportException exception = null;
        boolean hasDuplicates;
        try {
            hasDuplicates = findDuplicates(inputFiles, filter);
        } finally {
            eventDispatcher.removeEventHandler(this);
            config.getImportConfig().getCityGMLOptions().getXMLValidation().setUseXMLValidation(validate);
            filter.reset();

            if (duplicateLogger != null) {
                try {
                    duplicateLogger.close(shouldRun);
                } catch (IOException e) {
                    String message = "Failed to close the duplicate log. It is most likely corrupt.";
                    if (shouldRun) {
                        exception = new CityGMLImportException(message, e);
                    } else {
                        log.error(message, e);
                    }
                }
            }
        }

        if (exception != null) {
            throw exception;
        }

        return hasDuplicates;
    }

    private boolean findDuplicates(List<InputFile> inputFiles, CityGMLFilter filter) throws CityGMLImportException {
        log.info("Checking database for duplicate top-level features...");
        eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.duplicates.check")));

        try {
            duplicateLogger = new DuplicateLogger(config.getImportConfig().getDuplicateLog(),
                    config.getDatabaseConfig().getActiveConnection());
            if (!duplicateLogger.isTemporary()) {
                log.info("Log file of duplicate top-level features: " + duplicateLogger.getLogFilePath().toString());
            }
        } catch (IOException e) {
            throw new CityGMLImportException("Failed to create log file for duplicate top-level features.", e);
        }

        DuplicateCheckerWorker worker;
        try {
            worker = new DuplicateCheckerWorker(duplicateLogger, filter);
        } catch (SQLException e) {
            throw new CityGMLImportException("Failed to create duplicate worker.", e);
        }

        WorkerPool<CityGML> workerPool = null;
        try {
            FeatureReaderFactoryBuilder builder = new FeatureReaderFactoryBuilder();
            workerPool = new SingleWorkerPool<>("duplicate_checker_pool",
                    () -> worker,
                    config.getImportConfig().getResources().getThreadPool().getMaxThreads() * 2,
                    false);

            workerPool.setEventSource(eventChannel);

            for (int i = 0; shouldRun && i < inputFiles.size(); i++) {
                Path contentFile = null;
                try (InputFile inputFile = inputFiles.get(i)) {
                    contentFile = inputFile.getType() != FileType.ARCHIVE ?
                            inputFile.getFile() :
                            Paths.get(inputFile.getFile().toString(), ((AbstractArchiveInputFile) inputFile).getContentFile());

                    worker.setInputFile(contentFile);

                    FeatureReaderFactory factory = builder.buildFactory(inputFile, filter, Event.GLOBAL_CHANNEL, config);
                    try (FeatureReader reader = factory.createFeatureReader()) {
                        reader.read(inputFile, workerPool);
                    }
                } catch (FeatureReadException | IOException e) {
                    throw new CityGMLImportException("Failed to read input file '" + contentFile + "'.", e);
                }
            }

            try {
                workerPool.shutdownAndWait();
            } catch (InterruptedException e) {
                throw new CityGMLImportException("Failed to shutdown worker pools.", e);
            }
        } finally {
            if (workerPool != null && !workerPool.isTerminated()) {
                workerPool.shutdownNow();
            }
        }

        long duplicates = worker.getNumberOfDuplicates();
        if (shouldRun) {
            log.info(duplicates > 0 ?
                    "Found " + duplicates + " duplicate top-level features." :
                    "No duplicate top-level features found.");
        }

        return duplicates > 0;
    }

    public void doDelete() throws CityGMLImportException {
        if (duplicateLogger != null) {
            eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
            eventDispatcher.triggerEvent(new StatusDialogTitle(Language.I18N.getString("import.dialog.duplicates.process")));

            DeleteConfig currentConfig = config.getDeleteConfig();

            DeleteConfig deleteConfig = new DeleteConfig();
            deleteConfig.setUseDeleteList(true);
            deleteConfig.setDeleteList(duplicateLogger.toIdList(IdColumnType.DATABASE_ID));
            deleteConfig.setMode(config.getImportConfig().getMode().getOverwriteMode() == OverwriteMode.DELETE_EXISTING ?
                    DeleteMode.DELETE :
                    DeleteMode.TERMINATE);

            try {
                config.setDeleteConfig(deleteConfig);
                new Deleter().logTotalProcessingTime(false).doDelete(false);
            } catch (DeleteException e) {
                throw new CityGMLImportException("Failed to " +
                        (deleteConfig.getMode() == DeleteMode.DELETE ? "delete" : "terminate") +
                        " duplicate top-level features.", e);
            } finally {
                eventDispatcher.removeEventHandler(this);
                config.setDeleteConfig(currentConfig);

                if (shouldRun) {
                    eventDispatcher.triggerEvent(new GenericEvent(DELETE_DUPLICATES_COMPLETE));
                }

                if (duplicateLogger.isTemporary()) {
                    deleteDuplicateLog();
                }
            }
        }
    }

    public CacheTable createDuplicateList(CacheTableManager cacheTableManager) throws CityGMLImportException {
        CacheTable duplicateListCacheTable = null;
        if (duplicateLogger != null) {
            eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

            try {
                log.info("Loading list of duplicate top-level features into local cache...");

                try {
                    duplicateListCacheTable = cacheTableManager.createCacheTable(
                            CacheTableModel.DUPLICATE_LIST, CacheMode.LOCAL);
                } catch (SQLException e) {
                    throw new CityGMLImportException("Failed to create duplicate list cache.", e);
                }

                IdList duplicateList = duplicateLogger.toIdList(IdColumnType.RESOURCE_ID);
                int maxBatchSize = config.getDatabaseConfig().getImportBatching().getTempBatchSize();
                IdListImporter importer = new IdListImporter(duplicateListCacheTable, maxBatchSize);

                try {
                    for (String file : duplicateList.getFiles()) {
                        log.debug("Loading CSV file '" + file + "' into local cache.");
                        try (IdListParser parser = new IdListParser(file, duplicateList)) {
                            try {
                                importer.doImport(parser);
                            } catch (IdListException e) {
                                throw new CityGMLImportException("Failed to parse CSV file '" + file + "'.", e);
                            }
                        } catch (IdListException e) {
                            throw new CityGMLImportException("Failed to create duplicate list parser.", e);
                        }
                    }

                    duplicateListCacheTable.createIndexes();
                } catch (SQLException e) {
                    throw new CityGMLImportException("Failed to load duplicate list into cache.", e);
                }
            } finally {
                eventDispatcher.removeEventHandler(this);
                if (duplicateLogger.isTemporary()) {
                    deleteDuplicateLog();
                }
            }
        }

        return duplicateListCacheTable;
    }

    private void deleteDuplicateLog() {
        try {
            Files.deleteIfExists(duplicateLogger.getLogFilePath());
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        shouldRun = false;
    }
}
