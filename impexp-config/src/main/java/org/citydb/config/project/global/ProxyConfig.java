/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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
package org.citydb.config.project.global;

import org.citydb.config.i18n.Language;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

@XmlType(name = "ProxyConfigType", propOrder = {
        "type",
        "host",
        "port",
        "username",
        "password",
        "savePassword"
})
public class ProxyConfig {
    @XmlAttribute(required = true)
    private Boolean isEnabled = false;
    @XmlAttribute(required = true)
    private Boolean requiresAuthentication;
    @XmlAttribute(required = true)
    private ProxyType type = ProxyType.HTTP;
    private String host = "";
    private Integer port = 0;
    private String username = "";
    private String password = "";
    private Boolean savePassword = false;
    @XmlTransient
    private String tempPassword = "";
    @XmlTransient
    private int failedConnectAttempts = 0;
    @XmlTransient
    private ProxyConfig other = null;

    public ProxyConfig() {
    }

    public ProxyConfig(ProxyType type, ProxyConfig other) {
        this.type = type;
        this.other = other;

        isEnabled = other.isEnabled;
        requiresAuthentication = other.requiresAuthentication;
        host = other.host;
        port = other.port;
        username = other.username;
        password = other.password;
        savePassword = other.savePassword;
        tempPassword = other.tempPassword;
        failedConnectAttempts = other.failedConnectAttempts;
    }

    public ProxyConfig(ProxyType type) {
        this.type = type;
        switch (type) {
            case HTTP:
                port = 80;
                break;
            case HTTPS:
                port = 443;
                break;
            case SOCKS:
                port = 1080;
                break;
        }
    }

    public boolean isEnabled() {
        return isEnabled != null ? isEnabled : false;
    }

    public void setEnabled(boolean enable) {
        this.isEnabled = enable;
    }

    public boolean requiresAuthentication() {
        return requiresAuthentication != null ? requiresAuthentication : false;
    }

    public void setRequiresAuthentication(boolean requiresAuthentication) {
        this.requiresAuthentication = requiresAuthentication;
    }

    public ProxyType getType() {
        return type;
    }

    public void setType(ProxyType type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        if (host != null)
            this.host = host;
    }

    public int getPort() {
        return port != null ? port : 0;
    }

    public void setPort(int port) {
        if (port > 0)
            this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String user) {
        if (user != null)
            this.username = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password != null)
            this.password = password;
    }

    public boolean isSavePassword() {
        return savePassword != null ? savePassword : false;
    }

    public void setSavePassword(boolean savePassword) {
        this.savePassword = savePassword;
    }

    public int failed() {
        return ++failedConnectAttempts;
    }

    public void resetFailedConnectAttempts() {
        failedConnectAttempts = 0;
    }

    public boolean hasValidProxySettings() {
        return host.length() > 0 && port > 0;
    }

    public boolean hasValidUserCredentials() {
        return username.length() > 0 && password.length() > 0;
    }

    public boolean isCopy() {
        return other != null;
    }

    public ProxyConfig getCopiedFrom() {
        return other;
    }

    public Proxy toProxy() {
        if (hasValidProxySettings()) {
            switch (type) {
                case HTTP:
                case HTTPS:
                    return new Proxy(Type.HTTP, new InetSocketAddress(host, port));
                case SOCKS:
                    return new Proxy(Type.SOCKS, new InetSocketAddress(host, port));
            }
        }

        return null;
    }

    @Override
    public String toString() {
        switch (type) {
            case HTTP:
                return Language.I18N.getString("pref.proxy.label.http");
            case HTTPS:
                return Language.I18N.getString("pref.proxy.label.https");
            case SOCKS:
                return Language.I18N.getString("pref.proxy.label.socks");
            default:
                return "n/a";
        }
    }

    void beforeMarshal(Marshaller marshaller) {
        if (!isSavePassword()) {
            tempPassword = password;
            password = null;
        }
    }

    void afterMarshal(Marshaller marshaller) {
        if (!isSavePassword()) {
            password = tempPassword;
            tempPassword = null;
        }
    }
}
