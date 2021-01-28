package org.citydb.citygml.importer.reader.cityjson;

import org.citydb.citygml.importer.filter.selection.counter.CounterFilter;
import org.citydb.citygml.importer.reader.FeatureReadException;
import org.citydb.citygml.importer.reader.FeatureReader;
import org.citydb.concurrent.WorkerPool;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.file.InputFile;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.builder.cityjson.json.io.reader.CityJSONChunkReader;
import org.citygml4j.builder.cityjson.json.io.reader.CityJSONInputFactory;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;

public class CityJSONReader implements FeatureReader, EventHandler {
    private final CityGMLInputFilter typeFilter;
    private final CounterFilter counterFilter;
    private final CityJSONInputFactory factory;
    private final EventDispatcher eventDispatcher;

    private WorkerPool<CityGML> workerPool;
    private volatile boolean shouldRun = true;

    CityJSONReader(CityGMLInputFilter typeFilter, CounterFilter counterFilter, CityJSONInputFactory factory) {
        this.typeFilter = typeFilter;
        this.counterFilter = counterFilter;
        this.factory = factory;

        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
        eventDispatcher.addEventHandler(EventType.INTERRUPT,this);
    }

    @Override
    public long getValidationErrors() {
        return 0;
    }

    @Override
    public void read(InputFile inputFile, WorkerPool<CityGML> workerPool) throws FeatureReadException {
        this.workerPool = workerPool;

        try (CityJSONChunkReader reader = factory.createFilteredCityJSONReader(
                factory.createCityJSONChunkReader(inputFile.openStream()), typeFilter)) {
            reader.read(this::process);
        } catch (Exception e) {
            throw new FeatureReadException("Failed to read CityJSON input file.", e);
        }
    }

    private void process(AbstractFeature feature) {
        if (shouldRun) {
            if (feature instanceof CityGML) {
                if (counterFilter != null && !(feature instanceof Appearance)) {
                    if (!counterFilter.isStartIndexSatisfied()) {
                        counterFilter.incrementStartIndex();
                        return;
                    }

                    counterFilter.incrementCount();
                    if (!counterFilter.isCountSatisfied()) {
                        return;
                    }
                }

                workerPool.addWork((CityGML) feature);
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
}
