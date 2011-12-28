package de.tub.citydb.api.controller;

import de.tub.citydb.api.io.ProxyConfig;
import de.tub.citydb.api.io.ProxyType;

public interface IOController {
	public ProxyConfig getProxy(ProxyType type);
	public ProxyConfig getProxy(String protocol);
}
