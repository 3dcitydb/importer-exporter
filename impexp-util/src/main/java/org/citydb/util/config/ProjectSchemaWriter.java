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
package org.citydb.util.config;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ProjectSchemaWriter extends SchemaOutputResolver {
    private final Path targetDir;

    public ProjectSchemaWriter(File path) {
        this.targetDir = path.toPath();
    }

    @Override
    public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
        Path file = namespaceUri.equals("http://www.3dcitydb.org/importer-exporter/config") ?
                targetDir.resolve("config.xsd") :
                targetDir.resolve("plugin_" + suggestedFileName);

        StreamResult res = new StreamResult(file.toFile());
        res.setSystemId(URLDecoder.decode(file.toUri().toURL().toString(), StandardCharsets.UTF_8.name()));

        return res;
    }

}
