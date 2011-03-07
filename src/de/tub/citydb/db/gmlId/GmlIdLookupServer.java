package de.tub.citydb.db.gmlId;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.citygml4j.model.citygml.CityGMLClass;

import de.tub.citydb.log.Logger;

public class GmlIdLookupServer {
	private final Logger LOG = Logger.getInstance();
	
	private final ConcurrentHashMap<String, GmlIdEntry> map;
	private final DBCacheModel cacheModel;
	private final int capacity;
	private final float drainFactor;

	private final ReentrantLock mainLock = new ReentrantLock(true);
	private final Condition drainingDone = mainLock.newCondition();

	private final AtomicBoolean isDraining = new AtomicBoolean(false);
	private final AtomicInteger entries = new AtomicInteger(0);
	private volatile boolean backUp = false;

	public GmlIdLookupServer(
			DBCacheModel model,
			int capacity,
			float drainFactor,
			int concurrencyLevel) throws SQLException {
		this.cacheModel = model;
		this.capacity = capacity;
		this.drainFactor = drainFactor;

		map = new ConcurrentHashMap<String, GmlIdEntry>(capacity, .75f, concurrencyLevel);
	}

	public void put(String key, long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
		GmlIdEntry entry = lookupMap(key);

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
			GmlIdEntry entry = getOrCreate(key, id, rootId, reverse, mapping, type);
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

	public GmlIdEntry get(String key) {
		GmlIdEntry entry = lookupMap(key);
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

	private GmlIdEntry lookupMap(String key) {
		GmlIdEntry entry = map.get(key);
		if (entry != null)
			entry.getAndSetRequested(true);

		return entry;
	}

	private String lookupMap(long id, CityGMLClass type) {
		Iterator<Map.Entry<String, GmlIdEntry>> iter = map.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, GmlIdEntry> entry = iter.next();
			if (entry.getValue().getId() == id)
				if (entry.getValue().getType().childOrSelf(type))
					return entry.getKey();
		}

		return null;
	}

	private GmlIdEntry getOrCreate(String key, long id, long rootId, boolean reverse, String mapping, CityGMLClass type) {
		GmlIdEntry entry = map.get(key);
		if (entry == null) {
			GmlIdEntry newEntry = new GmlIdEntry(id, rootId, reverse, mapping, type);
			entry = map.putIfAbsent(key, newEntry);
			if (entry == null)
				entry = newEntry;
		}

		return entry;
	}

	private void drainToDB() {
		try {
			LOG.debug("Writing gml:id " + cacheModel.getType() + " cache to database.");
			backUp = true;
			
			int drain = Math.round(capacity * drainFactor);
			try {
				cacheModel.drainToDB(map, drain);
				entries.set(map.size());

				LOG.debug("gml:id " + cacheModel.getType() + " cache written to database.");

			} catch (SQLException sqlEx) {
				LOG.error("SQL error while writing gml:id " + cacheModel.getType() + " cache to database: " + sqlEx.getMessage());
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

	private GmlIdEntry lookupDB(String key) {
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
			return null;
		} 
	}
	
	public void shutdown() throws SQLException {
		cacheModel.close();
	}
}
