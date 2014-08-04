package org.citydb.api.database;


public interface DatabaseAdapter {
	public DatabaseType getDatabaseType();
	public boolean hasVersioningSupport();
	
	public DatabaseConnectionDetails getConnectionDetails();
	public DatabaseMetaData getConnectionMetaData();
	public DatabaseWorkspaceManager getWorkspaceManager();
	public DatabaseGeometryConverter getGeometryConverter();
	public DatabaseUtil getUtil();
	public BalloonTemplateFactory getBalloonTemplateFactory();
}
