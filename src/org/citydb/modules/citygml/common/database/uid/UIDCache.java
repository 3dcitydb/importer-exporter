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
package org.citydb.modules.citygml.common.database.uid;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.log.Logger;
import org.citygml4j.model.citygml.CityGMLClass;

public class UIDCache {
	private final Logger LOG = Logger.getInstance();
	
	private final ConcurrentHashMap<String, UIDCacheEntry> map;
	private final UIDCachingModel cacheModel;
	private final int capacity;
	private final float drainFactor;

	private final ReentrantLock mainLock = new ReentrantLock(true);
	private final Condition drainingDone = mainLock.newCondition();

	private final AtomicBoolean isDraining = new AtomicBoolean(false);
	private final AtomicInteger entries = new AtomicInteger(0);
	private volatile boolean backUp = false;

	public UIDCache(
			UIDCachingModel cacheModel,
			int capacity,
			float drainFactor,
			int concurrencyLevel) {
		this.cacheModel = cacheModel;
		this.capacity = capacity;
		this.drainFactor = drainFactor;

		map = new ConcurrentHashMap<String, UIDCacheEntry>(capacity, .75f, concurrencyLevel);
	}

	public void put(String key, long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
		UIDCacheEntry entry = lookupMap(key);

		if (entry == null) {
			entry = getOrCreate(key, id, rootId, reverse, mapping, type);

			if (!entry.getAndSetRegistered(true)) {
				if (entries.incrementAndGet() >= capacity && isDraining.compareAndSet(false, true)) 
					drainToDB();
			}
		}
	}

	public boolean lookupAndPut(String key, long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
		boolean lookup = lookupMap(key) != null;
		if (!lookup && backUp)
			lookup = lookupDB(key) != null;

		if (!lookup) {		
			UIDCacheEntry entry = getOrCreate(key, id, rootId, reverse, mapping, type);
			if (!entry.getAndSetRegistered(true)) {
				if (entries.incrementAndGet() >= capacity && isDraining.compareAndSet(false, true))
					drainToDB();
			} else
				lookup = true;
		}

		return lookup;
	}

	public boolean lookupAndPut(String key, long id, CityGMLClass type) {
		return lookupAndPut(key, id, 0, false, null, type);
	}

	public UIDCacheEntry get(String key) {
		UIDCacheEntry entry = lookupMap(key);
		if (entry == null && backUp)
			entry = lookupDB(key);

		return entry;
	}

	public String get(long id, CityGMLClass type) {
		String key = lookupMap(id, type);
		if (key == null && backUp)
			key = lookupDB(id, type);

		return key;
	}
	
	public UIDCacheEntry getFromMemory(String key) {
		return lookupMap(key);
	}

	private UIDCacheEntry lookupMap(String key) {
		UIDCacheEntry entry = map.get(key);
		if (entry != null)
			entry.getAndSetRequested(true);

		return entry;
	}

	private String lookupMap(long id, CityGMLClass type) {
		Iterator<Map.Entry<String, UIDCacheEntry>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, UIDCacheEntry> entry = iter.next();
			if (entry.getValue().getId() == id)
				if (entry.getValue().getType().isInstance(type))
					return entry.getKey();
		}

		return null;
	}

	private UIDCacheEntry getOrCreate(String key, long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
		UIDCacheEntry entry = map.get(key);
		if (entry == null) {
			UIDCacheEntry newEntry = new UIDCacheEntry(id, rootId, reverse, mapping, type);
			entry = map.putIfAbsent(key, newEntry);
			if (entry == null)
				entry = newEntry;
		}

		return entry;
	}

	private void drainToDB() {
		try {
			LOG.debug("Writing entries to " + cacheModel.getType() + " cache.");
			backUp = true;
			
			int drain = Math.round(capacity * drainFactor);
			try {
				cacheModel.drainToDB(map, drain);
				entries.set(map.size());

				LOG.debug("Entries written to " + cacheModel.getType() + " cache.");

			} catch (SQLException sqlEx) {
				LOG.error("SQL error while writing entries to " + cacheModel.getType() + " cache: " + sqlEx.getMessage());
			}
		} finally {
			final ReentrantLock lock = this.mainLock;
			lock.lock();

			try {
				isDraining.set(false);
				drainingDone.signalAll();
			} finally {
				lock.unlock();
			}
		}
	}

	private UIDCacheEntry lookupDB(String key) {
		if (isDraining.get()) {
			final ReentrantLock lock = this.mainLock;
			lock.lock();
			
			try {
				while (isDraining.get())
					drainingDone.await();
			} catch (InterruptedException ie) {
				//
			} finally {
				lock.unlock();
			}
		}

		try {			
			return cacheModel.lookupDB(key);
		} catch (SQLException sqlEx) {
			LOG.error("SQL error while querying the " + cacheModel.getType() + " cache: " + sqlEx.getMessage());
			return null;
		} 
	}

	private String lookupDB(long id, CityGMLClass type) {
		if (isDraining.get()) {
			final ReentrantLock lock = this.mainLock;
			lock.lock();

			try {
				while (isDraining.get())
					drainingDone.await();
			} catch (InterruptedException ie) {
				//
			} finally {
				lock.unlock();
			}
		}
		
		try {
			return cacheModel.lookupDB(id, type);
		} catch (SQLException sqlEx) {
			LOG.error("SQL error while querying the " + cacheModel.getType() + " cache: " + sqlEx.getMessage());
			return null;
		} 
	}
	
	public void shutdown() throws SQLException {
		cacheModel.close();
	}
}
