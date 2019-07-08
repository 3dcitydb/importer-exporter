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

package org.citydb.citygml.validator.reader;

import org.apache.tika.mime.MediaType;
import org.citydb.citygml.validator.ValidationException;
import org.citydb.citygml.validator.reader.citygml.CityGMLValidatorFactory;
import org.citydb.citygml.validator.reader.cityjson.CityJSONValidatorFactory;
import org.citydb.config.Config;
import org.citydb.file.InputFile;

import java.util.HashMap;
import java.util.Map;

public class ValidatorFactoryBuilder {
    private Map<MediaType, ValidatorFactory> factories = new HashMap<>();

    public ValidatorFactory buildFactory(InputFile file, Config config) throws ValidationException {
        ValidatorFactory factory = factories.get(file.getMediaType());
        if (factory == null) {
            if (file.getMediaType().equals(InputFile.APPLICATION_XML))
                factory = new CityGMLValidatorFactory();
            else if (file.getMediaType().equals(InputFile.APPLICATION_JSON))
                factory = new CityJSONValidatorFactory();

            if (factory == null)
                throw new ValidationException("No validator available for media type '" + file.getMediaType() + "'.");

            factory.initializeContext(config);
            factories.put(file.getMediaType(), factory);
        }

        return factory;
    }

}
