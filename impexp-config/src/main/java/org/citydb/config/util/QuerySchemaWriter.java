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

package org.citydb.config.util;

import org.citydb.config.ConfigUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class QuerySchemaWriter {

	public static void main(String[] args) throws Exception {
		Path configFile = Paths.get("src/main/resources/org/citydb/config/schema/query.xsd");
		System.out.print("Generting XML schema in " + configFile.toAbsolutePath() + "... ");
		
		JAXBContext context = JAXBContext.newInstance(QueryWrapper.class);
		context.generateSchema(new SchemaOutputResolver() {
			@Override
			public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
				if (ConfigUtil.CITYDB_CONFIG_NAMESPACE_URI.equals(namespaceUri)) {
					Files.createDirectories(configFile.getParent());
					StreamResult res = new StreamResult();
					res.setSystemId(configFile.toUri().toString());

					return res;
				} else
					return null;
			}
			
		});
		
		System.out.println("finished.");
	}

}
