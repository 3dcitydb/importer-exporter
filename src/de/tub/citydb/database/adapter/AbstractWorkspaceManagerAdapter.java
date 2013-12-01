package de.tub.citydb.database.adapter;

import java.sql.Connection;
import java.sql.SQLException;

import de.tub.citydb.api.database.DatabaseWorkspaceManager;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.log.Logger;

public abstract class AbstractWorkspaceManagerAdapter implements DatabaseWorkspaceManager {
	private final Logger LOG = Logger.getInstance();
	protected final AbstractDatabaseAdapter databaseAdapter;

	protected AbstractWorkspaceManagerAdapter(AbstractDatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
	}

	public abstract boolean equalsDefaultWorkspaceName(String workspaceName);
	public abstract boolean gotoWorkspace(Connection connection, Workspace workspace);

	@Override
	public boolean existsWorkspace(String workspaceName) {
		return existsWorkspace(new Workspace(workspaceName), false);
	}

	@Override
	public boolean gotoWorkspace(Connection connection, String workspaceName, String timestamp) throws SQLException {
		return gotoWorkspace(connection, new Workspace(workspaceName, timestamp));
	}

	@Override
	public boolean gotoWorkspace(Connection connection, String workspaceName) throws SQLException {
		return gotoWorkspace(connection, workspaceName, null);
	}

	public boolean existsWorkspace(Workspace workspace, boolean logResult) {
		Connection conn = null;

		try {
			conn = databaseAdapter.connectionPool.getConnection();
			boolean exists = gotoWorkspace(conn, workspace);
			if (logResult) {
				if (!exists)
					LOG.error("Database workspace " + workspace + " is not available.");
				else 
					LOG.info("Switching to database workspace " + workspace + '.');
			}
			
			return exists;
		} catch (SQLException e) {
			return false;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					//;
				}

				conn = null;
			}
		}
	}

}
