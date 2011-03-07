package de.tub.citydb.db.cache;

import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import de.tub.citygml4j.model.citygml.CityGMLClass;

public interface DBCacheModel {
	public void drainToDB(ConcurrentHashMap<String, GmlIdEntry> map, int drain) throws SQLException;
	public GmlIdEntry lookupDB(String key) throws SQLException;
	public String lookupDB(long id, CityGMLClass type) throws SQLException;
}
