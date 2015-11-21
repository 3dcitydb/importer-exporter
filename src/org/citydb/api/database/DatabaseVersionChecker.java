package org.citydb.api.database;

public interface DatabaseVersionChecker {
	public void checkIfSupported(DatabaseVersion version, String productName) throws DatabaseVersionException;
	public void checkIfOutdated(DatabaseVersion version, String productName) throws DatabaseConnectionWarning;
}
