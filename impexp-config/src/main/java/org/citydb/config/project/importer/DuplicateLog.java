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
package org.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "DuplicateLogType", propOrder = {
        "logDuplicates",
        "logFile"
})
public class DuplicateLog {
    @XmlElement(required = true, defaultValue = "false")
    private Boolean logDuplicates = false;
    private String logFile;

    public boolean isSetLogDuplicates() {
        return logDuplicates != null ? logDuplicates : false;
    }

    public Boolean getLogDuplicates() {
        return logDuplicates;
    }

    public void setLogDuplicates(Boolean logDuplicates) {
        this.logDuplicates = logDuplicates;
    }

    public boolean isSetLogFile() {
        return logFile != null;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        if (logFile != null && !logFile.isEmpty()) {
            this.logFile = logFile;
        }
    }
}
