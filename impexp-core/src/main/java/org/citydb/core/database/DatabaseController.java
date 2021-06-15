/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.database;

import org.citydb.config.Config;
import org.citydb.config.project.database.DatabaseConfigurationException;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.connection.ConnectionManager;
import org.citydb.core.database.connection.ConnectionState;
import org.citydb.core.database.connection.ConnectionViewHandler;
import org.citydb.core.database.connection.DatabaseConnectionDetails;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.connection.DatabaseConnectionWarning;
import org.citydb.core.database.version.DatabaseVersionChecker;
import org.citydb.core.database.version.DatabaseVersionException;
import org.citydb.core.log.Logger;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.Util;

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

		DatabaseConnection connection = config.getDatabaseConfig().getActiveConnection();
		return connect(connection, suppressDialog);
	}

	public synchronized boolean connect(DatabaseConnection connection) {
		config.getDatabaseConfig().setActiveConnection(connection);
		return connect(connection, false);
	}

	public synchronized boolean connect(DatabaseConnection connection, boolean suppressDialog) {
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
				for (DatabaseSrs refSys : config.getDatabaseConfig().getReferenceSystems()) {
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
		disconnect(false);
	}

	public void disconnect(boolean suppressLogMessages) {
		if (connectionPool.isConnected()) {
			showConnectionStatus(ConnectionState.INIT_DISCONNECT);
			connectionPool.disconnect();
			showConnectionStatus(ConnectionState.FINISH_DISCONNECT);

			if (!suppressLogMessages) {
				log.info("Disconnected from database.");
			}
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
		for (DatabaseConnection connection : config.getDatabaseConfig().getConnections()) {
			result.add(new DatabaseConnectionDetails(connection));
		}

		return result;
	}

	public List<DatabaseSrs> getDatabaseSrs() {
		return config.getDatabaseConfig().getReferenceSystems();
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
