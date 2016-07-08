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
