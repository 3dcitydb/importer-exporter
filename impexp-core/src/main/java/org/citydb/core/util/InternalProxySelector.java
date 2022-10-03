/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.util;

import org.citydb.config.Config;
import org.citydb.config.project.global.ProxyConfig;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.global.ProxyServerUnavailableEvent;
import org.citydb.util.log.Logger;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class InternalProxySelector extends ProxySelector {
	private static InternalProxySelector instance;
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger log = Logger.getInstance();
	private final ProxySelector parent;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private InternalProxySelector() {
		this.parent = ProxySelector.getDefault();
		config = ObjectRegistry.getInstance().getConfig();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		setDefaultAuthentication();
	}

	public static synchronized InternalProxySelector getInstance() {
		if (instance == null) {
			instance = new InternalProxySelector();
		}

		return instance;
	}

	@Override
	public List<Proxy> select(URI uri) {
		List<Proxy> proxies = new ArrayList<>();
		ProxyConfig proxy = config.getGlobalConfig().getProxies().getProxyForProtocol(uri.getScheme());

		if (proxy != null && proxy.isEnabled() && proxy.hasValidProxySettings())
			proxies.add(proxy.toProxy());
		else if (parent != null)
			proxies = parent.select(uri);
		else
			proxies.add(Proxy.NO_PROXY);

		return proxies;
	}

	@Override
	public void connectFailed(URI uri, SocketAddress socketAddress, IOException e) {
		ProxyConfig proxy = config.getGlobalConfig().getProxies().getProxyForProtocol(uri.getScheme());
		if (proxy != null) {
			if (proxy.isCopy())
				proxy = proxy.getCopiedFrom();

			InetSocketAddress proxyAddress = new InetSocketAddress(proxy.getHost(), proxy.getPort());
			if (proxyAddress.equals(socketAddress)) {
				final ReentrantLock lock = this.mainLock;
				lock.lock();
				try {
					int connectAttempts = proxy.failed();
					int maxConnectAttempts = 3;
					if (connectAttempts <= maxConnectAttempts)
						log.warn("Could not connect to " + proxy.getType().toString() + " proxy server at " + proxy.getHost() + ":" + proxy.getPort() + ".");

					if (connectAttempts == maxConnectAttempts) {
						log.error("Failed " + maxConnectAttempts + " times to connect to " + proxy.getType().toString() + " proxy server.");
						proxy.setEnabled(false);
						Authenticator.setDefault(null);
						eventDispatcher.triggerEvent(new ProxyServerUnavailableEvent(proxy));
					}
				} finally {
					lock.unlock();
				}
			}
		} else
			parent.connectFailed(uri, socketAddress, e);
	}

	public void setDefaultAuthentication() {
		Authenticator.setDefault(new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				if (getRequestorType() == RequestorType.PROXY) {
					ProxyConfig proxy = config.getGlobalConfig().getProxies().getProxyForProtocol(getRequestingProtocol());
					if (proxy != null
							&& proxy.requiresAuthentication()
							&& proxy.hasValidUserCredentials()
							&& proxy.getHost().equals(getRequestingHost())
							&& proxy.getPort() == getRequestingPort()) {
						return new PasswordAuthentication(proxy.getUsername(), proxy.getPassword().toCharArray());
					}
				}

				return super.getPasswordAuthentication();
			}
		});
	}
}
