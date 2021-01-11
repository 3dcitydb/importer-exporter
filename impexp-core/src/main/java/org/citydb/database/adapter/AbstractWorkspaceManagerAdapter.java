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

import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.database.Workspace;
import org.citydb.log.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public abstract class AbstractWorkspaceManagerAdapter {
	private final Logger log = Logger.getInstance();
	protected final AbstractDatabaseAdapter databaseAdapter;

	protected AbstractWorkspaceManagerAdapter(AbstractDatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
	}

	public abstract String getDefaultWorkspaceName();
	public abstract boolean equalsDefaultWorkspaceName(String workspaceName);
	public abstract boolean gotoWorkspace(Connection connection, Workspace workspace);
	public abstract List<String> fetchWorkspacesFromDatabase(Connection connection) throws SQLException;
	public abstract String formatWorkspaceName(String workspaceName);

	public boolean existsWorkspace(Workspace workspace) {
		return existsWorkspace(workspace, false);
	}

	public boolean existsWorkspace(Workspace workspace, boolean logResult) {
		try (Connection conn = databaseAdapter.connectionPool.getConnection()) {
			boolean exists = gotoWorkspace(conn, workspace);
			if (logResult) {
				if (!exists) {
					log.error("The database workspace '" + workspace + "' does not exist.");
				} else {
					log.info("Switching to database workspace '" + workspace + "'.");
				}
			}

			return exists;
		} catch (SQLException e) {
			return false;
		}
	}

	public boolean gotoWorkspace(Connection connection, String workspaceName, Date timestamp) {
		return gotoWorkspace(connection, new Workspace(workspaceName, timestamp));
	}

	public boolean gotoWorkspace(Connection connection, String workspaceName) {
		return gotoWorkspace(connection, workspaceName, null);
	}

	public List<String> fetchWorkspacesFromDatabase(DatabaseConnection databaseConnection) throws SQLException {
		Properties properties = new Properties();
		properties.setProperty("user", databaseConnection.getUser());
		properties.setProperty("password", databaseConnection.getPassword());

		try (Connection conn = DriverManager.getConnection(databaseAdapter.getJDBCUrl(
				databaseConnection.getServer(), databaseConnection.getPort(), databaseConnection.getSid()), properties)) {
			return fetchWorkspacesFromDatabase(conn);
		}
	}
}
