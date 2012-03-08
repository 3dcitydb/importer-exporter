package de.tub.citydb.api.database;


public interface DatabaseMetaData {	
	public String getDatabaseProductName();	
	public String getShortDatabaseProductVersion();
	public String getDatabaseProductVersion();
	public int getDatabaseMajorVersion();
	public int getDatabaseMinorVersion();
	public String getDatabaseProductString();
	public DatabaseSrs getReferenceSystem();
//	public boolean isVersionEnabled();
	public void printToConsole();
}
