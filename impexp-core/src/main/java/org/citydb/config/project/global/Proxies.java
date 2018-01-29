/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name="ProxiesType", propOrder={
		"proxies"
})
public class Proxies {
	@XmlAttribute
	private Boolean useSingleProxy = false;
	@XmlAttribute
	private ProxyType singleProxy;
	@XmlJavaTypeAdapter(org.citydb.config.project.global.ProxyListAdapter.class)
	@XmlElement(name="proxyList")
	private HashMap<ProxyType, ProxyConfig> proxies;

	public Proxies() {
		proxies = new HashMap<ProxyType, ProxyConfig>();
	}

	public ProxyConfig getProxy(ProxyType type) {
		ProxyConfig proxy = proxies.get(type);
		if (proxy == null) {
			proxy = new ProxyConfig(type);
			proxies.put(type, proxy);
		}

		return proxy;
	}
	
	public ProxyConfig getProxyForProtocol(ProxyType type) {
		ProxyConfig proxy = null;
		if (type != null) {
			proxy = isSetSingleProxy() ? getSingleProxy() : getProxy(type);
			if (proxy.getType() != type)
				proxy = new ProxyConfig(type, proxy);
		}

		return proxy;
	}

	public ProxyConfig getProxyForProtocol(String protocol) {
		return getProxyForProtocol(ProxyType.fromProtocol(protocol));
	}

	public void setProxy(ProxyConfig proxy) {
		if (proxy != null)
			proxies.put(proxy.getType(), proxy);
	}

	public List<ProxyConfig> getProxyList() {
		List<ProxyConfig> proxies = new ArrayList<ProxyConfig>();
		for (ProxyType type : ProxyType.values())
			proxies.add(getProxy(type));
		
		return proxies;
	}

	public Boolean isSetSingleProxy() {
		return useSingleProxy == true && singleProxy != null;
	}

	public void setSingleProxy(ProxyType type) {
		if (type != null) {
			useSingleProxy = true;
			singleProxy = type;
		}
	}

	public void unsetSingleProxy() {
		useSingleProxy = false;
		singleProxy = null;
	}

	public ProxyConfig getSingleProxy() {
		if (isSetSingleProxy())
			return proxies.get(singleProxy);

		return null;
	}

	public ProxyType getSingleProxyType() {
		return singleProxy;
	}
	
	public void reset() {
		proxies.clear();
	}

}
