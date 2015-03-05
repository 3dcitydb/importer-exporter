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
package org.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="IndexModeType")
@XmlEnum
public enum IndexMode {
	@XmlEnumValue("unchanged")
    UNCHANGED("unchanged"),
	@XmlEnumValue("deactivate")
    DEACTIVATE("deactivate"),
    @XmlEnumValue("deactivate_activate")
    DEACTIVATE_ACTIVATE("deactivate_activate");

    private final String value;

    IndexMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static IndexMode fromValue(String v) {
        for (IndexMode c: IndexMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return UNCHANGED;
    }
}
