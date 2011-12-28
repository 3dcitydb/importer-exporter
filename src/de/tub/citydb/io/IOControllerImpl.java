package de.tub.citydb.io;

import de.tub.citydb.api.controller.IOController;
import de.tub.citydb.api.io.ProxyConfig;
import de.tub.citydb.api.io.ProxyType;
import de.tub.citydb.config.Config;

public class IOControllerImpl implements IOController {
	private final Config config;
	
	public IOControllerImpl(Config config) {
		this.config = config;
	}
	
	@Override
	public ProxyConfig getProxy(ProxyType type) {
		return config.getProject().getGlobal().getProxies().getProxyForProtocol(type);
	}

	@Override
	public ProxyConfig getProxy(String protocol) {
		return getProxy(ProxyType.fromProtocol(protocol));
	}

}
