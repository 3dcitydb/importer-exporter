package de.tub.citydb.db.cache.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CacheTableTextureAssociation extends CacheTableModel {
	public static CacheTableTextureAssociation instance = null;
	
	private CacheTableTextureAssociation() {		
	}
	
	public synchronized static CacheTableTextureAssociation getInstance() {
		if (instance == null)
			instance = new CacheTableTextureAssociation();
		
		return instance;
	}

	@Override
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (GMLID) " + properties);
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	@Override
	public CacheTableModelEnum getType() {
		return CacheTableModelEnum.TEXTUREASSOCIATION;
	}
	
	@Override
	protected String getColumns() {
		return "(SURFACE_DATA_ID NUMBER, " +
		"SURFACE_GEOMETRY_ID NUMBER, " +
		"GMLID VARCHAR2(256))";
	}

}
