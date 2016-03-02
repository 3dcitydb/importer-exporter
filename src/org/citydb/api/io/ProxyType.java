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
package org.citydb.api.io;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ProxyType")
@XmlEnum
public enum ProxyType {
	@XmlEnumValue("http")
	HTTP("HTTP", "http"),
	@XmlEnumValue("https")
	HTTPS("HTTPS", "https"),
	@XmlEnumValue("socks")
	SOCKS("SOCKS", "socket", "socks");

	private final String value;
	private final String[] protocols;

	ProxyType(String value, String... protocol) {
		this.value = value;
		this.protocols = protocol;
	}

	public static ProxyType fromProtocol(String protocol) {
		if (protocol != null) {
			String p = protocol.toLowerCase().trim();
			for (ProxyType type: ProxyType.values()) {
				for (String tmp : type.protocols) {
					if (tmp.equals(p))
						return type;
				}
			}
		}

		return null;
	}

	public String toString() {
		return value;
	}
}
