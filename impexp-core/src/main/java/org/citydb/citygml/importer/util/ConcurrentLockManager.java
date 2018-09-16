/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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
package org.citydb.citygml.importer.util;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentLockManager {
	private static HashMap<String, ConcurrentLockManager> instances;	
	private final ConcurrentHashMap<String, ReentrantLock> locks;

	private ConcurrentLockManager() {
		locks = new ConcurrentHashMap<>();
	}

	public static synchronized ConcurrentLockManager getInstance(Class<?> className) {
		if (instances == null)
			instances = new HashMap<>();

		return instances.computeIfAbsent(className.getName(), v -> new ConcurrentLockManager());
	}

	public ReentrantLock getLock(String key) {
		return locks.computeIfAbsent(key, v -> new ReentrantLock());
	}

	public void releaseLock(String key) {
		locks.remove(key);
	}

}
