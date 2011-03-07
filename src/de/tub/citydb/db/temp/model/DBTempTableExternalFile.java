package de.tub.citydb.db.temp.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTempTableExternalFile implements DBTempTableModel {
	public static DBTempTableExternalFile instance = null;
	
	private DBTempTableExternalFile() {		
	}
	
	public synchronized static DBTempTableExternalFile getInstance() {
		if (instance == null)
			instance = new DBTempTableExternalFile();
		
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
		// nothing to do here so far
	}

	@Override
	public DBTempTableModelEnum getType() {
		return DBTempTableModelEnum.EXTERNAL_FILE;
	}
	
	private String getColumns() {
		return "(ID NUMBER, " +
		"FILE_URI VARCHAR2(1000), " +
		"TYPE NUMBER(3))";
	}
}
