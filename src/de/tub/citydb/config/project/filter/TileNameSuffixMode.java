/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.internal.Internal;

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
    		return Internal.I18N.getString("pref.export.boundingBox.label.tile.nameSuffix.none");
    	case SAME_AS_PATH:
    		return Internal.I18N.getString("pref.export.boundingBox.label.tile.nameSuffix.sameAsPath");
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
