/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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

import javax.swing.JOptionPane;

import org.citydb.config.Config;
import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.ConnectionState;
import org.citydb.database.connection.ConnectionViewHandler;
import org.citydb.config.project.database.DatabaseConfigurationException;
import org.citydb.database.connection.DatabaseConnectionDetails;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.connection.DatabaseConnectionWarning;
import org.citydb.database.version.DatabaseVersionChecker;
import org.citydb.database.version.DatabaseVersionException;
import org.citydb.log.Logger;

public class DatabaseController {
	private final Logger log = Logger.getInstance();
	private final Config config;
	private final DatabaseConnectionPool dbPool;

	private ConnectionViewHandler viewHandler;

	public DatabaseController(Config config) {
		this.config = config;
		dbPool = DatabaseConnectionPool.getInstance();
	}	

	public void setConnectionViewHandler(ConnectionViewHandler viewHandler) {
		this.viewHandler = viewHandler;
	}

	public synchronized boolean connect(boolean showErrorDialog) throws DatabaseConfigurationException, DatabaseVersionException, SQLException {
		if (!dbPool.isConnected()) {
			viewHandler.commitConnectionDetails();
			viewHandler.printConnectionState(ConnectionState.INIT_CONNECT);

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
				for (DatabaseConnectionWarning warning : warnings) {
					int option = viewHandler.printWarning(warning, showErrorDialog);
					if (option == JOptionPane.CANCEL_OPTION) {
						log.warn("Database connection attempt aborted.");
						dbPool.disconnect();
					}
				}
			}			
			
			viewHandler.printConnectionState(ConnectionState.FINISH_CONNECT);
		}
		
		return dbPool.isConnected();
	}

	public void disconnect() {
		if (dbPool.isConnected()) {
			viewHandler.printConnectionState(ConnectionState.INIT_DISCONNECT);
			dbPool.disconnect();
			viewHandler.printConnectionState(ConnectionState.FINISH_DISCONNECT);
		}
	}

	public boolean isConnected() {
		return dbPool.isConnected();
	}

	public Connection getConnection() throws SQLException {
		return dbPool.getConnection();
	}

	public List<DatabaseConnectionDetails> getConnectionDetails() {
		ArrayList<DatabaseConnectionDetails> tmp = new ArrayList<DatabaseConnectionDetails>();
		for (DBConnection conn : config.getProject().getDatabase().getConnections())
			tmp.add(new DatabaseConnectionDetails(conn));

		return tmp;
	}

	public List<DatabaseSrs> getDatabaseSrs() {
		return config.getProject().getDatabase().getReferenceSystems();
	}

	public AbstractDatabaseAdapter getActiveDatabaseAdapter() {
		return dbPool.getActiveDatabaseAdapter();
	}

	public DatabaseVersionChecker getDatabaseVersionChecker() {
		return dbPool.getDatabaseVersionChecker();
	}

}
