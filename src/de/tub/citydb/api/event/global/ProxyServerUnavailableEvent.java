package de.tub.citydb.api.event.global;

import de.tub.citydb.api.io.ProxyConfig;

public interface ProxyServerUnavailableEvent {
	public ProxyConfig getProxy();
}
