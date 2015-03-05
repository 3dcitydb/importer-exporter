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
package org.citydb.config.project.global;

import java.util.HashMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.citydb.api.io.ProxyType;

public class ProxyListAdapter extends XmlAdapter<ProxyList, HashMap<ProxyType, ProxyConfigImpl>> {

	@Override
	public HashMap<ProxyType, ProxyConfigImpl> unmarshal(ProxyList v) throws Exception {
		HashMap<ProxyType, ProxyConfigImpl> map = new HashMap<ProxyType, ProxyConfigImpl>();
		
		if (v != null) {
			for (ProxyConfigImpl proxy : v.getProxies())
				map.put(proxy.getType(), proxy);
		}
		
		for (ProxyType type : ProxyType.values())
			if (!map.containsKey(type))
				map.put(type, new ProxyConfigImpl(type));

		return map;
	}

	@Override
	public ProxyList marshal(HashMap<ProxyType, ProxyConfigImpl> v) throws Exception {
		ProxyList list = new ProxyList();
		
		if (v != null) {
			for (ProxyConfigImpl proxy : v.values())
				list.addProxy(proxy);
		}
		
		return list;
	}
	
}
