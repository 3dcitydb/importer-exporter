package de.tub.citydb.api.database;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseWorkspaceManager {
	public String getDefaultWorkspaceName();
	public boolean existsWorkspace(String workspaceName);
	public boolean gotoWorkspace(Connection conn, String workspaceName) throws SQLException;
	public boolean gotoWorkspace(Connection conn, String workspaceName, String timestamp) throws SQLException;
}
