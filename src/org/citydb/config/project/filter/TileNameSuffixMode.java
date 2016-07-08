/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.language.Language;

@XmlType(name="TileNameSuffixModeType")
@XmlEnum
public enum TileNameSuffixMode {
	@XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("sameAsPath")
    SAME_AS_PATH("sameAsPath");

    private final String value;

    TileNameSuffixMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public String toString() {
    	switch (this) {
    	case NONE:
    		return Language.I18N.getString("pref.export.boundingBox.label.tile.nameSuffix.none");
    	case SAME_AS_PATH:
    		return Language.I18N.getString("pref.export.boundingBox.label.tile.nameSuffix.sameAsPath");
    	default:
    		return null;
    	}
    }
    
    public static TileNameSuffixMode fromValue(String v) {
        for (TileNameSuffixMode c: TileNameSuffixMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return NONE;
    }
}
