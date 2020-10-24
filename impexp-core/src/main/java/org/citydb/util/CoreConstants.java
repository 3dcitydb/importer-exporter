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
package org.citydb.util;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CoreConstants {
    public static final Path IMPEXP_DATA_DIR = Paths.get(System.getProperty("user.home"), "3dcitydb", "importer-exporter");
    public static final String IMPORT_LOG_DIR = "imported-features";
    public static final String LIBRARY_OBJECTS_DIR = "library-objects";
    public static final URL CITYDB_SCHEMA_MAPPING_FILE = CoreConstants.class.getResource("/org/citydb/database/schema/3dcitydb-schema.xml");

    public static final String DEFAULT_DELIMITER = "--/\\--";
    public static final String OBJECT_ORIGINAL_GMLID = "origGMLId";
    public static final String GEOMETRY_XLINK = "isXlink";
    public static final String GEOMETRY_ORIGINAL = "origGeom";
    public static final String GEOMETRY_INVALID = "geomInvalid";
    public static final String TEXTURE_IMAGE_XLINK = "textureXlink";
    public static final String FOREIGN_KEYS_SET = "foreignKeys";
    public static final String EXPORT_STUB = "exportStub";
    public static final String EXPORT_AS_ADDITIONAL_OBJECT = "additionalObject";
    public static final String UNIQUE_TEXTURE_FILENAME_PREFIX = "tex_";

    public static boolean IS_GUI_MODE = false;
}
