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
package org.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.citydb.config.language.Language;

@XmlType(name="AltitudeMode")
@XmlEnum
public enum AltitudeMode {
	@XmlEnumValue("relative")
    RELATIVE("relative"),
    @XmlEnumValue("absolute")
    ABSOLUTE("absolute"),
    @XmlEnumValue("clampToGround")
    CLAMP_TO_GROUND("clampToGround");
    
    private final String value;

    AltitudeMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AltitudeMode fromValue(String v) {
        for (AltitudeMode c: AltitudeMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return ABSOLUTE;
    }
    
	public String toString() {
		return Language.I18N.getString("pref.kmlexport.altitude.mode.label." + value());
	}

}
