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
