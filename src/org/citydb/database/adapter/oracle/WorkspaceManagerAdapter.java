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
package org.citydb.database.adapter.oracle;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.config.project.database.Workspace;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.AbstractWorkspaceManagerAdapter;

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
