package de.tub.citydb.db.temp.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DBTempTableTextureAssociation implements DBTempTableModel {
	public static DBTempTableTextureAssociation instance = null;
	
	private DBTempTableTextureAssociation() {		
	}
	
	public synchronized static DBTempTableTextureAssociation getInstance() {
		if (instance == null)
			instance = new DBTempTableTextureAssociation();
		
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
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}
	
	@Override
	public DBTempTableModelEnum getType() {
		return DBTempTableModelEnum.TEXTUREASSOCIATION;
	}
	
	private String getColumns() {
		return "(SURFACE_DATA_ID NUMBER, " +
		"SURFACE_GEOMETRY_ID NUMBER, " +
		"GMLID VARCHAR2(256))";
	}

}
