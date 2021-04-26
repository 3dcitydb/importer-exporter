/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.database.adapter.oracle;

import org.citydb.config.project.database.Workspace;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.AbstractWorkspaceManagerAdapter;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class WorkspaceManagerAdapter extends AbstractWorkspaceManagerAdapter {
	private final String defaultWorkspaceName = "LIVE";
	private final SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

	protected WorkspaceManagerAdapter(AbstractDatabaseAdapter databaseAdapter) {
		super(databaseAdapter);
	}
	
	@Override
	public String getDefaultWorkspaceName() {
		return defaultWorkspaceName;
	}
	
	@Override
	public boolean equalsDefaultWorkspaceName(String workspaceName) {
		workspaceName = formatWorkspaceName(workspaceName);
		return workspaceName == null || workspaceName.isEmpty() || defaultWorkspaceName.equals(workspaceName);
	}

	@Override
	public void gotoWorkspace(Connection connection, Workspace workspace) throws SQLException {
		String workspaceName = formatWorkspaceName(workspace.getName());
		if (workspaceName == null || workspaceName.isEmpty()) {
			workspaceName = defaultWorkspaceName;
		}

		try (CallableStatement workspaceStmt = connection.prepareCall("{call dbms_wm.GotoWorkspace('" + workspaceName + "')}")) {
			workspaceStmt.executeQuery();
			if (workspace.isSetTimestamp()) {
				try (CallableStatement timestampStmt = connection.prepareCall("{call dbms_wm.GotoDate('" + format.format(workspace.getTimestamp()) + "', 'DD.MM.YYYY')}")) {
					timestampStmt.executeQuery();
				}
			}
		}
	}

	@Override
	public List<String> fetchWorkspacesFromDatabase(Connection connection) throws SQLException {
		try (Statement stmt = connection.createStatement();
			 ResultSet rs = stmt.executeQuery("select workspace from all_workspaces order by workspace")) {
			List<String> schemas = new ArrayList<>();
			while (rs.next()) {
				schemas.add(rs.getString(1));
			}

			return schemas;
		}
	}

	@Override
	public String formatWorkspaceName(String workspaceName) {
		return workspaceName != null ? workspaceName.trim() : null;
	}
}
