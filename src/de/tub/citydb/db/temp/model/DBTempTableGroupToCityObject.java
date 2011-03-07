package de.tub.citydb.db.temp.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTempTableGroupToCityObject implements DBTempTableModel {
	public static DBTempTableGroupToCityObject instance = null;
	
	private DBTempTableGroupToCityObject() {		
	}
	
	public synchronized static DBTempTableGroupToCityObject getInstance() {
		if (instance == null)
			instance = new DBTempTableGroupToCityObject();
		
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
		// nothing to do here so far
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
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (GROUP_ID) nologging");
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	@Override
	public DBTempTableModelEnum getType() {
		return DBTempTableModelEnum.GROUP_TO_CITYOBJECT;
	}
	
	private String getColumns() {
		return "(GROUP_ID NUMBER," +
		"GMLID VARCHAR2(256), " +
		"IS_PARENT NUMBER(1,0), " +
		"ROLE VARCHAR2(256))";
	}
}
