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
package org.citydb.config.gui.style;

import org.citydb.config.project.global.LogLevel;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ConsoleStyleType", propOrder = {
        "debug",
        "info",
        "warn",
        "error"
})
public class ConsoleStyle {
    private LogLevelStyle debug = new LogLevelStyle("#ff0000ee");
    private LogLevelStyle info = new LogLevelStyle();
    private LogLevelStyle warn = new LogLevelStyle("#ffa66f00");
    private LogLevelStyle error = new LogLevelStyle("#ffcd0000");

    public LogLevelStyle getLogLevelStyle(LogLevel level) {
        switch (level) {
            case DEBUG:
                return debug;
            case INFO:
                return info;
            case WARN:
                return warn;
            case ERROR:
                return error;
            default:
                throw new IllegalArgumentException("No style definition for log level " + level.name() + ".");
        }
    }

    public void setLogLevelStyle(LogLevel level, LogLevelStyle style) {
        if (style != null) {
            switch (level) {
                case DEBUG:
                    debug = style;
                    break;
                case INFO:
                    info = style;
                    break;
                case WARN:
                    warn = style;
                    break;
                case ERROR:
                    error = style;
                    break;
            }
        }
    }
}
