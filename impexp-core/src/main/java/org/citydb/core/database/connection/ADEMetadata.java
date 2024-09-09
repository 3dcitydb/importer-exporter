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
package org.citydb.core.database.connection;

public class ADEMetadata {
    private String adeId;
    private String name;
    private String description;
    private String version;
    private String dbPrefix;
    private boolean isSupported;

    public String getADEId() {
        return adeId;
    }

    public void setADEId(String adeId) {
        this.adeId = adeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDBPrefix() {
        return dbPrefix;
    }

    public void setDBPrefix(String dbPrefix) {
        this.dbPrefix = dbPrefix;
    }

    public boolean isSupported() {
        return isSupported;
    }

    public void setSupported(boolean isSupported) {
        this.isSupported = isSupported;
    }

    public String toString() {
        return new StringBuilder(name)
                .append(" ")
                .append(version)
                .toString();
    }

}
