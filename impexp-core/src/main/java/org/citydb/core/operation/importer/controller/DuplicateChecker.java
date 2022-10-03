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
import org.citydb.config.project.deleter.DeleteConfig;
import org.citydb.config.project.deleter.DeleteMode;
import org.citydb.core.file.FileType;
import org.citydb.core.file.InputFile;
import org.citydb.core.file.input.AbstractArchiveInputFile;
import org.citydb.core.operation.importer.CityGMLImportException;
import org.citydb.core.operation.importer.concurrent.DuplicateWorker;
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
import org.citydb.util.log.Logger;
import org.citygml4j.model.citygml.CityGML;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

public class DuplicateChecker implements EventHandler {
    private final Logger log = Logger.getInstance();
    private final Config config;
    private final EventDispatcher eventDispatcher;

    private volatile boolean shouldRun = true;
    private DuplicateLogger duplicateLogger;

    public DuplicateChecker() {
        config = ObjectRegistry.getInstance().getConfig();
        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
        eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
    }

    public long doCheck(List<InputFile> inputFiles, CityGMLFilter filter) throws CityGMLImportException {
        boolean validate = config.getImportConfig().getCityGMLOptions().getXMLValidation().isSetUseXMLValidation();
        config.getImportConfig().getCityGMLOptions().getXMLValidation().setUseXMLValidation(false);

        try {
            return process(inputFiles, filter);
        } finally {
            eventDispatcher.removeEventHandler(this);
            config.getImportConfig().getCityGMLOptions().getXMLValidation().setUseXMLValidation(validate);
            filter.reset();

            if (duplicateLogger != null) {
                try {
                    duplicateLogger.close(shouldRun);
                } catch (IOException e) {
                    log.error("Failed to close the duplicate log. It is most likely corrupt.", e);
                }
            }
        }
    }

    public DeleteConfig generateDeleteConfig() {
        if (duplicateLogger != null) {
            DeleteConfig deleteConfig = new DeleteConfig();
            deleteConfig.setUseDeleteList(true);
            deleteConfig.setDeleteList(duplicateLogger.toIdList());
            deleteConfig.setMode(DeleteMode.DELETE);
            return deleteConfig;
        } else {
            return null;
        }
    }

    private long process(List<InputFile> inputFiles, CityGMLFilter filter) throws CityGMLImportException {
        try {
            duplicateLogger = new DuplicateLogger(config.getDatabaseConfig().getActiveConnection());
            if (!duplicateLogger.isTemporary()) {
                log.info("Log file of duplicate top-level features: " + duplicateLogger.getLogFilePath().toString());
            }
        } catch (IOException e) {
            throw new CityGMLImportException("Failed to create log file for duplicate top-level features.", e);
        }

        DuplicateWorker worker;
        try {
            worker = new DuplicateWorker(duplicateLogger, filter);
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

            for (int i = 0; shouldRun && i < inputFiles.size(); i++) {
                Path contentFile = null;
                try (InputFile inputFile = inputFiles.get(i)) {
                    contentFile = inputFile.getType() != FileType.ARCHIVE ?
                            inputFile.getFile() :
                            Paths.get(inputFile.getFile().toString(), ((AbstractArchiveInputFile) inputFile).getContentFile());

                    worker.setInputFile(contentFile);

                    FeatureReaderFactory factory = builder.buildFactory(inputFile, filter, config);
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

            return worker.getNumberOfDuplicates();
        } finally {
            if (workerPool != null && !workerPool.isTerminated()) {
                workerPool.shutdownNow();
            }
        }
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        shouldRun = false;
    }
}
