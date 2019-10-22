/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
import java.sql.SQLException;
import java.text.SimpleDateFormat;

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
		return (workspaceName == null || workspaceName.trim().isEmpty() || defaultWorkspaceName.equalsIgnoreCase(workspaceName.trim()));
	}

	@Override
	public boolean gotoWorkspace(Connection connection, Workspace workspace) {
		String workspaceName = workspace.getName();
		if (workspaceName == null || workspaceName.trim().isEmpty() || defaultWorkspaceName.equalsIgnoreCase(workspaceName))
			workspaceName = defaultWorkspaceName;

		try (CallableStatement workspaceStmt = connection.prepareCall("{call dbms_wm.GotoWorkspace('" + workspaceName + "')}")) {
			workspaceStmt.executeQuery();
			if (workspace.isSetTimestamp()) {
				try (CallableStatement timestampStmt = connection.prepareCall("{call dbms_wm.GotoDate('" + format.format(workspace.getTimestamp()) + "', 'DD.MM.YYYY')}")) {
					timestampStmt.executeQuery();
				}
			}

			return true;
		} catch (SQLException e) {
			return false;
		}
	}

}
