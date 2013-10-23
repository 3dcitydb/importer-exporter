package de.tub.citydb.api.database;

public interface DatabaseAdapter {
	public DatabaseType getDatabaseType();
	public boolean hasVersioningSupport();
	
	public boolean requiresPseudoTableInSelect();
	public String getPseudoTableName();
	
	public DatabaseConnectionDetails getConnectionDetails();
	public DatabaseMetaData getConnectionMetaData();
	public DatabaseWorkspaceManager getWorkspaceManager();
	public DatabaseGeometryConverter getGeometryConverter();
	public DatabaseUtil getUtil();
	public BalloonTemplateFactory getBalloonTemplateFactory();
}
