package de.tub.citydb.plugin.api.controller;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseController {
	public boolean connect();
	public boolean disconnect();
	public boolean isConnected();

	public Connection getConnection() throws SQLException;
	public boolean existsWorkspace(String workspaceName);
	public boolean gotoWorkspace(Connection conn, String workspaceName) throws SQLException;
	public boolean gotoWorkspace(Connection conn, String workspaceName, String timestamp) throws SQLException;
}
