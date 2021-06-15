/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.operation.common.cache;

import org.citydb.util.log.Logger;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class IdCache {
	private final Logger log = Logger.getInstance();
	
	private final ConcurrentHashMap<String, IdCacheEntry> map;
	private final IdCachingModel cacheModel;
	private final int capacity;
	private final float drainFactor;

	private final ReentrantLock mainLock = new ReentrantLock();
	private final Condition drainingDone = mainLock.newCondition();

	private final AtomicBoolean isDraining = new AtomicBoolean(false);
	private final AtomicInteger entries = new AtomicInteger(0);
	private volatile boolean backUp = false;

	public IdCache(
			IdCachingModel cacheModel,
			int capacity,
			float drainFactor,
			int concurrencyLevel) {
		this.cacheModel = cacheModel;
		this.capacity = capacity;
		this.drainFactor = drainFactor;

		map = new ConcurrentHashMap<>(capacity, .75f, concurrencyLevel);
	}

	public void put(String key, long id, long rootId, boolean reverse, String mapping, int objectClassId) {
		IdCacheEntry entry = lookupMap(key);

		if (entry == null) {
			entry = getOrCreate(key, id, rootId, reverse, mapping, objectClassId);

			if (!entry.getAndSetRegistered(true)) {
				if (entries.incrementAndGet() >= capacity && isDraining.compareAndSet(false, true)) 
					drainToDB();
			}
		}
	}

	public boolean lookupAndPut(String key, long id, long rootId, boolean reverse, String mapping, int objectClassId) {
		boolean lookup = lookupMap(key) != null;
		if (!lookup && backUp)
			lookup = lookupDB(key) != null;

		if (!lookup) {		
			IdCacheEntry entry = getOrCreate(key, id, rootId, reverse, mapping, objectClassId);
			if (!entry.getAndSetRegistered(true)) {
				if (entries.incrementAndGet() >= capacity && isDraining.compareAndSet(false, true))
					drainToDB();
			} else
				lookup = true;
		}

		return lookup;
	}

	public boolean lookupAndPut(String key, long id, int objectClassId) {
		return lookupAndPut(key, id, 0, false, null, objectClassId);
	}

	public IdCacheEntry get(String key) {
		IdCacheEntry entry = lookupMap(key);
		if (entry == null && backUp)
			entry = lookupDB(key);

		return entry;
	}

	public IdCacheEntry getFromMemory(String key) {
		return lookupMap(key);
	}

	private IdCacheEntry lookupMap(String key) {
		IdCacheEntry entry = map.get(key);
		if (entry != null)
			entry.getAndSetRequested(true);

		return entry;
	}

	private IdCacheEntry getOrCreate(String key, long id, long rootId, boolean reverse, String mapping, int objectClassId) {
		IdCacheEntry entry = map.get(key);
		if (entry == null) {
			IdCacheEntry newEntry = new IdCacheEntry(id, rootId, reverse, mapping, objectClassId);
			entry = map.putIfAbsent(key, newEntry);
			if (entry == null)
				entry = newEntry;
		}

		return entry;
	}

	private void drainToDB() {
		try {
			log.debug("Writing entries to " + cacheModel.getType() + " cache.");
			backUp = true;
			
			int drain = Math.round(capacity * drainFactor);
			try {
				cacheModel.drainToDB(map, drain);
				entries.set(map.size());

				log.debug("Entries written to " + cacheModel.getType() + " cache.");

			} catch (SQLException e) {
				log.error("SQL error while writing entries to " + cacheModel.getType() + " cache.", e);
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

	private IdCacheEntry lookupDB(String key) {
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
		} catch (SQLException e) {
			log.error("SQL error while querying the " + cacheModel.getType() + " cache.", e);
			return null;
		} 
	}
	
	public void shutdown() throws SQLException {
		cacheModel.close();
	}
}
