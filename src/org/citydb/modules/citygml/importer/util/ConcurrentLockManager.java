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
			instances.remove(this);
			
			if (instances.isEmpty()) {
				instances.clear();
				instances = null;
			}
		}		
	}
}
