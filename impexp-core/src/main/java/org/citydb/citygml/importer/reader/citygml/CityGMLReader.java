package org.citydb.citygml.importer.reader.citygml;

import org.citydb.citygml.importer.concurrent.FeatureReaderWorkerFactory;
import org.citydb.citygml.importer.filter.selection.counter.CounterFilter;
import org.citydb.citygml.importer.reader.FeatureReadException;
import org.citydb.citygml.importer.reader.FeatureReader;
import org.citydb.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.file.InputFile;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.XMLChunk;

import java.io.IOException;

public class CityGMLReader implements FeatureReader, EventHandler {
    private final CityGMLInputFilter typeFilter;
    private final CounterFilter counterFilter;
    private final ValidationErrorHandler validationHandler;
    private final CityGMLInputFactory factory;
    private final Config config;
    private final EventDispatcher eventDispatcher;
    private final int minThreads, maxThreads;

    private volatile boolean shouldRun = true;

    CityGMLReader(CityGMLInputFilter typeFilter, CounterFilter counterFilter, ValidationErrorHandler validationHandler, CityGMLInputFactory factory, Config config) {
        this.typeFilter = typeFilter;
        this.counterFilter = counterFilter;
        this.validationHandler = validationHandler;
        this.factory = factory;
        this.config = config;

        minThreads = config.getProject().getImporter().getResources().getThreadPool().getDefaultPool().getMinThreads();
        maxThreads = config.getProject().getImporter().getResources().getThreadPool().getDefaultPool().getMaxThreads();

        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
        eventDispatcher.addEventHandler(EventType.INTERRUPT,this);
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

            featureWorkerPool.prestartCoreWorkers();

            try {
                reader = factory.createFilteredCityGMLReader(factory.createCityGMLReader(inputFile.getFile().toString(), inputFile.openStream()), typeFilter);

                while (shouldRun && reader.hasNext()) {
                    XMLChunk chunk = reader.nextChunk();

                    if (counterFilter != null) {
                        if (!counterFilter.isStartIndexSatisfied()) {
                            counterFilter.incrementStartIndex();
                            continue;
                        }

                        counterFilter.incrementCount();
                        if (!counterFilter.isCountSatisfied())
                            break;
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
            if (featureWorkerPool != null && !featureWorkerPool.isTerminated())
                featureWorkerPool.shutdownNow();
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
}
