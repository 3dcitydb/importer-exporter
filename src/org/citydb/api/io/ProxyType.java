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
