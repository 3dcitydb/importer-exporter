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
package org.citydb.config.project.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.citydb.api.io.ProxyType;

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
	private HashMap<ProxyType, ProxyConfigImpl> proxies;

	public Proxies() {
		proxies = new HashMap<ProxyType, ProxyConfigImpl>();
	}

	public ProxyConfigImpl getProxy(ProxyType type) {
		ProxyConfigImpl proxy = proxies.get(type);
		if (proxy == null) {
			proxy = new ProxyConfigImpl(type);
			proxies.put(type, proxy);
		}

		return proxy;
	}
	
	public ProxyConfigImpl getProxyForProtocol(ProxyType type) {
		ProxyConfigImpl proxy = null;
		if (type != null) {
			proxy = isSetSingleProxy() ? getSingleProxy() : getProxy(type);
			if (proxy.getType() != type)
				proxy = new ProxyConfigImpl(type, proxy);
		}

		return proxy;
	}

	public ProxyConfigImpl getProxyForProtocol(String protocol) {
		return getProxyForProtocol(ProxyType.fromProtocol(protocol));
	}

	public void setProxy(ProxyConfigImpl proxy) {
		if (proxy != null)
			proxies.put(proxy.getType(), proxy);
	}

	public List<ProxyConfigImpl> getProxyList() {
		List<ProxyConfigImpl> proxies = new ArrayList<ProxyConfigImpl>();
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

	public ProxyConfigImpl getSingleProxy() {
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
