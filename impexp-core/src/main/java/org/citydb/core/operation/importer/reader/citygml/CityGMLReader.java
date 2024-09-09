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

package org.citydb.core.operation.importer.reader.citygml;

import org.citydb.config.Config;
import org.citydb.core.file.InputFile;
import org.citydb.core.operation.importer.concurrent.FeatureReaderWorkerFactory;
import org.citydb.core.operation.importer.filter.selection.counter.CounterFilter;
import org.citydb.core.operation.importer.reader.FeatureReadException;
import org.citydb.core.operation.importer.reader.FeatureReader;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.util.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.EventType;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.XMLChunk;

import java.io.IOException;
import java.io.InputStream;

public class CityGMLReader implements FeatureReader, EventHandler {
    private final CityGMLInputFilter typeFilter;
    private final CounterFilter counterFilter;
    private final ValidationErrorHandler validationHandler;
    private final CityGMLInputFactory factory;
    private final Object eventChannel;
    private final Config config;
    private final EventDispatcher eventDispatcher;
    private final int minThreads, maxThreads;

    private volatile boolean shouldRun = true;

    CityGMLReader(CityGMLInputFilter typeFilter, CounterFilter counterFilter, ValidationErrorHandler validationHandler, CityGMLInputFactory factory, Object eventChannel, Config config) {
        this.typeFilter = typeFilter;
        this.counterFilter = counterFilter;
        this.validationHandler = validationHandler;
        this.factory = factory;
        this.eventChannel = eventChannel;
        this.config = config;

        minThreads = config.getImportConfig().getResources().getThreadPool().getMinThreads();
        maxThreads = config.getImportConfig().getResources().getThreadPool().getMaxThreads();

        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
        eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
    }

    @Override
    public long getValidationErrors() {
        return validationHandler != null ? validationHandler.getValidationErrors() : 0;
    }

    @Override
    public void read(InputFile inputFile, WorkerPool<CityGML> workerPool) throws FeatureReadException {
        if (validationHandler != null)
            validationHandler.reset();

        WorkerPool<XMLChunk> featureWorkerPool = null;
        org.citygml4j.xml.io.reader.CityGMLReader reader;

        try {
            // this worker pool unmarshals feature chunks and passes them to the database worker pool
            featureWorkerPool = new WorkerPool<>(
                    "citygml_parser_pool",
                    minThreads,
                    maxThreads,
                    PoolSizeAdaptationStrategy.AGGRESSIVE,
                    new FeatureReaderWorkerFactory(workerPool, config, eventDispatcher),
                    maxThreads * 2,
                    false);

            featureWorkerPool.setEventSource(eventChannel);
            featureWorkerPool.prestartCoreWorkers();

            try {
                reader = factory.createFilteredCityGMLReader(
                        createCityGMLReader(inputFile.getFile().toString(), inputFile.openStream()), typeFilter);

                while (shouldRun && reader.hasNext()) {
                    XMLChunk chunk = reader.nextChunk();
                    CityGMLClass type = chunk.getCityGMLClass();

                    if (counterFilter != null && type != CityGMLClass.APPEARANCE) {
                        if (!counterFilter.isStartIndexSatisfied()) {
                            counterFilter.incrementStartIndex();
                            continue;
                        }

                        counterFilter.incrementCount();
                        if (!counterFilter.isCountSatisfied())
                            continue;
                    }

                    featureWorkerPool.addWork(chunk);
                }
            } catch (CityGMLReadException | IOException e) {
                throw new FeatureReadException("Failed to read CityGML input file.", e);
            }

            try {
                featureWorkerPool.shutdownAndWait();
            } catch (InterruptedException e) {
                throw new FeatureReadException("Failed to shutdown CityGML feature reader pool.", e);
            }

            try {
                reader.close();
            } catch (CityGMLReadException e) {
                throw new FeatureReadException("Failed to close CityGML reader.", e);
            }
        } finally {
            if (featureWorkerPool != null && !featureWorkerPool.isTerminated()) {
                featureWorkerPool.shutdownNow();
            }
        }
    }

    @Override
    public void close() throws FeatureReadException {
        eventDispatcher.removeEventHandler(this);
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        shouldRun = false;
    }

    private org.citygml4j.xml.io.reader.CityGMLReader createCityGMLReader(String systemId, InputStream stream) throws CityGMLReadException {
        return !config.getImportConfig().getGeneralOptions().isSetFileEncoding() ?
                factory.createCityGMLReader(systemId, stream) :
                factory.createCityGMLReader(systemId, stream, config.getImportConfig().getGeneralOptions().getFileEncoding());
    }
}
