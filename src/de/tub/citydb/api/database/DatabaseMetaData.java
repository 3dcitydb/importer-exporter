package de.tub.citydb.api.database;

import de.tub.citydb.api.log.LogLevelType;

public interface DatabaseMetaData {	
	public String getDatabaseProductName();	
	public String getShortDatabaseProductVersion();
	public String getDatabaseProductVersion();
	public int getDatabaseMajorVersion();
	public int getDatabaseMinorVersion();
	public String getDatabaseProductString();
	public String getReferenceSystemName();
	public boolean isReferenceSystem3D();
	public int getSrid();
	public String getSrsName();
	public boolean isVersionEnabled();
	public void printToConsole(LogLevelType level);
}
