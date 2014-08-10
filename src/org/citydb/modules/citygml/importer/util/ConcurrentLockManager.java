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
