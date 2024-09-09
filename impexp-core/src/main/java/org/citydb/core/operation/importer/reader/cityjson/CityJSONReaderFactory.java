/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

package org.citydb.core.operation.importer.reader.cityjson;

import org.citydb.config.Config;
import org.citydb.core.operation.importer.filter.CityGMLFilter;
import org.citydb.core.operation.importer.filter.selection.counter.CounterFilter;
import org.citydb.core.operation.importer.reader.FeatureReadException;
import org.citydb.core.operation.importer.reader.FeatureReader;
import org.citydb.core.operation.importer.reader.FeatureReaderFactory;
import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.cityjson.CityJSONBuilder;
import org.citygml4j.builder.cityjson.CityJSONBuilderException;
import org.citygml4j.builder.cityjson.json.io.reader.CityJSONInputFactory;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;

public class CityJSONReaderFactory implements FeatureReaderFactory {
    private CityJSONInputFactory factory;
    private CityGMLInputFilter typeFilter;
    private CounterFilter counterFilter;
    private Config config;

    @Override
    public void initializeContext(CityGMLFilter filter, Object eventChannel, Config config) throws FeatureReadException {
        this.config = config;

        CityJSONBuilder builder = CityGMLContext.getInstance().createCityJSONBuilder();
        try {
            factory = builder.createCityJSONInputFactory();
            factory.setProcessUnknownExtensions(config.getImportConfig().getCityJSONOptions().isMapUnknownExtensions());
        } catch (CityJSONBuilderException e) {
            throw new FeatureReadException("Failed to initialize CityJSON input factory.", e);
        }

        // prepare feature filter
        typeFilter = name -> {
            Module module = Modules.getModule(name.getNamespaceURI());
            if (module != null && module.getType() == CityGMLModuleType.APPEARANCE && name.getLocalPart().equals("Appearance"))
                return config.getImportConfig().getAppearances().isSetImportAppearance();
            else
                return filter.getFeatureTypeFilter().isSatisfiedBy(name, true);
        };

        counterFilter = filter.getCounterFilter();
    }

    @Override
    public FeatureReader createFeatureReader() throws FeatureReadException {
        return new CityJSONReader(typeFilter, counterFilter, factory, config);
    }
}
