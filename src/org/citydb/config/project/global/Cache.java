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

import java.io.File;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlType(name="CacheType", propOrder={
		"mode",
		"localPath"
})
public class Cache {
	@XmlElement(required=true)
	private CacheMode mode = CacheMode.DATABASE;
	private String localPath;

	public Cache() {
		File tmp = new File(System.getProperty("java.io.tmpdir") + "3dcitydb.tmp");
		if ((tmp.exists() || tmp.mkdir()) && tmp.canWrite())
			localPath = tmp.getAbsolutePath();
	}

	public boolean isUseDatabase() {
		return mode == CacheMode.DATABASE;
	}

	public boolean isUseLocal() {
		return mode == CacheMode.LOCAL;
	}

	public CacheMode getCacheMode() {
		return mode;
	}

	public void setCacheMode(CacheMode mode) {
		this.mode = mode;
	}

	public String getLocalCachePath() {	
		return localPath;
	}

	public void setLocalCachePath(String localPath) {
		this.localPath = localPath;
	}

}
