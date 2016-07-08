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
