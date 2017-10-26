/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.modules.citygml.common.database.uid;

import java.sql.SQLException;
import java.util.HashMap;

import org.citygml4j.model.citygml.CityGMLClass;

public class UIDCacheManager {
	private final HashMap<UIDCacheType, UIDCache> cacheMap;

	public UIDCacheManager() {
		cacheMap = new HashMap<UIDCacheType, UIDCache>();
	}

	public void initCache(
		UIDCacheType cacheType,
		UIDCachingModel model,
		int cacheSize,
		float drainFactor,
		int concurrencyLevel) {

		cacheMap.put(cacheType, new UIDCache(
				model,
				cacheSize,
				drainFactor,
				concurrencyLevel
		));
	}

	public UIDCache getCache(CityGMLClass type) {
		UIDCacheType cacheType;

		switch (type) {
		case ABSTRACT_GML_GEOMETRY:
			cacheType = UIDCacheType.GEOMETRY;
			break;
		case ABSTRACT_TEXTURE:
			cacheType = UIDCacheType.TEX_IMAGE;
			break;
		default:
			cacheType = UIDCacheType.FEATURE;
		}

		return cacheMap.get(cacheType);
	}
	
	public void shutdownAll() throws SQLException {
		for (UIDCache server : cacheMap.values())
			server.shutdown();
	}
}
