package de.tub.citydb.db.temp.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTempTableIdCache implements DBTempTableModel {
	private final DBTempTableModelEnum type;
	
	public DBTempTableIdCache(DBTempTableModelEnum type) {
		this.type = type;
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
			conn.commit();
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
			stmt.executeUpdate("create index idx2_" + tableName + " on " + tableName + " (ID)");
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
			stmt.executeUpdate("create index idx2_" + tableName + " on " + tableName + " (ID) nologging");
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	@Override
	public DBTempTableModelEnum getType() {
		return type;
	}
	
	private String getColumns() {
		return "(GMLID VARCHAR2(256), " +
		"ID NUMBER, " +
		"ROOT_ID NUMBER, " +
		"REVERSE NUMBER(1,0), " +
		"MAPPING VARCHAR2(256)," +
		"TYPE NUMBER(3))";
	}
}
