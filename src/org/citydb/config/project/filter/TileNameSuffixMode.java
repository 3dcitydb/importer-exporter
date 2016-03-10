/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
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
