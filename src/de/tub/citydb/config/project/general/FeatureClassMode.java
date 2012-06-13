/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.config.project.general;

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
	@XmlEnumValue("WaterBody")
    WATERBODY("WaterBody"),
	@XmlEnumValue("LandUse")
    LANDUSE("LandUse"),
	@XmlEnumValue("Vegetation")
    VEGETATION("Vegetation"),
	@XmlEnumValue("Transportation")
    TRANSPORTATION("Transportation"),
	@XmlEnumValue("ReliefFeature")
    RELIEFFEATURE("ReliefFeature"),
	@XmlEnumValue("CityFurniture")
    CITYFURNITURE("CityFurniture"),
	@XmlEnumValue("GenericCityObject")
    GENERICCITYOBJECT("GenericCityObject"),
	@XmlEnumValue("CityObjectGroup")
    CITYOBJECTGROUP("CityObjectGroup");
	
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
