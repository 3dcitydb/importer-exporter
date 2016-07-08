/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
