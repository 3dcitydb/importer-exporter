package de.tub.citydb.db.temp.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;


public class DBTempTableTextureParam implements DBTempTableModel {
	public static DBTempTableTextureParam instance = null;
	
	private DBTempTableTextureParam() {		
	}
	
	public synchronized static DBTempTableTextureParam getInstance() {
		if (instance == null)
			instance = new DBTempTableTextureParam();
		
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
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (TEXCOORDLIST_ID)");
			stmt.executeUpdate("create index idx2_" + tableName + " on " + tableName + " (GMLID)");
			stmt.executeUpdate("create index idx3_" + tableName + " on " + tableName + " (TYPE)");
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
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (TEXCOORDLIST_ID) nologging");
			stmt.executeUpdate("create index idx2_" + tableName + " on " + tableName + " (GMLID) nologging");
			stmt.executeUpdate("create index idx3_" + tableName + " on " + tableName + " (TYPE) nologging");
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	@Override
	public DBTempTableModelEnum getType() {
		return DBTempTableModelEnum.TEXTUREPARAM;
	}
	
	private String getColumns() {
		return "(ID NUMBER, " +
		"GMLID VARCHAR2(256), " +
		"TYPE NUMBER(3), " +
		"IS_TEXTURE_PARAMETERIZATION NUMBER(1,0), " +
		"TEXPARAM_GMLID VARCHAR2(256), " +
		"WORLD_TO_TEXTURE VARCHAR2(1000), " +
		"TEXTURE_COORDINATES VARCHAR2(4000), " +
		"TARGET_URI VARCHAR2(256), " +
		"TEXCOORDLIST_ID VARCHAR2(256))";
	}

}
