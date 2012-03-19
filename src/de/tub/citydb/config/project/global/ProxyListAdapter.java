/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
package de.tub.citydb.config.project.global;

import java.util.HashMap;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.tub.citydb.api.io.ProxyType;

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
