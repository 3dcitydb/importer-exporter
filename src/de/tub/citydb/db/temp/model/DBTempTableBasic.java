package de.tub.citydb.db.temp.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;



public class DBTempTableBasic implements DBTempTableModel {
	public static DBTempTableBasic instance = null;

	private DBTempTableBasic() {		
	}

	public synchronized static DBTempTableBasic getInstance() {
		if (instance == null)
			instance = new DBTempTableBasic();

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
		return DBTempTableModelEnum.BASIC;
	}

	private String getColumns() {
		return "(ID NUMBER, FROM_TABLE NUMBER(3), " +
				"GMLID VARCHAR2(256), " +
				"TO_TABLE NUMBER(3), " +
				"ATTRNAME VARCHAR(50))";
	}

}
