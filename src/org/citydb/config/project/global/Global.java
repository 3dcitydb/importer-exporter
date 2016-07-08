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
package org.citydb.config.project.global;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="GlobalType", propOrder={
		"cache",
		"logging",
		"language",
		"proxies"
		})
public class Global {
	private Cache cache;
	private Logging logging;
	private LanguageType language = LanguageType.fromValue(System.getProperty("user.language"));
	private Proxies proxies;

	public Global() {
		cache = new Cache();
		logging = new Logging();
		proxies = new Proxies();
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
		this.language = language;
	}

	public Proxies getProxies() {
		return proxies;
	}

	public void setProxies(Proxies proxies) {
		this.proxies = proxies;
	}
}
