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
package org.citydb.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.citydb.api.database.DatabaseConfigurationException;
import org.citydb.api.database.DatabaseSrs;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.registry.ObjectRegistry;
import org.citydb.config.Config;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.DBConnection;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.DatabaseAdapterFactory;
import org.citydb.event.DatabaseConnectionStateEventImpl;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class DatabaseConnectionPool {
	private static DatabaseConnectionPool instance = new DatabaseConnectionPool();
	private static final int LOGIN_TIMEOUT = 120;

	private final String poolName = DefaultGMLIdManager.getInstance().generateUUID();
	private final EventDispatcher eventDispatcher;
	private AbstractDatabaseAdapter databaseAdapter;
	private DataSource dataSource;

	private DatabaseConnectionPool() {
		// just to thwart instantiation
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
	}

	public static DatabaseConnectionPool getInstance() {
		return instance;
	}

	public synchronized void connect(Config config) throws DatabaseConfigurationException, SQLException {
		DBConnection conn = config.getProject().getDatabase().getActiveConnection();

		if (conn == null)
			throw new DatabaseConfigurationException("No valid database connection details provided.");

		// check valid connection details
		if (conn.getUser() == null || conn.getUser().trim().length() == 0)
			throw new DatabaseConfigurationException(Language.I18N.getString("db.dialog.error.conn.user"));

		if (conn.getInternalPassword() == null || conn.getInternalPassword().trim().length() == 0)
			throw new DatabaseConfigurationException(Language.I18N.getString("db.dialog.error.conn.pass"));

		if (conn.getServer() == null || conn.getServer().trim().length() == 0)
			throw new DatabaseConfigurationException(Language.I18N.getString("db.dialog.error.conn.server"));

		if (conn.getPort() == null)
			throw new DatabaseConfigurationException(Language.I18N.getString("db.dialog.error.conn.port"));

		if (conn.getSid() == null || conn.getSid().trim().length() == 0)
			throw new DatabaseConfigurationException(Language.I18N.getString("db.dialog.error.conn.sid"));

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
		if (conn.getMaxActive() != null) properties.setMaxActive(conn.getMaxActive().intValue());
		if (conn.getMaxIdle() != null) properties.setMaxIdle(conn.getMaxIdle().intValue());
		if (conn.getMinIdle() != null) properties.setMinIdle(conn.getMinIdle().intValue());
		if (conn.getInitialSize() != null) properties.setInitialSize(conn.getInitialSize().intValue());
		if (conn.getMaxWait() != null) properties.setMaxWait(conn.getMaxWait().intValue());
		if (conn.getTestOnBorrow() != null) properties.setTestOnBorrow(conn.getTestOnBorrow().booleanValue());
		if (conn.getTestOnReturn() != null) properties.setTestOnReturn(conn.getTestOnReturn().booleanValue());
		if (conn.getTestWhileIdle() != null) properties.setTestWhileIdle(conn.getTestWhileIdle().booleanValue());
		if (conn.getValidationQuery() != null) properties.setValidationQuery(conn.getValidationQuery());
		if (conn.getValidatorClassName() != null) properties.setValidatorClassName(conn.getValidatorClassName());
		if (conn.getTimeBetweenEvictionRunsMillis() != null) properties.setTimeBetweenEvictionRunsMillis(conn.getTimeBetweenEvictionRunsMillis().intValue());
		if (conn.getNumTestsPerEvictionRun() != null) properties.setNumTestsPerEvictionRun(conn.getNumTestsPerEvictionRun().intValue());
		if (conn.getMinEvictableIdleTimeMillis() != null) properties.setMinEvictableIdleTimeMillis(conn.getMinEvictableIdleTimeMillis().intValue());
		if (conn.getRemoveAbandoned() != null) properties.setRemoveAbandoned(conn.getRemoveAbandoned().booleanValue());
		if (conn.getRemoveAbandonedTimeout() != null) properties.setRemoveAbandonedTimeout(conn.getRemoveAbandonedTimeout().intValue());
		if (conn.getLogAbandoned() != null) properties.setLogAbandoned(conn.getLogAbandoned().booleanValue());
		if (conn.getConnectionProperties() != null) properties.setConnectionProperties(conn.getConnectionProperties());
		if (conn.getInitSQL() != null) properties.setInitSQL(conn.getInitSQL());
		if (conn.getValidationInterval() != null) properties.setValidationInterval(conn.getValidationInterval().longValue());
		if (conn.getJmxEnabled() != null) properties.setJmxEnabled(conn.getJmxEnabled().booleanValue());
		if (conn.getFairQueue() != null) properties.setFairQueue(conn.getFairQueue().booleanValue());
		if (conn.getAbandonWhenPercentageFull() != null) properties.setAbandonWhenPercentageFull(conn.getAbandonWhenPercentageFull().intValue());
		if (conn.getMaxAge() != null) properties.setMaxAge(conn.getMaxAge().longValue());
		if (conn.getUseEquals() != null) properties.setUseEquals(conn.getUseEquals().booleanValue());
		if (conn.getSuspectTimeout() != null) properties.setSuspectTimeout(conn.getSuspectTimeout().intValue());

		// pool maintenance
		properties.setJdbcInterceptors("StatementFinalizer");

		// create new data source
		dataSource = new DataSource(properties);

		try {
			// create connection pool
			dataSource.createPool();
			databaseAdapter.setConnectionDetails(conn);

			// retrieve connection metadata
			databaseAdapter.setConnectionMetaData(databaseAdapter.getUtil().getDatabaseInfo());

			// check whether user-defined reference systems are supported
			for (DatabaseSrs refSys : config.getProject().getDatabase().getReferenceSystems())
				databaseAdapter.getUtil().getSrsInfo(refSys);

		} catch (SQLException e) {
			disconnect();
			throw e;
		}

		// fire property change events
		eventDispatcher.triggerSyncEvent(new DatabaseConnectionStateEventImpl(false, true, this));
	}

	public AbstractDatabaseAdapter getActiveDatabaseAdapter() {
		return databaseAdapter;
	}

	public Connection getConnectionWithTimeout() throws SQLException {
		if (!isConnected())
			throw new SQLException("Database is not connected.");

		ExecutorService service = Executors.newFixedThreadPool(1, new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		});

		FutureTask<Connection> connectTask = new FutureTask<Connection>(new Callable<Connection>() {
			public Connection call() throws SQLException {
				return dataSource.getConnection();
			}
		});

		Connection connection = null;
		service.execute(connectTask);

		try {
			connection = connectTask.get(LOGIN_TIMEOUT, TimeUnit.SECONDS);
			service.shutdown();
		} catch (Exception e) {
			service.shutdownNow();

			if (e instanceof ExecutionException)
				throw (SQLException)e.getCause();
			else 
				throw new SQLException("A connection to the database could not be established.\nThe database did not respond for " + LOGIN_TIMEOUT + " seconds.");
		}

		return connection;
	}

	public Connection getConnection() throws SQLException {
		if (!isConnected())
			throw new SQLException("Database is not connected.");

		return dataSource.getConnection();
	}

	public boolean isConnected() {
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
		eventDispatcher.triggerSyncEvent(new DatabaseConnectionStateEventImpl(wasConnected, false, this));
	}

}
