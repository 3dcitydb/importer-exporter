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
package org.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="AltitudeOffsetMode")
@XmlEnum
public enum AltitudeOffsetMode {
	@XmlEnumValue("no_offset")
    NO_OFFSET("no_offset"),
    @XmlEnumValue("constant")
    CONSTANT("constant"),
    @XmlEnumValue("bottom_zero")
    BOTTOM_ZERO("bottom_zero"),
    @XmlEnumValue("generic_attribute")
    GENERIC_ATTRIBUTE("generic_attribute");

    private final String value;

    AltitudeOffsetMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AltitudeOffsetMode fromValue(String v) {
        for (AltitudeOffsetMode c: AltitudeOffsetMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return NO_OFFSET;
    }
}
