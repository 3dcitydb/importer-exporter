package org.citydb.api.database;

import java.util.List;

public interface DatabaseVersionChecker {
	public List<DatabaseConnectionWarning> checkVersionSupport(DatabaseAdapter databaseAdapter) throws DatabaseVersionException;
	public List<DatabaseVersion> getSupportedVersions(String productName);
}
