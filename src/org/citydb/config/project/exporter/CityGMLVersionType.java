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
package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="CityGMLVersionType")
@XmlEnum
public enum CityGMLVersionType {
	@XmlEnumValue("v2.0.0")
	v2_0_0("v2.0.0 (OGC Encoding Standard)"),
	@XmlEnumValue("v1.0.0")
	v1_0_0("v1.0.0");

	private final String value;

	CityGMLVersionType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
	
	public static CityGMLVersionType fromValue(String value) {
		for (CityGMLVersionType type : values())
			if (type.toString().equals(value))
				return type;
		
		return v2_0_0;
	}

}
