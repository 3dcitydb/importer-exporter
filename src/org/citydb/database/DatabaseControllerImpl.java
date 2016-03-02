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
package org.citydb.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.citydb.api.controller.DatabaseController;
import org.citydb.api.database.DatabaseAdapter;
import org.citydb.api.database.DatabaseConfigurationException;
import org.citydb.api.database.DatabaseConnectionDetails;
import org.citydb.api.database.DatabaseConnectionWarning;
import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.database.DatabaseVersionChecker;
import org.citydb.api.database.DatabaseVersionException;
import org.citydb.config.Config;
import org.citydb.config.project.database.DBConnection;

public class DatabaseControllerImpl implements DatabaseController {
	private final Config config;
	private final DatabaseConnectionPool dbPool;

	private ConnectionViewHandler viewHandler;

	public DatabaseControllerImpl(Config config) {
		this.config = config;
		dbPool = DatabaseConnectionPool.getInstance();
	}	

	public void setConnectionViewHandler(ConnectionViewHandler viewHandler) {
		this.viewHandler = viewHandler;
	}

	@Override
	public synchronized void connect(boolean showErrorDialog) throws DatabaseConfigurationException, DatabaseVersionException, SQLException {
		if (!dbPool.isConnected()) {
			viewHandler.commitConnectionDetails();
			viewHandler.printConnectionState(ConnectionStateEnum.INIT_CONNECT);

			try {
				dbPool.connect(config);
			} catch (DatabaseConfigurationException e) {
				viewHandler.printError(e, showErrorDialog);
				throw e;
			} catch (DatabaseVersionException e) {
				viewHandler.printError(e, showErrorDialog);
				throw e;
			} catch (SQLException e) {
				viewHandler.printError(e, showErrorDialog);
				throw e;
			}

			// print connection warnings
			List<DatabaseConnectionWarning> warnings = dbPool.getActiveDatabaseAdapter().getConnectionWarnings();
			if (!warnings.isEmpty()) {
				for (DatabaseConnectionWarning warning : warnings)
					viewHandler.printWarning(warning, showErrorDialog);
			}			
			
			viewHandler.printConnectionState(ConnectionStateEnum.FINISH_CONNECT);
		}
	}

	@Override
	public void disconnect() {
		if (dbPool.isConnected()) {
			viewHandler.printConnectionState(ConnectionStateEnum.INIT_DISCONNECT);
			dbPool.disconnect();
			viewHandler.printConnectionState(ConnectionStateEnum.FINISH_DISCONNECT);
		}
	}

	public boolean isConnected() {
		return dbPool.isConnected();
	}

	@Override
	public Connection getConnection() throws SQLException {
		return dbPool.getConnection();
	}

	@Override
	public List<DatabaseConnectionDetails> getConnectionDetails() {
		ArrayList<DatabaseConnectionDetails> tmp = new ArrayList<DatabaseConnectionDetails>();
		for (DBConnection conn : config.getProject().getDatabase().getConnections())
			tmp.add(conn);

		return tmp;
	}

	@Override
	public List<DatabaseSrs> getDatabaseSrs() {
		return config.getProject().getDatabase().getReferenceSystems();
	}

	@Override
	public DatabaseAdapter getActiveDatabaseAdapter() {
		return dbPool.getActiveDatabaseAdapter();
	}

	@Override
	public DatabaseVersionChecker getDatabaseVersionChecker() {
		return dbPool.getDatabaseVersionChecker();
	}

}
