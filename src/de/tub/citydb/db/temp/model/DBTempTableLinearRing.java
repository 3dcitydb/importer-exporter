package de.tub.citydb.db.temp.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTempTableLinearRing implements DBTempTableModel {
	public static DBTempTableLinearRing instance = null;
	
	private DBTempTableLinearRing() {		
	}
	
	public synchronized static DBTempTableLinearRing getInstance() {
		if (instance == null)
			instance = new DBTempTableLinearRing();
		
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
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (GMLID)");
			stmt.executeUpdate("create index idx2_" + tableName + " on " + tableName + " (PARENT_GMLID)");
			stmt.executeUpdate("create index idx3_" + tableName + " on " + tableName + " (RING_NO)");
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	@Override
	public void createHeap(Connection conn, String tableName) throws SQLException {		
		// nothing to do here so far
	}

	@Override
	public void createHeapIndexes(Connection conn, String tableName) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (GMLID) nologging");
			stmt.executeUpdate("create index idx2_" + tableName + " on " + tableName + " (PARENT_GMLID) nologging");
			stmt.executeUpdate("create index idx3_" + tableName + " on " + tableName + " (RING_NO) nologging");
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	@Override
	public DBTempTableModelEnum getType() {
		return DBTempTableModelEnum.LINEAR_RING;
	}
	
	private String getColumns() {
		return "(GMLID VARCHAR2(256), " +
		"PARENT_GMLID VARCHAR2(256), " +
		"RING_NO NUMBER)"; 
	}
}
