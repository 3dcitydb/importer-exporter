package de.tub.citydb.config.project.global;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ProxyListType", propOrder={
		"proxies"
})
public class ProxyList {
	@XmlElement(name="proxy")
	private List<ProxyConfigImpl> proxies;

	public ProxyList() {
		proxies = new ArrayList<ProxyConfigImpl>();
	}

	public List<ProxyConfigImpl> getProxies() {
		return proxies;
	}

	public void addProxy(ProxyConfigImpl proxy) {
		if (proxy != null)
			proxies.add(proxy);
	}
}
