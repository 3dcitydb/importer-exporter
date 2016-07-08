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
package org.citydb.config.project.general;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="FeatureClassModeType")
@XmlEnum
public enum FeatureClassMode {
	@XmlEnumValue("CityObject")
    CITYOBJECT("CityObject"),
	@XmlEnumValue("Building")
    BUILDING("Building"),
    @XmlEnumValue("Bridge")
    BRIDGE("Bridge"),
	@XmlEnumValue("CityFurniture")
    CITYFURNITURE("CityFurniture"),
	@XmlEnumValue("CityObjectGroup")
    CITYOBJECTGROUP("CityObjectGroup"),
	@XmlEnumValue("GenericCityObject")
    GENERICCITYOBJECT("GenericCityObject"),
	@XmlEnumValue("LandUse")
    LANDUSE("LandUse"),
	@XmlEnumValue("ReliefFeature")
    RELIEFFEATURE("ReliefFeature"),
	@XmlEnumValue("Transportation")
    TRANSPORTATION("Transportation"),
    @XmlEnumValue("Tunnel")
    TUNNEL("Tunnel"),
	@XmlEnumValue("Vegetation")
    VEGETATION("Vegetation"),
	@XmlEnumValue("WaterBody")
    WATERBODY("WaterBody");
	
	private final String value;

	FeatureClassMode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
    
    public String toString() {
    	return value;
    }

    public static FeatureClassMode fromValue(String v) {
        for (FeatureClassMode c: FeatureClassMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return CITYOBJECT;
    }
}
