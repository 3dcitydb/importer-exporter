package de.tub.citydb.db.cache.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CacheTableSurfaceGeometry extends CacheTableModel {
	public static CacheTableSurfaceGeometry instance = null;
	
	private CacheTableSurfaceGeometry() {		
	}
	
	public synchronized static CacheTableSurfaceGeometry getInstance() {
		if (instance == null)
			instance = new CacheTableSurfaceGeometry();
		
		return instance;
	}

	@Override
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (PARENT_ID) " + properties);
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	@Override
	public CacheTableModelEnum getType() {
		return CacheTableModelEnum.SURFACE_GEOMETRY;
	}
	
	@Override
	protected String getColumns() {
		return "(ID NUMBER," +
		"PARENT_ID NUMBER, " +
		"ROOT_ID NUMBER, " +
		"REVERSE NUMBER(1,0), " +
		"GMLID VARCHAR2(256))";
	}

}
