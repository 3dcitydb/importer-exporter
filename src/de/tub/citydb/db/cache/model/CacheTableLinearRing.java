package de.tub.citydb.db.cache.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CacheTableLinearRing extends CacheTableModel {
	public static CacheTableLinearRing instance = null;
	
	private CacheTableLinearRing() {		
	}
	
	public synchronized static CacheTableLinearRing getInstance() {
		if (instance == null)
			instance = new CacheTableLinearRing();
		
		return instance;
	}

	@Override
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (GMLID) " + properties);
			stmt.executeUpdate("create index idx2_" + tableName + " on " + tableName + " (PARENT_GMLID) " + properties);
			stmt.executeUpdate("create index idx3_" + tableName + " on " + tableName + " (RING_NO) " + properties);
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	@Override
	public CacheTableModelEnum getType() {
		return CacheTableModelEnum.LINEAR_RING;
	}
	
	@Override
	protected String getColumns() {
		return "(GMLID VARCHAR2(256), " +
		"PARENT_GMLID VARCHAR2(256), " +
		"RING_NO NUMBER)"; 
	}
}
