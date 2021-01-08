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
package org.citydb.database.adapter;

import org.citydb.config.project.database.Workspace;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public abstract class AbstractWorkspaceManagerAdapter {
	protected final AbstractDatabaseAdapter databaseAdapter;

	protected AbstractWorkspaceManagerAdapter(AbstractDatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
	}

	public abstract String getDefaultWorkspaceName();
	public abstract boolean equalsDefaultWorkspaceName(String workspaceName);
	protected abstract boolean changeWorkspace(Connection connection, Workspace workspace);

	public void checkWorkspace(Workspace workspace) throws SQLException {
		if (workspace != null) {
			try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
				if (!gotoWorkspace(conn, workspace)) {
					throw new SQLException("The database workspace " + workspace + " is not available.");
				}
			}
		}
	}

	public boolean gotoWorkspace(Connection connection, Workspace workspace) {
		return workspace == null || changeWorkspace(connection, workspace);
	}

	public boolean gotoWorkspace(Connection connection, String workspaceName, Date timestamp) {
		return gotoWorkspace(connection, new Workspace(workspaceName, timestamp));
	}

	public boolean gotoWorkspace(Connection connection, String workspaceName) {
		return gotoWorkspace(connection, workspaceName, null);
	}
}
