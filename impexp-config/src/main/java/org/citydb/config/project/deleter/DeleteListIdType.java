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
package org.citydb.config.project.deleter;

import org.citydb.config.i18n.Language;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "DeleteListIdType")
@XmlEnum
public enum DeleteListIdType {
    @XmlEnumValue("resource")
    RESOURCE_ID("resource"),
    @XmlEnumValue("database")
    DATABASE_ID("database");

    private final String value;

    DeleteListIdType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DeleteListIdType fromValue(String v) {
        for (DeleteListIdType c : DeleteListIdType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return RESOURCE_ID;
    }


    @Override
    public String toString() {
        switch (this) {
            case RESOURCE_ID:
                return Language.I18N.getString("delete.list.id.resource");
            case DATABASE_ID:
                return Language.I18N.getString("delete.list.id.database");
            default:
                return "";
        }
    }
}
