package de.tub.citydb.api.database;

public interface DatabaseSrs {
	public int getSrid();
	public String getSrsName();
	public String getDescription();
	public boolean isSupported();
}
