/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.io;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.api.event.EventDispatcher;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.config.project.global.ProxyConfigImpl;
import org.citydb.event.ProxyServerUnavailableEventImpl;
import org.citydb.log.Logger;

public class InternalProxySelector extends ProxySelector {
	private static InternalProxySelector instance;
	private final ReentrantLock mainLock = new ReentrantLock();

	private final Logger LOG = Logger.getInstance();
	private final ProxySelector parent;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private final int maxConnectAttempts = 3;

	private InternalProxySelector(Config config) {
		this.parent = ProxySelector.getDefault();
		this.config = config;

		Authenticator.setDefault(new InternalAuthenticator());
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
	}

	public static synchronized InternalProxySelector getInstance(Config config) {
		if (instance == null)
			instance = new InternalProxySelector(config);

		return instance;
	}

	@Override
	public List<Proxy> select(URI uri) {
		if (uri == null)
			throw new IllegalArgumentException("URI can't be null.");

		List<Proxy> proxies = new ArrayList<Proxy>();
		ProxyConfigImpl proxy = config.getProject().getGlobal().getProxies().getProxyForProtocol(uri.getScheme());

		/*System.out.println(uri.getHost());
		InetSocketAddress a = new InetSocketAddress(uri.getHost(), 0);
		System.out.println(a.getHostName());*/
		
		if (proxy != null && proxy.isEnabled() && proxy.hasValidProxySettings())		
			proxies.add(proxy.toProxy());
		else if (parent != null)
			proxies = parent.select(uri);
		else
			proxies.add(Proxy.NO_PROXY);

		return proxies;
	}

	@Override
	public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
		if (uri == null || sa == null || ioe == null)
			throw new IllegalArgumentException("Arguments can't be null.");

		ProxyConfigImpl proxy = config.getProject().getGlobal().getProxies().getProxyForProtocol(uri.getScheme());

		if (proxy != null) {
			if (proxy.isCopy())
				proxy = proxy.getCopiedFrom();

			InetSocketAddress proxyAddress = new InetSocketAddress(proxy.getHost(), proxy.getPort());
			if (proxyAddress.equals(sa)) {
				final ReentrantLock lock = this.mainLock;
				lock.lock();

				try {
					int connectAttempts = proxy.failed();					
					if (connectAttempts <= maxConnectAttempts)
						LOG.warn("Could not connect to " + proxy.getType().toString() + " proxy server at " + proxy.getHost() + ":" + proxy.getPort() + ".");

					if (connectAttempts == maxConnectAttempts) {
						LOG.error("Failed " + maxConnectAttempts + " times to connect to " + proxy.getType().toString() + " proxy server.");
						proxy.setEnabled(false);
						eventDispatcher.triggerEvent(new ProxyServerUnavailableEventImpl(proxy, this));
					}
				} finally {
					lock.unlock();
				}
			}
		} else
			parent.connectFailed(uri, sa, ioe);
	}

	@SuppressWarnings("unchecked")
	public void resetAuthenticationCache(ProxyConfigImpl proxy) {
		// the following is a hack to overcome a bug in Oracle's Java 6

		try {
			Class<?> containerClass = Class.forName("sun.net.www.protocol.http.AuthCacheValue");
			Class<?> cacheClass = Class.forName("sun.net.www.protocol.http.AuthCacheImpl");
			Class<?> authInfoClass = Class.forName("sun.net.www.protocol.http.AuthenticationInfo");

			// static field holding the AuthCacheImpl cache
			Field cacheField = containerClass.getDeclaredField("cache");
			cacheField.setAccessible(true);
			Object authCacheImplObj = cacheField.get(containerClass);

			// field holding the authInfo entries
			Field hashtableField = cacheClass.getDeclaredField("hashtable");
			hashtableField.setAccessible(true);

			// methods for querying authInfo information
			Method getHostMethod = authInfoClass.getDeclaredMethod("getHost", (Class<?>[])null);
			Method getPortMethod = authInfoClass.getDeclaredMethod("getPort", (Class<?>[])null);
			Method removeFromCacheMethod = authInfoClass.getDeclaredMethod("removeFromCache", (Class<?>[])null);
			getHostMethod.setAccessible(true);
			getPortMethod.setAccessible(true);
			removeFromCacheMethod.setAccessible(true);

			HashMap<String, Object> hashtable = (HashMap<String, Object>)hashtableField.get(authCacheImplObj);
			for (String key : hashtable.keySet()) {
				if (key.startsWith("p")) {
					LinkedList<Object> authInfos = (LinkedList<Object>)hashtable.get(key); 
					ArrayList<Object> authInfosCopy = new ArrayList<Object>();				
					ListIterator<Object> iter = authInfos.listIterator();
					while (iter.hasNext())
						authInfosCopy.add(iter.next());

					for (Object authInfoObj : authInfosCopy) {	
						String host = (String)getHostMethod.invoke(authInfoObj, (Object[])null);
						int port = (Integer)getPortMethod.invoke(authInfoObj, (Object[])null);

						if (proxy.getHost().equals(host) && proxy.getPort() == port)
							removeFromCacheMethod.invoke(authInfoObj, (Object[])null);
					}
				}
			}

		} catch (Exception e) {
			Authenticator.setDefault(null);
			Authenticator.setDefault(new InternalAuthenticator());
		}
	}

	private final class InternalAuthenticator extends Authenticator {

		@Override
		protected PasswordAuthentication getPasswordAuthentication() {	
			if (getRequestorType() == RequestorType.PROXY) {
				ProxyConfigImpl proxy = config.getProject().getGlobal().getProxies().getProxyForProtocol(getRequestingProtocol());
				if (proxy != null && proxy.requiresAuthentication() && proxy.hasValidUserCredentials() &&
						proxy.getHost().equals(getRequestingHost()) && proxy.getPort() == getRequestingPort()) {
					return new PasswordAuthentication(proxy.getUsername(), proxy.getInternalPassword().toCharArray());				
				}
			}

			return super.getPasswordAuthentication();
		}
	}
}
