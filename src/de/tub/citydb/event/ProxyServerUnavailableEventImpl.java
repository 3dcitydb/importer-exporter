package de.tub.citydb.event;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.api.event.global.GlobalEvents;
import de.tub.citydb.api.event.global.ProxyServerUnavailableEvent;
import de.tub.citydb.api.io.ProxyConfig;

public class ProxyServerUnavailableEventImpl extends Event implements ProxyServerUnavailableEvent {
	private final ProxyConfig proxy;
	
	public ProxyServerUnavailableEventImpl(ProxyConfig proxy, Object source) {
		super(GlobalEvents.PROXY_SERVER_UNAVAILABLE, source);
		this.proxy = proxy;
	}

	@Override
	public ProxyConfig getProxy() {
		return proxy;
	}
	
}
