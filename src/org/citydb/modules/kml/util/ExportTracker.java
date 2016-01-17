/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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
package org.citydb.modules.kml.util;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ExportTracker {
	
	private final ConcurrentHashMap<Long, CityObject4JSON> map;
	private String currentWorkingDirectoryPath;
	
	public ExportTracker() {
		map = new ConcurrentHashMap<Long, CityObject4JSON>();
	}
	
	public void put(long id, CityObject4JSON json) {
		map.putIfAbsent(id, json);
	}
	
	public CityObject4JSON get(long id) {
		return map.get(id);
	}
	
	public void clear() {
		map.clear();
	}
	
	public Collection<CityObject4JSON> values() {
		return map.values();
	}

	public String getCurrentWorkingDirectoryPath() {
		return currentWorkingDirectoryPath;
	}

	public void setCurrentWorkingDirectoryPath(String currentWorkingDirectoryPath) {
		this.currentWorkingDirectoryPath = currentWorkingDirectoryPath;
	}
	
}
