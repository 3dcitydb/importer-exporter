/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
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
