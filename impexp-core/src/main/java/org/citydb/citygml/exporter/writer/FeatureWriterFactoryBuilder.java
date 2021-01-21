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
package org.citydb.citygml.exporter.writer;

import org.citydb.citygml.exporter.writer.citygml.CityGMLWriterFactory;
import org.citydb.citygml.exporter.writer.cityjson.CityJSONWriterFactory;
import org.citydb.config.Config;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.Query;
import org.citydb.util.Util;

import java.nio.file.Path;

public class FeatureWriterFactoryBuilder {

	public static FeatureWriterFactory buildFactory(Path outputFile, Query query, SchemaMapping schemaMapping, Config config) throws FeatureWriteException {
		switch (Util.getFileExtension(outputFile)) {
			case "json":
			case "cityjson":
				return new CityJSONWriterFactory(query, config);
			default:
				return new CityGMLWriterFactory(query, schemaMapping, config);
		}
	}
}
