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
package org.citydb.database.connection;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DatabaseConfig;
import org.citydb.config.project.database.DatabaseConfigurationException;
import org.citydb.config.project.database.DatabaseConnection;
import org.citydb.config.project.database.DatabaseSrs;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.DatabaseAdapterFactory;
import org.citydb.database.connection.DatabaseConnectionWarning.ConnectionWarningType;
import org.citydb.database.version.DatabaseVersionChecker;
import org.citydb.database.version.DatabaseVersionException;
import org.citydb.database.version.DefaultDatabaseVersionChecker;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.DatabaseConnectionStateEvent;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.Util;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseConnectionPool implements ConnectionManager {
	private static DatabaseConnectionPool instance;
	private final int DEFAULT_LOGIN_TIMEOUT = 60;

	private final String poolName = DefaultGMLIdManager.getInstance().generateUUID();
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private final ADEExtensionManager adeManager;
	private AbstractDatabaseAdapter databaseAdapter;
	private DataSource dataSource;
	private DatabaseVersionChecker versionChecker;

	private DatabaseConnectionPool() {
		// just to thwart instantiation
		config = ObjectRegistry.getInstance().getConfig();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		adeManager = ADEExtensionManager.getInstance();
		versionChecker = new DefaultDatabaseVersionChecker();
	}

	public static synchronized DatabaseConnectionPool getInstance() {
		if (instance == null) {
			instance = new DatabaseConnectionPool();
		}

		return instance;
	}

	public synchronized void connect(DatabaseConnection connection) throws DatabaseConfigurationException, DatabaseVersionException, SQLException {
		// check valid connection details
		connection.validate();

		// disconnect if we are currently connected to another database
		if (isConnected()) {
			disconnect();
		}

		// get database adapter
		databaseAdapter = DatabaseAdapterFactory.getInstance().createDatabaseAdapter(connection.getDatabaseType());

		// general pool properties
		PoolProperties properties = new PoolProperties();
		properties.setUrl(databaseAdapter.getJDBCUrl(connection.getServer(), connection.getPort(), connection.getSid()));
		properties.setDriverClassName(databaseAdapter.getConnectionFactoryClassName());
		properties.setUsername(connection.getUser());
		properties.setPassword(connection.getPassword());
		properties.setName(poolName);
		properties.setDefaultAutoCommit(true);

		// set user-definable pool properties	
		if (connection.getMaxActive() != null) properties.setMaxActive(connection.getMaxActive());
		if (connection.getMaxIdle() != null) properties.setMaxIdle(connection.getMaxIdle());
		if (connection.getMinIdle() != null) properties.setMinIdle(connection.getMinIdle());
		if (connection.getInitialSize() != null) properties.setInitialSize(connection.getInitialSize());
		if (connection.getMaxWait() != null) properties.setMaxWait(connection.getMaxWait());
		if (connection.getTestOnBorrow() != null) properties.setTestOnBorrow(connection.getTestOnBorrow());
		if (connection.getTestOnReturn() != null) properties.setTestOnReturn(connection.getTestOnReturn());
		if (connection.getTestWhileIdle() != null) properties.setTestWhileIdle(connection.getTestWhileIdle());
		if (connection.getValidationQuery() != null) properties.setValidationQuery(connection.getValidationQuery());
		if (connection.getValidatorClassName() != null) properties.setValidatorClassName(connection.getValidatorClassName());
		if (connection.getTimeBetweenEvictionRunsMillis() != null) properties.setTimeBetweenEvictionRunsMillis(connection.getTimeBetweenEvictionRunsMillis());
		if (connection.getNumTestsPerEvictionRun() != null) properties.setNumTestsPerEvictionRun(connection.getNumTestsPerEvictionRun());
		if (connection.getMinEvictableIdleTimeMillis() != null) properties.setMinEvictableIdleTimeMillis(connection.getMinEvictableIdleTimeMillis());
		if (connection.getRemoveAbandoned() != null) properties.setRemoveAbandoned(connection.getRemoveAbandoned());
		if (connection.getRemoveAbandonedTimeout() != null) properties.setRemoveAbandonedTimeout(connection.getRemoveAbandonedTimeout());
		if (connection.getLogAbandoned() != null) properties.setLogAbandoned(connection.getLogAbandoned());
		if (connection.getConnectionProperties() != null) properties.setConnectionProperties(connection.getConnectionProperties());
		if (connection.getInitSQL() != null) properties.setInitSQL(connection.getInitSQL());
		if (connection.getValidationInterval() != null) properties.setValidationInterval(connection.getValidationInterval());
		if (connection.getJmxEnabled() != null) properties.setJmxEnabled(connection.getJmxEnabled());
		if (connection.getFairQueue() != null) properties.setFairQueue(connection.getFairQueue());
		if (connection.getAbandonWhenPercentageFull() != null) properties.setAbandonWhenPercentageFull(connection.getAbandonWhenPercentageFull());
		if (connection.getMaxAge() != null) properties.setMaxAge(connection.getMaxAge());
		if (connection.getUseEquals() != null) properties.setUseEquals(connection.getUseEquals());
		if (connection.getSuspectTimeout() != null) properties.setSuspectTimeout(connection.getSuspectTimeout());

		// pool maintenance
		properties.setJdbcInterceptors("StatementFinalizer");

		// create new data source
		dataSource = new DataSource(properties);
		dataSource.setLoginTimeout(connection.isSetLoginTimeout() ?
				connection.getLoginTimeout() :
				DEFAULT_LOGIN_TIMEOUT);

		try {
			// create connection pool
			dataSource.createPool();
						
			// set connection details
			DatabaseConnectionDetails connectionDetails = new DatabaseConnectionDetails(connection);
			databaseAdapter.setConnectionDetails(connectionDetails);

			// check workspace
			if (databaseAdapter.hasVersioningSupport()) {
				if (connectionDetails.isSetWorkspace()) {
					Workspace workspace = connectionDetails.getWorkspace();
					workspace.setName(databaseAdapter.getWorkspaceManager().formatWorkspaceName(workspace.getName()));
					if (!databaseAdapter.getWorkspaceManager().existsWorkspace(workspace)) {
						throw new SQLException("The database workspace '" + workspace + "' does not exist.");
					}
				} else {
					String name = databaseAdapter.getWorkspaceManager().getDefaultWorkspaceName();
					connectionDetails.setWorkspace(new Workspace(name));
				}
			}

			// check database schema
			if (connectionDetails.isSetSchema()) {
				connectionDetails.setSchema(databaseAdapter.getSchemaManager().formatSchema(connectionDetails.getSchema()));
				if (!databaseAdapter.getSchemaManager().existsSchema(connectionDetails.getSchema())) {
					throw new SQLException("The database schema '" + connectionDetails.getSchema() + "' does not exist.");
				}
			} else {
				connectionDetails.setSchema(databaseAdapter.getSchemaManager().getDefaultSchema());
			}

			// retrieve connection metadata
			databaseAdapter.setConnectionMetaData(databaseAdapter.getUtil().getDatabaseInfo(connectionDetails.getSchema()));

			// check for supported database version
			List<DatabaseConnectionWarning> warnings = versionChecker.checkVersionSupport(databaseAdapter);
			if (!warnings.isEmpty()) {
				databaseAdapter.addConnectionWarnings(warnings);
			}

			// check for registered ADEs
			List<ADEMetadata> ades = Collections.emptyList();
			if (databaseAdapter.getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0) {
				ades = databaseAdapter.getUtil().getADEInfo();
				if (!ades.isEmpty()) {
					databaseAdapter.getConnectionMetaData().setRegisteredADEs(ades);
				}
			}

			// check whether registered ADE are supported by ADE extensions
			DatabaseConnectionWarning warning = checkADESupport(ades);
			if (warning != null) {
				databaseAdapter.addConnectionWarning(warning);
			}

			// check whether user-defined reference systems are supported
			for (DatabaseSrs refSys : config.getDatabaseConfig().getReferenceSystems()) {
				databaseAdapter.getUtil().getSrsInfo(refSys);
			}
		} catch (DatabaseVersionException | SQLException e) {
			try {
				disconnect();
			} catch (Exception ignored) {
				//
			}

			throw e;
		}

		// fire property change event
		eventDispatcher.triggerSyncEvent(new DatabaseConnectionStateEvent(false, true, this));
	}

	public AbstractDatabaseAdapter getActiveDatabaseAdapter() {
		return databaseAdapter;
	}

	public Connection getConnection() throws SQLException {
		if (!isConnected()) {
			throw new SQLException("Database is not connected.");
		}

		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(true);

		// change workspace if required
		if (databaseAdapter.hasVersioningSupport()) {
			Workspace workspace = databaseAdapter.getConnectionDetails().getWorkspace();
			databaseAdapter.getWorkspaceManager().gotoWorkspace(connection, workspace);
		}

		return connection;
	}

	public synchronized boolean isConnected() {
		return dataSource != null
				&& dataSource.getPool() != null
				&& !dataSource.getPool().isClosed();
	}

	public synchronized void purge() {
		if (isConnected()) {
			dataSource.purge();
		}
	}

	public synchronized void disconnect() {
		boolean wasConnected = isConnected();

		dataSource.close(true);
		dataSource = null;

		if (databaseAdapter != null) {
			databaseAdapter = null;
		}

		// fire property change event
		eventDispatcher.triggerSyncEvent(new DatabaseConnectionStateEvent(wasConnected, false, this));
	}

	public DatabaseVersionChecker getDatabaseVersionChecker() {
		return versionChecker;
	}

	public void setDatabaseVersionChecker(DatabaseVersionChecker versionChecker) {
		if (versionChecker != null) {
			this.versionChecker = versionChecker;
		}
	}

	public ADEExtensionManager getADEExtensionManager() {
		return adeManager;
	}

	private DatabaseConnectionWarning checkADESupport(List<ADEMetadata> ades) {
		DatabaseConnectionWarning warning = null;

		if (adeManager != null) {
			// disable ADE extensions per default
			for (ADEExtension extension : adeManager.getExtensions()) {
				extension.setEnabled(false);
			}
			
			// search and enable ADE extensions for the ADEs registered in database
			for (ADEMetadata ade : ades) {
				ADEExtension extension = adeManager.getExtensionById(ade.getADEId());
				if (extension != null) {
					ade.setSupported(true);
					extension.setEnabled(true);
				}
			}
		}		

		// create connection warning for unsupported ADEs
		List<ADEMetadata> unsupported = ades.stream().filter(ade -> !ade.isSupported()).collect(Collectors.toList());
		if (!unsupported.isEmpty()) {
			String message = "The following CityGML ADEs are registered in the database but are not supported:\n" +
					Util.collection2string(unsupported, "\n");
			String formattedMessage = MessageFormat.format(Language.I18N.getString("db.dialog.warn.ade.unsupported"),
					Util.collection2string(unsupported, "<br>"));
			warning = new DatabaseConnectionWarning(message, formattedMessage, DatabaseConfig.CITYDB_PRODUCT_NAME, ConnectionWarningType.UNSUPPORTED_ADE);
		}

		return warning;
	}

}
