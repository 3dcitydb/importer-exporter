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
package org.citydb.config.project.global;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.time.ZoneId;
import java.time.ZoneOffset;

@XmlRootElement(name = "global")
@XmlType(name = "GlobalType", propOrder = {
        "cache",
        "logging",
        "language",
        "proxies",
        "apiKeys",
        "timeZone"
})
public class GlobalConfig {
    private Cache cache;
    private Logging logging;
    private LanguageType language = LanguageType.fromValue(System.getProperty("user.language"));
    private Proxies proxies;
    private APIKeys apiKeys;
    private String timeZone;

    @XmlTransient
    private ZoneId zoneId;

    public GlobalConfig() {
        cache = new Cache();
        logging = new Logging();
        proxies = new Proxies();
        apiKeys = new APIKeys();
    }

    public Cache getCache() {
        return cache;
    }

    public void setCache(Cache cache) {
        if (cache != null)
            this.cache = cache;
    }

    public Logging getLogging() {
        return logging;
    }

    public void setLogging(Logging logging) {
        if (logging != null)
            this.logging = logging;
    }

    public LanguageType getLanguage() {
        return language;
    }

    public void setLanguage(LanguageType language) {
        if (language != null)
            this.language = language;
    }

    public Proxies getProxies() {
        return proxies;
    }

    public void setProxies(Proxies proxies) {
        if (proxies != null)
            this.proxies = proxies;
    }

    public APIKeys getApiKeys() {
        return apiKeys;
    }

    public void setApiKeys(APIKeys apiKeys) {
        if (apiKeys != null)
            this.apiKeys = apiKeys;
    }

    public ZoneId getZoneId() {
        return zoneId != null ? zoneId : ZoneOffset.UTC;
    }

    public void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId;
        timeZone = zoneId != null ? zoneId.getId() : null;
    }

    void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if (timeZone != null) {
            try {
                zoneId = ZoneId.of(timeZone);
            } catch (Exception e) {
                timeZone = null;
            }
        }
    }
}
