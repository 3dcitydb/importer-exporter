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
package org.citydb.database.connection;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.database.DBConnection;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.database.DatabaseConfigurationException;
import org.citydb.config.project.database.DatabaseSrs;
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

public class DatabaseConnectionPool {
	private static DatabaseConnectionPool instance;
	private final int DEFAULT_LOGIN_TIMEOUT = 60;

	private final String poolName = DefaultGMLIdManager.getInstance().generateUUID();
	private final EventDispatcher eventDispatcher;
	private AbstractDatabaseAdapter databaseAdapter;
	private DataSource dataSource;
	private DatabaseVersionChecker versionChecker;
	private ADEExtensionManager adeManager;

	private DatabaseConnectionPool() {
		// just to thwart instantiation
		versionChecker = new DefaultDatabaseVersionChecker();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		adeManager = ADEExtensionManager.getInstance();
	}

	public static synchronized DatabaseConnectionPool getInstance() {
		if (instance == null)
			instance = new DatabaseConnectionPool();

		return instance;
	}

	public synchronized void connect(Config config) throws DatabaseConfigurationException, DatabaseVersionException, SQLException {
		DBConnection conn = config.getProject().getDatabase().getActiveConnection();
		if (conn == null)
			throw new DatabaseConfigurationException("No valid database connection details provided.");

		// check valid connection details
		conn.validate();

		// disconnect if we are currently connected to another database
		if (isConnected())
			disconnect();

		// get database adapter
		databaseAdapter = DatabaseAdapterFactory.getInstance().createDatabaseAdapter(conn.getDatabaseType());

		// general pool properties
		PoolProperties properties = new PoolProperties();
		properties.setUrl(databaseAdapter.getJDBCUrl(conn.getServer(), conn.getPort(), conn.getSid()));
		properties.setDriverClassName(databaseAdapter.getConnectionFactoryClassName());
		properties.setUsername(conn.getUser());
		properties.setPassword(conn.getInternalPassword());
		properties.setName(poolName);
		properties.setDefaultAutoCommit(true);

		// set user-definable pool properties	
		if (conn.getMaxActive() != null) properties.setMaxActive(conn.getMaxActive());
		if (conn.getMaxIdle() != null) properties.setMaxIdle(conn.getMaxIdle());
		if (conn.getMinIdle() != null) properties.setMinIdle(conn.getMinIdle());
		if (conn.getInitialSize() != null) properties.setInitialSize(conn.getInitialSize());
		if (conn.getMaxWait() != null) properties.setMaxWait(conn.getMaxWait());
		if (conn.getTestOnBorrow() != null) properties.setTestOnBorrow(conn.getTestOnBorrow());
		if (conn.getTestOnReturn() != null) properties.setTestOnReturn(conn.getTestOnReturn());
		if (conn.getTestWhileIdle() != null) properties.setTestWhileIdle(conn.getTestWhileIdle());
		if (conn.getValidationQuery() != null) properties.setValidationQuery(conn.getValidationQuery());
		if (conn.getValidatorClassName() != null) properties.setValidatorClassName(conn.getValidatorClassName());
		if (conn.getTimeBetweenEvictionRunsMillis() != null) properties.setTimeBetweenEvictionRunsMillis(conn.getTimeBetweenEvictionRunsMillis());
		if (conn.getNumTestsPerEvictionRun() != null) properties.setNumTestsPerEvictionRun(conn.getNumTestsPerEvictionRun());
		if (conn.getMinEvictableIdleTimeMillis() != null) properties.setMinEvictableIdleTimeMillis(conn.getMinEvictableIdleTimeMillis());
		if (conn.getRemoveAbandoned() != null) properties.setRemoveAbandoned(conn.getRemoveAbandoned());
		if (conn.getRemoveAbandonedTimeout() != null) properties.setRemoveAbandonedTimeout(conn.getRemoveAbandonedTimeout());
		if (conn.getLogAbandoned() != null) properties.setLogAbandoned(conn.getLogAbandoned());
		if (conn.getConnectionProperties() != null) properties.setConnectionProperties(conn.getConnectionProperties());
		if (conn.getInitSQL() != null) properties.setInitSQL(conn.getInitSQL());
		if (conn.getValidationInterval() != null) properties.setValidationInterval(conn.getValidationInterval());
		if (conn.getJmxEnabled() != null) properties.setJmxEnabled(conn.getJmxEnabled());
		if (conn.getFairQueue() != null) properties.setFairQueue(conn.getFairQueue());
		if (conn.getAbandonWhenPercentageFull() != null) properties.setAbandonWhenPercentageFull(conn.getAbandonWhenPercentageFull());
		if (conn.getMaxAge() != null) properties.setMaxAge(conn.getMaxAge());
		if (conn.getUseEquals() != null) properties.setUseEquals(conn.getUseEquals());
		if (conn.getSuspectTimeout() != null) properties.setSuspectTimeout(conn.getSuspectTimeout());

		// pool maintenance
		properties.setJdbcInterceptors("StatementFinalizer");

		// create new data source
		dataSource = new DataSource(properties);
		dataSource.setLoginTimeout(conn.isSetLoginTimeout() ? conn.getLoginTimeout() : DEFAULT_LOGIN_TIMEOUT);

		try {
			// create connection pool
			dataSource.createPool();
						
			// set connection details
			DatabaseConnectionDetails connectionDetails = new DatabaseConnectionDetails(conn);
			databaseAdapter.setConnectionDetails(connectionDetails);

			// check database schema
			if (connectionDetails.isSetSchema()) {
				connectionDetails.setSchema(databaseAdapter.getSchemaManager().formatSchema(connectionDetails.getSchema()));
				if (!databaseAdapter.getSchemaManager().existsSchema(connectionDetails.getSchema()))
					throw new SQLException("The database schema '" + connectionDetails.getSchema() + "' does not exist.");
			} else
				connectionDetails.setSchema(databaseAdapter.getSchemaManager().getDefaultSchema());
			
			// retrieve connection metadata
			databaseAdapter.setConnectionMetaData(databaseAdapter.getUtil().getDatabaseInfo(connectionDetails.getSchema()));

			// check for supported database version
			List<DatabaseConnectionWarning> warnings = versionChecker.checkVersionSupport(databaseAdapter);
			if (!warnings.isEmpty())
				databaseAdapter.addConnectionWarnings(warnings);

			// check for registered ADEs
			List<ADEMetadata> ades = Collections.emptyList();
			if (databaseAdapter.getConnectionMetaData().getCityDBVersion().compareTo(4, 0, 0) >= 0) {
				ades = databaseAdapter.getUtil().getADEInfo();
				if (!ades.isEmpty())
					databaseAdapter.getConnectionMetaData().setRegisteredADEs(ades);
			}

			// check whether registered ADE are supported by ADE extensions
			DatabaseConnectionWarning warning = checkADESupport(ades);
			if (warning != null)
				databaseAdapter.addConnectionWarning(warning);

			// check whether user-defined reference systems are supported
			for (DatabaseSrs refSys : config.getProject().getDatabase().getReferenceSystems())
				databaseAdapter.getUtil().getSrsInfo(refSys);			

		} catch (DatabaseVersionException | SQLException e) {
			try { disconnect(); } catch (Exception ignored) { }
			throw e;
		}

		// fire property change events
		eventDispatcher.triggerSyncEvent(new DatabaseConnectionStateEvent(false, true, this));
	}

	public AbstractDatabaseAdapter getActiveDatabaseAdapter() {
		return databaseAdapter;
	}

	public Connection getConnection() throws SQLException {
		if (!isConnected())
			throw new SQLException("Database is not connected.");

		Connection connection = dataSource.getConnection();
		connection.setAutoCommit(true);

		return connection;
	}

	public synchronized boolean isConnected() {
		return dataSource != null && dataSource.getPool() != null && !dataSource.getPool().isClosed();
	}

	public synchronized void purge() {
		if (isConnected())
			dataSource.purge();
	}

	public synchronized void disconnect() {
		boolean wasConnected = isConnected();

		dataSource.close(true);
		dataSource = null;

		if (databaseAdapter != null)
			databaseAdapter = null;

		// fire property change events
		eventDispatcher.triggerSyncEvent(new DatabaseConnectionStateEvent(wasConnected, false, this));
	}

	public DatabaseVersionChecker getDatabaseVersionChecker() {
		return versionChecker;
	}

	public void setDatabaseVersionChecker(DatabaseVersionChecker versionChecker) {
		if (versionChecker != null)
			this.versionChecker = versionChecker;
	}

	public ADEExtensionManager getADEExtensionManager() {
		return adeManager;
	}

	public void setADEExtensionManager(ADEExtensionManager adeManager) {
		this.adeManager = adeManager;
	}

	private DatabaseConnectionWarning checkADESupport(List<ADEMetadata> ades) {
		DatabaseConnectionWarning warning = null;

		if (adeManager != null) {
			// disable ADE extensions per default
			for (ADEExtension extension : adeManager.getExtensions())
				extension.setEnabled(false);
			
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
			String message = "The following CityGML ADEs are registered in the database but are not supported:\n" + Util.collection2string(unsupported, "\n");
			String formattedMessage = MessageFormat.format(Language.I18N.getString("db.dialog.warn.ade.unsupported"),
					Util.collection2string(unsupported, "<br>"));
			warning = new DatabaseConnectionWarning(message, formattedMessage, Database.CITYDB_PRODUCT_NAME, ConnectionWarningType.UNSUPPORTED_ADE);
		}

		return warning;
	}

}
