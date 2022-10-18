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
package org.citydb.config.project.importer;

import org.citydb.config.i18n.Language;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ImportModeType")
@XmlEnum
public enum ImportMode {
    @XmlEnumValue("importAll")
    IMPORT_ALL("importAll"),
    @XmlEnumValue("skipExisting")
    SKIP_EXISTING("skipExisting"),
    @XmlEnumValue("deleteExisting")
    DELETE_EXISTING("deleteExisting"),
    @XmlEnumValue("terminateExisting")
    TERMINATE_EXISTING("terminateExisting");

    private final String value;

    ImportMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ImportMode fromValue(String v) {
        for (ImportMode c : ImportMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return IMPORT_ALL;
    }

    @Override
    public String toString() {
        switch (this) {
            case IMPORT_ALL:
                return Language.I18N.getString("import.mode.importAll");
            case SKIP_EXISTING:
                return Language.I18N.getString("import.mode.skipExisting");
            case DELETE_EXISTING:
                return Language.I18N.getString("import.mode.deleteExisting");
            case TERMINATE_EXISTING:
                return Language.I18N.getString("import.mode.terminateExisting");
            default:
                return "";
        }
    }
}
