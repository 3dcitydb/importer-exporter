package de.tub.citydb.db.cache.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

public class CacheTableGmlId extends CacheTableModel {
	private static HashMap<CacheTableModelEnum, CacheTableGmlId> cacheTableMap;
	private final CacheTableModelEnum type;
	
	private CacheTableGmlId(CacheTableModelEnum type) {
		// just to thwart instantiation
		this.type = type;
	}
	
	public synchronized static CacheTableGmlId getInstance(CacheTableModelEnum type) {
		switch (type) {
		case GMLID_FEATURE:
		case GMLID_GEOMETRY:
			break;
		default:
			throw new IllegalArgumentException("Unsupported cache table type " + type);
		}
		
		if (cacheTableMap == null)
			cacheTableMap = new HashMap<CacheTableModelEnum, CacheTableGmlId>();
		
		CacheTableGmlId cacheTable = cacheTableMap.get(type);
		if (cacheTable == null) {
			cacheTable = new CacheTableGmlId(type);
			cacheTableMap.put(type, cacheTable);
		}
		
		return cacheTable;
	}
	
	@Override
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (GMLID) " + properties);
			stmt.executeUpdate("create index idx2_" + tableName + " on " + tableName + " (ID) " + properties);
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	@Override
	public CacheTableModelEnum getType() {
		return type;
	}
	
	@Override
	protected String getColumns() {
		return "(GMLID VARCHAR2(256), " +
		"ID NUMBER, " +
		"ROOT_ID NUMBER, " +
		"REVERSE NUMBER(1,0), " +
		"MAPPING VARCHAR2(256)," +
		"TYPE NUMBER(3))";
	}
}
