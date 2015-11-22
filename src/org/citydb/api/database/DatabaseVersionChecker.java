package org.citydb.api.database;

import java.util.List;

public interface DatabaseVersionChecker {
	public void checkVersionSupport(DatabaseAdapter databaseAdapter) throws DatabaseVersionException, DatabaseConnectionWarning;
	public List<DatabaseVersion> getSupportedVersions(String productName);
}
