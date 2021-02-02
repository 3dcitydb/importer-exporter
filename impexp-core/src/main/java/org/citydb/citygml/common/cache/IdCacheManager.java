/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
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
package org.citydb.citygml.common.cache;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class IdCacheManager {
	private final Map<IdCacheType, IdCache> cacheMap;

	public IdCacheManager() {
		cacheMap = new HashMap<>();
	}

	public void initCache(
		IdCacheType cacheType,
		IdCachingModel model,
		int cacheSize,
		float drainFactor,
		int concurrencyLevel) {

		cacheMap.put(cacheType, new IdCache(
				model,
				cacheSize,
				drainFactor,
				concurrencyLevel
		));
	}
	
	public IdCache getCache(IdCacheType cacheType) {
		return cacheMap.get(cacheType);
	}
	
	public void shutdownAll() throws SQLException {
		for (IdCache server : cacheMap.values())
			server.shutdown();
	}
}
