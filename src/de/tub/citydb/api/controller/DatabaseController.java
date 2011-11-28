package de.tub.citydb.api.controller;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import de.tub.citydb.api.config.DatabaseSrs;
import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.database.DatabaseConnectionDetails;

public interface DatabaseController {
	public void connect(boolean showErrorDialog) throws DatabaseConfigurationException, SQLException;
	public void disconnect(boolean showErrorDialog) throws SQLException;
	public void forceDisconnect();
	public boolean isConnected();

	public DatabaseConnectionDetails getActiveConnectionDetails();
	public List<DatabaseConnectionDetails> getConnectionDetails();
	public List<DatabaseSrs> getDatabaseSrs();
	
	public Connection getConnection() throws SQLException;
	public boolean isIndexEnabled(String tableName, String columnName) throws SQLException;
	
	public boolean existsWorkspace(String workspaceName);
	public boolean gotoWorkspace(Connection conn, String workspaceName) throws SQLException;
	public boolean gotoWorkspace(Connection conn, String workspaceName, String timestamp) throws SQLException;
}
