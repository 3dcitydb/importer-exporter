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

package org.citydb.operation.importer.reader.cityjson;

import org.citydb.operation.importer.filter.selection.counter.CounterFilter;
import org.citydb.operation.importer.reader.FeatureReadException;
import org.citydb.operation.importer.reader.FeatureReader;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.file.InputFile;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.builder.cityjson.json.io.reader.CityJSONChunkReader;
import org.citygml4j.builder.cityjson.json.io.reader.CityJSONInputFactory;
import org.citygml4j.builder.cityjson.json.io.reader.CityJSONReadException;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;

import java.io.InputStream;

public class CityJSONReader implements FeatureReader, EventHandler {
    private final CityGMLInputFilter typeFilter;
    private final CounterFilter counterFilter;
    private final CityJSONInputFactory factory;
    private final Config config;
    private final EventDispatcher eventDispatcher;

    private WorkerPool<CityGML> workerPool;
    private volatile boolean shouldRun = true;

    CityJSONReader(CityGMLInputFilter typeFilter, CounterFilter counterFilter, CityJSONInputFactory factory, Config config) {
        this.typeFilter = typeFilter;
        this.counterFilter = counterFilter;
        this.factory = factory;
        this.config = config;

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
                createCityJSONChunkReader(inputFile.openStream()), typeFilter)) {
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

    private CityJSONChunkReader createCityJSONChunkReader(InputStream stream) throws CityJSONReadException {
        return !config.getImportConfig().getGeneralOptions().isSetFileEncoding() ?
                factory.createCityJSONChunkReader(stream) :
                factory.createCityJSONChunkReader(stream, config.getImportConfig().getGeneralOptions().getFileEncoding());
    }
}
