/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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

@XmlType(name="TileSuffixModeType")
@XmlEnum
public enum TileSuffixMode {
	@XmlEnumValue("row_column")
    ROW_COLUMN("row_column"),
    @XmlEnumValue("xMin_yMin")
    XMIN_YMIN("xMin_yMin"),
    @XmlEnumValue("xMax_yMin")
    XMAX_YMIN("xMax_yMin"),
    @XmlEnumValue("xMin_yMax")
    XMIN_YMAX("xMin_yMax"),
    @XmlEnumValue("xMax_yMax")
    XMAX_YMAX("xMax_yMax"),
    @XmlEnumValue("xMin_yMin_xMax_yMax")
    XMIN_YMIN_XMAX_YMAX("xMin_yMin_xMax_yMax");

    private final String value;

    TileSuffixMode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
    
    public String toString() {
    	switch (this) {
    	case ROW_COLUMN:
    		return Language.I18N.getString("pref.export.boundingBox.label.tile.pathSuffix.rowColumn");
    	case XMIN_YMIN:
    		return "Xmin / Ymin";
    	case XMIN_YMAX:
    		return "Xmin / Ymax";
    	case XMAX_YMIN:
    		return "Xmax / Ymin";
    	case XMAX_YMAX:
    		return "Xmax / Ymax";
    	case XMIN_YMIN_XMAX_YMAX:
    		return "Xmin / Ymin / Xmax / Ymax";
    	default:
    		return null;
    	}
    }

    public static TileSuffixMode fromValue(String v) {
        for (TileSuffixMode c: TileSuffixMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return ROW_COLUMN;
    }
}
