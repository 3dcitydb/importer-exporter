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
package org.citydb.database;

import org.citydb.config.Config;
import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.database.DatabaseConfigurationException;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.ConnectionManager;
import org.citydb.database.connection.ConnectionState;
import org.citydb.database.connection.ConnectionViewHandler;
import org.citydb.database.connection.DatabaseConnectionDetails;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.connection.DatabaseConnectionWarning;
import org.citydb.database.version.DatabaseVersionChecker;
import org.citydb.database.version.DatabaseVersionException;
import org.citydb.log.Logger;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseController implements ConnectionManager {
	private final Logger log = Logger.getInstance();
	private final Config config;
	private final DatabaseConnectionPool connectionPool;

	private ConnectionViewHandler viewHandler;

	public DatabaseController() {
		config = ObjectRegistry.getInstance().getConfig();
		connectionPool = DatabaseConnectionPool.getInstance();
	}	

	public void setConnectionViewHandler(ConnectionViewHandler viewHandler) {
		this.viewHandler = viewHandler;
	}

	public synchronized boolean connect() {
		return connect(false);
	}

	public synchronized boolean connect(boolean suppressDialog) {
		// request to commit connection details
		if (viewHandler != null) {
			viewHandler.commitConnectionDetails();
		}

		DBConnection connection = config.getProject().getDatabaseConfig().getActiveConnection();
		return connect(connection, suppressDialog);
	}

	public synchronized boolean connect(DBConnection connection) {
		return connect(connection, false);
	}

	public synchronized boolean connect(DBConnection connection, boolean suppressDialog) {
		if (!connectionPool.isConnected()) {
			if (connection == null) {
				log.error("Connection to database could not be established.");
				log.error("No valid database connection details provided in configuration.");
				return false;
			}

			try {
				log.info("Connecting to database '" + connection + "'.");
				showConnectionStatus(ConnectionState.INIT_CONNECT);

				// connect to database
				connectionPool.connect(connection);

				// show connection warnings
				for (DatabaseConnectionWarning warning : connectionPool.getActiveDatabaseAdapter().getConnectionWarnings()) {
					log.warn(warning.getMessage());
					boolean connect = suppressDialog || showWarning(warning);
					if (!connect) {
						log.warn("Database connection attempt aborted.");
						connectionPool.disconnect();
						return false;
					}
				}

				log.info("Database connection established.");
				connectionPool.getActiveDatabaseAdapter().getConnectionMetaData().printToConsole();

				// log unsupported user-defined SRSs
				for (DatabaseSrs refSys : config.getProject().getDatabaseConfig().getReferenceSystems()) {
					if (!refSys.isSupported()) {
						log.warn("Reference system '" + refSys.getDescription() +
								"' (SRID: " + refSys.getSrid() + ") is not supported.");
					}
				}
			} catch (DatabaseConfigurationException | SQLException e) {
				log.error("Connection to database could not be established.", e);
				showError(e, suppressDialog);
				return false;
			} catch (DatabaseVersionException e) {
				log.error("Connection to database could not be established.", e);
				log.error("Supported versions are '" + Util.collection2string(e.getSupportedVersions(), ", ") + "'.");
				showError(e, suppressDialog);
				return false;
			} finally {
				showConnectionStatus(ConnectionState.FINISH_CONNECT);
			}
		}

		return connectionPool.isConnected();
	}

	public void disconnect() {
		if (connectionPool.isConnected()) {
			showConnectionStatus(ConnectionState.INIT_DISCONNECT);
			connectionPool.disconnect();
			log.info("Disconnected from database.");
			showConnectionStatus(ConnectionState.FINISH_DISCONNECT);
		}
	}

	public boolean isConnected() {
		return connectionPool.isConnected();
	}

	public Connection getConnection() throws SQLException {
		return connectionPool.getConnection();
	}

	public List<DatabaseConnectionDetails> getConnectionDetails() {
		ArrayList<DatabaseConnectionDetails> result = new ArrayList<>();
		for (DBConnection connection : config.getProject().getDatabaseConfig().getConnections()) {
			result.add(new DatabaseConnectionDetails(connection));
		}

		return result;
	}

	public List<DatabaseSrs> getDatabaseSrs() {
		return config.getProject().getDatabaseConfig().getReferenceSystems();
	}

	public AbstractDatabaseAdapter getActiveDatabaseAdapter() {
		return connectionPool.getActiveDatabaseAdapter();
	}

	public DatabaseVersionChecker getDatabaseVersionChecker() {
		return connectionPool.getDatabaseVersionChecker();
	}

	private void showConnectionStatus(ConnectionState state) {
		if (viewHandler != null) {
			viewHandler.showConnectionStatus(state);
		}
	}

	private boolean showWarning(DatabaseConnectionWarning warning) {
		return viewHandler == null || viewHandler.showWarning(warning);
	}

	private void showError(Exception e, boolean suppressDialog) {
		if (!suppressDialog && viewHandler != null) {
			if (e instanceof DatabaseConfigurationException) {
				viewHandler.showError((DatabaseConfigurationException) e);
			} else if (e instanceof DatabaseVersionException) {
				viewHandler.showError((DatabaseVersionException) e);
			} else if (e instanceof SQLException) {
				viewHandler.showError((SQLException) e);
			}
		}
	}
}
