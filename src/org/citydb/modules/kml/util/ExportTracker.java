package org.citydb.modules.kml.util;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ExportTracker {
	private final ConcurrentHashMap<Long, CityObject4JSON> map;
	
	public ExportTracker() {
		map = new ConcurrentHashMap<Long, CityObject4JSON>();
	}
	
	public void put(long id, CityObject4JSON json) {
		map.put(id, json);
	}
	
	public boolean contains(long id) {
		return map.containsKey(id);
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
}
