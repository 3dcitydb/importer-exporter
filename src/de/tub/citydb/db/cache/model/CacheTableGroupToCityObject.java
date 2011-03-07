package de.tub.citydb.db.cache.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CacheTableGroupToCityObject extends CacheTableModel {
	public static CacheTableGroupToCityObject instance = null;
	
	private CacheTableGroupToCityObject() {		
	}
	
	public synchronized static CacheTableGroupToCityObject getInstance() {
		if (instance == null)
			instance = new CacheTableGroupToCityObject();
		
		return instance;
	}

	@Override
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();	
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (GROUP_ID) " + properties);
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	@Override
	public CacheTableModelEnum getType() {
		return CacheTableModelEnum.GROUP_TO_CITYOBJECT;
	}
	
	@Override
	protected String getColumns() {
		return "(GROUP_ID NUMBER," +
		"GMLID VARCHAR2(256), " +
		"IS_PARENT NUMBER(1,0), " +
		"ROLE VARCHAR2(256))";
	}
}
