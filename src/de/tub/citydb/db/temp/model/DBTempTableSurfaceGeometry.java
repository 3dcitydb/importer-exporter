package de.tub.citydb.db.temp.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTempTableSurfaceGeometry implements DBTempTableModel {
	public static DBTempTableSurfaceGeometry instance = null;
	
	private DBTempTableSurfaceGeometry() {		
	}
	
	public synchronized static DBTempTableSurfaceGeometry getInstance() {
		if (instance == null)
			instance = new DBTempTableSurfaceGeometry();
		
		return instance;
	}

	@Override
	public void createGTT(Connection conn, String tableName) throws SQLException {		
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("create global temporary table " + 
					tableName +
					getColumns() +
					"on commit preserve rows");
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	@Override
	public void createGTTIndexes(Connection conn, String tableName) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (PARENT_ID)");
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	@Override
	public void createHeap(Connection conn, String tableName) throws SQLException {		
		// nothing to do here
	}

	@Override
	public void createHeapIndexes(Connection conn, String tableName) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (PARENT_ID) nologging");
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	@Override
	public DBTempTableModelEnum getType() {
		return DBTempTableModelEnum.SURFACE_GEOMETRY;
	}
	
	private String getColumns() {
		return "(ID NUMBER," +
		"PARENT_ID NUMBER, " +
		"ROOT_ID NUMBER, " +
		"REVERSE NUMBER(1,0), " +
		"GMLID VARCHAR2(256))";
	}

}
