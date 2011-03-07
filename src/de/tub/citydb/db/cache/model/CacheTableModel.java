package de.tub.citydb.db.cache.model;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class CacheTableModel {

	public void create(Connection conn, String tableName, CacheTableType type) throws SQLException {
		switch (type) {
		case GLOBAL_TEMPORARY_TABLE:
			createGlobalTemporaryTable(conn, tableName);
			break;
		case HEAP_TABLE:
			createHeapTable(conn, tableName);
			break;
		default:
			throw new IllegalArgumentException("Unsupported cache table type: " + type);
		}
	}

	private void createGlobalTemporaryTable(Connection conn, String tableName) throws SQLException {		
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

	private void createHeapTable(Connection conn, String tableName) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("create table " + 
					tableName + 
					getColumns());
			conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	public void createAsSelectFrom(Connection conn, String tableName, String sourceTableName) throws SQLException {
		Statement stmt = null;
		
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("create table " + 
					tableName +
					" nologging" +
					" as select * from " + 
					sourceTableName);
			conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	public long size(Connection conn, String tableName) throws SQLException {
		Statement stmt = null;
		ResultSet rs = null;
		long count = -1;

		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("select count(*) from " + tableName);

			if (rs.next())
				count = rs.getLong(1);

		} finally {
			if (rs != null) {
				rs.close();
				rs = null;
			}

			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}

		return count;
	}

	public void truncate(Connection conn, String tableName) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("truncate table " + tableName);
			conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	public void drop(Connection conn, String tableName) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			stmt.executeUpdate("drop table " + tableName);
			conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	public void createIndexes(Connection conn, String tableName) throws SQLException {
		createIndexes(conn, tableName, "");
	}
	
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		// override in subclasses if necessary
	}
	
	public abstract CacheTableModelEnum getType();
	protected abstract String getColumns();
}
