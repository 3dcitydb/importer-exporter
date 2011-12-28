package de.tub.citydb.config.project.global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.tub.citydb.api.io.ProxyType;

@XmlType(name="ProxiesType", propOrder={
		"proxies"
})
public class Proxies {
	@XmlAttribute
	private Boolean useSingleProxy = false;
	@XmlAttribute
	private ProxyType singleProxy;
	@XmlJavaTypeAdapter(de.tub.citydb.config.project.global.ProxyListAdapter.class)
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
