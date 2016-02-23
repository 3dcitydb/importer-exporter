/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.database.adapter;

import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.api.database.DatabaseWorkspaceManager;
import org.citydb.config.project.database.Workspace;
import org.citydb.log.Logger;

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
