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
package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.citygml4j.model.module.citygml.CityGMLVersion;

@XmlType(name="CityGMLVersionType")
@XmlEnum
public enum CityGMLVersionType {
	@XmlEnumValue("v1.0.0")
	v1_0_0("v1.0.0 (OGC Encoding Standard)"),
	@XmlEnumValue("v0.4.0")
	v0_4_0("v0.4.0");

	private final String value;

	CityGMLVersionType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
	
	public CityGMLVersion toCityGMLVersion() {
		switch (this) {
		case v0_4_0:
			return CityGMLVersion.v0_4_0;
		default:
			return CityGMLVersion.v1_0_0;
		}
	}

	public static CityGMLVersionType fromCityGMLVersion(CityGMLVersion version) {
		if (version == CityGMLVersion.v0_4_0)
			return v0_4_0;
		else
			return v1_0_0;
	}
	
	public static CityGMLVersionType fromValue(String value) {
		for (CityGMLVersionType type : values())
			if (type.toString().equals(value))
				return type;
		
		return v1_0_0;
	}

}
