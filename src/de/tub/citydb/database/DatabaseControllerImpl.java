/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import de.tub.citydb.api.controller.DatabaseController;
import de.tub.citydb.api.database.DatabaseAdapter;
import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.database.DatabaseConnectionDetails;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.DBConnection;

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
	public synchronized void connect(boolean showErrorDialog) throws DatabaseConfigurationException, SQLException {
		if (!dbPool.isConnected()) {
			viewHandler.commitConnectionDetails();
			viewHandler.printConnectionState(ConnectionStateEnum.INIT_CONNECT);

			try {
				dbPool.connect(config);
			} catch (DatabaseConfigurationException e) {
				viewHandler.printError(e, showErrorDialog);
				throw e;
			} catch (SQLException e) {
				viewHandler.printError(e, showErrorDialog);
				throw e;
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

}
