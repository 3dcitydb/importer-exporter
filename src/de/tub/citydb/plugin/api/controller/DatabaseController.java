package de.tub.citydb.plugin.api.controller;

import java.sql.Connection;
import java.sql.SQLException;

import de.tub.citydb.plugin.api.data.database.DatabaseConnectionDetails;
import de.tub.citydb.plugin.api.exception.DatabaseConfigurationException;
import de.tub.citydb.plugin.api.listener.DatabaseConnectionListener;

public interface DatabaseController {
	public void connect() throws DatabaseConfigurationException, SQLException;
	public void disconnect() throws SQLException;
	public boolean isConnected();

	public DatabaseConnectionDetails getActiveConnectionDetails();
	public Connection getConnection() throws SQLException;
	public boolean existsWorkspace(String workspaceName);
	public boolean gotoWorkspace(Connection conn, String workspaceName) throws SQLException;
	public boolean gotoWorkspace(Connection conn, String workspaceName, String timestamp) throws SQLException;
	
	public void addDatabaseConnectionListener(DatabaseConnectionListener listener);
	public void removeDatabaseConnectionListener(DatabaseConnectionListener listener);
}
