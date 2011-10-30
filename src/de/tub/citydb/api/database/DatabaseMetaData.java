package de.tub.citydb.api.database;

import de.tub.citydb.api.config.DatabaseSrs;
import de.tub.citydb.api.log.LogLevel;

public interface DatabaseMetaData {	
	public String getDatabaseProductName();	
	public String getShortDatabaseProductVersion();
	public String getDatabaseProductVersion();
	public int getDatabaseMajorVersion();
	public int getDatabaseMinorVersion();
	public String getDatabaseProductString();
	public DatabaseSrs getReferenceSystem();
	public boolean isVersionEnabled();
	public void printToConsole(LogLevel level);
}
