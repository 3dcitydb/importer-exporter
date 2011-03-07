package de.tub.citydb.db.cache.model;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CacheTableTextureParam extends CacheTableModel {
	public static CacheTableTextureParam instance = null;
	
	private CacheTableTextureParam() {		
	}
	
	public synchronized static CacheTableTextureParam getInstance() {
		if (instance == null)
			instance = new CacheTableTextureParam();
		
		return instance;
	}

	@Override
	public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
		Statement stmt = null;

		try {
			stmt = conn.createStatement();
			
			stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (TEXCOORDLIST_ID) " + properties);
			stmt.executeUpdate("create index idx2_" + tableName + " on " + tableName + " (GMLID) " + properties);
			stmt.executeUpdate("create index idx3_" + tableName + " on " + tableName + " (TYPE) " + properties);
		} finally {
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		}
	}

	@Override
	public CacheTableModelEnum getType() {
		return CacheTableModelEnum.TEXTUREPARAM;
	}
	
	@Override
	protected String getColumns() {
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
