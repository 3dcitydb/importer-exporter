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

@XmlType(name="BalloonContentMode")
@XmlEnum
public enum BalloonContentMode {
	@XmlEnumValue("gen_attrib")
    GEN_ATTRIB("gen_attrib"),
    @XmlEnumValue("file")
    FILE("file"),
    @XmlEnumValue("gen_attrib_and_file")
    GEN_ATTRIB_AND_FILE("gen_attrib_and_file");

    private final String value;

    BalloonContentMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BalloonContentMode fromValue(String v) {
        for (BalloonContentMode c: BalloonContentMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return GEN_ATTRIB;
    }
}
