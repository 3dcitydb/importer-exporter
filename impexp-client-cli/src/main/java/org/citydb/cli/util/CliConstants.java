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

package org.citydb.cli.util;

import java.io.File;

public class CliConstants {
    public static final String CLI_NAME = "impexp";
    public static final String SRS_TEMPLATES_DIR = "templates" + File.separator + "CoordinateReferenceSystems";
    public static final String XSLT_TEMPLATES_DIR = "templates" + File.separator + "XSLTransformations";
    public static final String PLUGINS_DIR = "plugins";
    public static final String ADE_EXTENSIONS_DIR = "ade-extensions";
    public static final String LOG_DIR = "log";
    public static final String CONFIG_DIR = "config";
    public static final String PROJECT_SETTINGS_FILE = "project.xml";
    public static final String GUI_SETTINGS_FILE = "gui.xml";
}
