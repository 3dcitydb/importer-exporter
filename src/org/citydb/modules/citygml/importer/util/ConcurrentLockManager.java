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
package org.citydb.modules.citygml.importer.util;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentLockManager {
	private static HashMap<Class<?>, ConcurrentLockManager> instances;	
	private final ConcurrentHashMap<String, ReentrantLock> locks;

	private ConcurrentLockManager() {
		locks = new ConcurrentHashMap<String, ReentrantLock>();
	}

	public static synchronized ConcurrentLockManager getInstance(Class<?> className) {
		if (instances == null)
			instances = new HashMap<Class<?>, ConcurrentLockManager>();

		ConcurrentLockManager instance = instances.get(className);
		if (instance == null) {
			instance = new ConcurrentLockManager();
			instances.put(className, instance);
		}

		return instance;
	}

	public ReentrantLock putAndGetLock(String key) {
		ReentrantLock entry = locks.get(key);
		if (entry == null) {
			ReentrantLock lock = new ReentrantLock();
			entry = locks.putIfAbsent(key, lock);
			if (entry == null)
				entry = lock;
		}

		return entry;
	}

	public void releaseLock(String key) {
		locks.remove(key);
	}

	public synchronized void clear() {
		if (instances != null) {
			instances.values().removeIf(v -> v == this);
			
			if (instances.isEmpty()) {
				instances.clear();
				instances = null;
			}
		}		
	}
}
