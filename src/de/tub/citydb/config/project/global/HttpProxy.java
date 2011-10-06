/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 *
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.config.project.global;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.util.io.Base64;

@XmlType(name="HttpProxyType", propOrder={
		"host",
		"port",
		"user",
		"password",
		"savePassword"
		})
public class HttpProxy {
	@XmlAttribute(required=true)
	private Boolean useProxy = false;
	private String host = "";
	private int port = 0;
	private String user = "";
	private String password = "";
	private boolean savePassword = false;
	@XmlTransient
	private String internalPassword = "";

	public HttpProxy() {
	}
	
	public boolean isSetUseProxy() {
		if (useProxy != null)
			return useProxy.booleanValue();

		return false;
	}

	public Boolean getUseProxy() {
		return useProxy;
	}

	public void setUseProxy(Boolean useProxy) {
		this.useProxy = useProxy;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setSavePassword(boolean savePassword) {
		this.savePassword = savePassword;
	}

	public boolean isSavePassword() {
		return savePassword;
	}

	public void setInternalPassword(String internalPassword) {
		this.internalPassword = internalPassword;
	}

	public String getInternalPassword() {
		return internalPassword.length() > 0 ? internalPassword : password;
	}
	
	public boolean hasValidProxySettings() {
		return host.length() > 0 && port > 0;
	}
	
	public boolean hasUserCredentials() {
		return user.length() > 0 && password.length() > 0;	
	}
	
	public Proxy getProxy() {
		return hasValidProxySettings() ? new Proxy(Type.HTTP, new InetSocketAddress(host, port)) : null;
	}
	
	public String getBase64EncodedCredentials() {
		return hasUserCredentials() ? Base64.encode(user + ":" + internalPassword) : null;
	}

}
