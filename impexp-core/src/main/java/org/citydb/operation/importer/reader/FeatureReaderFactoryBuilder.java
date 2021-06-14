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

package org.citydb.operation.importer.reader;

import org.apache.tika.mime.MediaType;
import org.citydb.config.Config;
import org.citydb.file.InputFile;
import org.citydb.operation.importer.filter.CityGMLFilter;
import org.citydb.operation.importer.reader.citygml.CityGMLReaderFactory;
import org.citydb.operation.importer.reader.cityjson.CityJSONReaderFactory;

import java.util.HashMap;
import java.util.Map;

public class FeatureReaderFactoryBuilder {
    private Map<MediaType, FeatureReaderFactory> factories = new HashMap<>();

    public FeatureReaderFactory buildFactory(InputFile file, CityGMLFilter filter, Config config) throws FeatureReadException {
        FeatureReaderFactory factory = factories.get(file.getMediaType());
        if (factory == null) {
            if (file.getMediaType().equals(InputFile.APPLICATION_XML))
                factory = new CityGMLReaderFactory();
            else if (file.getMediaType().equals(InputFile.APPLICATION_JSON))
                factory = new CityJSONReaderFactory();

            if (factory == null)
                throw new FeatureReadException("No reader available for media type '" + file.getMediaType() + "'.");

            factory.initializeContext(filter, config);
            factories.put(file.getMediaType(), factory);
        }

        return factory;
    }

}
