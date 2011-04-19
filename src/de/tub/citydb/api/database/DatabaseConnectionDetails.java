package de.tub.citydb.api.database;

public interface DatabaseConnectionDetails {
	public String getDescription();
	public String getServer();
	public Integer getPort();
	public String getSid();
	public String getUser();
	public DatabaseMetaData getMetaData();
}
