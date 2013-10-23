package de.tub.citydb.database.adapter.oracle;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.database.adapter.AbstractDatabaseAdapter;
import de.tub.citydb.database.adapter.AbstractWorkspaceManagerAdapter;

public class WorkspaceManagerAdapter extends AbstractWorkspaceManagerAdapter {
	private final String defaultWorkspaceName = "LIVE";

	protected WorkspaceManagerAdapter(AbstractDatabaseAdapter databaseAdapter) {
		super(databaseAdapter);
	}
	
	@Override
	public String getDefaultWorkspaceName() {
		return defaultWorkspaceName;
	}
	
	@Override
	public boolean equalsDefaultWorkspaceName(String workspaceName) {
		return (workspaceName == null || workspaceName.trim().length() == 0 || defaultWorkspaceName.equals(workspaceName.trim().toUpperCase()));
	}

	@Override
	public boolean gotoWorkspace(Connection connection, Workspace workspace) {
		String workspaceName = workspace.getName();		
		if (workspaceName == null)
			throw new IllegalArgumentException("Workspace name may not be null.");

		String timestamp = workspace.getTimestamp();
		if (timestamp == null)
			throw new IllegalArgumentException("Workspace timestamp name may not be null.");

		workspaceName = workspaceName.trim();
		timestamp = timestamp.trim();
		CallableStatement stmt = null;

		if (!workspaceName.equals(defaultWorkspaceName) && (workspaceName.length() == 0 || workspaceName.toUpperCase().equals(defaultWorkspaceName)))
			workspaceName = defaultWorkspaceName;

		try {
			stmt = connection.prepareCall("{call dbms_wm.GotoWorkspace('" + workspaceName + "')}");
			stmt.executeQuery();

			if (timestamp.length() > 0) {
				stmt.close();
				stmt = connection.prepareCall("{call dbms_wm.GotoDate('" + timestamp + "', 'DD.MM.YYYY')}");
				stmt.executeQuery();
			}

			return true;
		} catch (SQLException e) {
			return false;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					//
				}

				stmt = null;
			}
		}
	}

}
