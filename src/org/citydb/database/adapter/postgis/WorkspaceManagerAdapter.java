package org.citydb.database.adapter.postgis;

import java.sql.Connection;

import org.citydb.config.project.database.Workspace;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.AbstractWorkspaceManagerAdapter;

public class WorkspaceManagerAdapter extends AbstractWorkspaceManagerAdapter {
	
	protected WorkspaceManagerAdapter(AbstractDatabaseAdapter databaseAdapter) {
		super(databaseAdapter);
	}

	@Override
	public String getDefaultWorkspaceName() {
		return null;
	}

	@Override
	public boolean equalsDefaultWorkspaceName(String workspaceName) {
		return false;
	}

	@Override
	public boolean gotoWorkspace(Connection connection, Workspace workspace) {
		return false;
	}

}
