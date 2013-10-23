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
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import oracle.ucp.UniversalConnectionPoolAdapter;
import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.UniversalConnectionPoolLifeCycleState;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;

import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import de.tub.citydb.api.database.DatabaseConfigurationException;
import de.tub.citydb.api.database.DatabaseSrs;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.api.registry.ObjectRegistry;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.database.adapter.AbstractDatabaseAdapter;
import de.tub.citydb.database.adapter.DatabaseAdapterFactory;
import de.tub.citydb.event.DatabaseConnectionStateEventImpl;

public class DatabaseConnectionPool {
	private static DatabaseConnectionPool instance = new DatabaseConnectionPool();
	private static final int LOGIN_TIMEOUT = 120;

	private final String poolName = DefaultGMLIdManager.getInstance().generateUUID();
	private final EventDispatcher eventDispatcher;
	private UniversalConnectionPoolManager poolManager;
	private AbstractDatabaseAdapter databaseAdapter;
	private PoolDataSource poolDataSource;

	private DatabaseConnectionPool() {
		// just to thwart instantiation
		try {
			poolManager = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
		} catch (UniversalConnectionPoolException e) {
			throw new IllegalStateException("Failed to initialize Oracle Universal Pool Manager.");
		}

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
			throw new DatabaseConfigurationException(Internal.I18N.getString("db.dialog.error.conn.user"));

		if (conn.getInternalPassword() == null || conn.getInternalPassword().trim().length() == 0)
			throw new DatabaseConfigurationException(Internal.I18N.getString("db.dialog.error.conn.pass"));

		if (conn.getServer() == null || conn.getServer().trim().length() == 0)
			throw new DatabaseConfigurationException(Internal.I18N.getString("db.dialog.error.conn.server"));

		if (conn.getPort() == null)
			throw new DatabaseConfigurationException(Internal.I18N.getString("db.dialog.error.conn.port"));

		if (conn.getSid() == null || conn.getSid().trim().length() == 0)
			throw new DatabaseConfigurationException(Internal.I18N.getString("db.dialog.error.conn.sid"));

		try {
			if (isManagedConnectionPool(poolName))
				disconnect();

			// get database adapter
			databaseAdapter = DatabaseAdapterFactory.getInstance().createDatabaseAdapter(conn.getDatabaseType());

			// create data source
			poolDataSource = PoolDataSourceFactory.getPoolDataSource();
			poolDataSource.setConnectionPoolName(poolName);

			poolDataSource.setConnectionFactoryClassName(databaseAdapter.getConnectionFactoryClassName());
			poolDataSource.setURL(databaseAdapter.getJDBCUrl(conn.getServer(), conn.getPort(), conn.getSid()));
			poolDataSource.setUser(conn.getUser());
			poolDataSource.setPassword(conn.getInternalPassword());

			// set connection properties
			Properties properties = databaseAdapter.getConnectionProperties();
			if (properties != null)
				poolDataSource.setConnectionProperties(properties);

			poolManager.createConnectionPool((UniversalConnectionPoolAdapter)poolDataSource);		
			poolManager.startConnectionPool(poolName);
		} catch (UniversalConnectionPoolException e) {
			poolDataSource = null;
			throw new SQLException(Internal.I18N.getString("db.dialog.error.conn.sql"), e);
		} catch (SQLException e) {
			poolDataSource = null;
			throw new SQLException(Internal.I18N.getString("db.dialog.error.conn.sql"), e);
		}

		try {
			// retrieve connection metadata
			databaseAdapter.setConnectionMetaData(databaseAdapter.getUtil().getDatabaseInfo());

			// check whether user-defined reference systems are supported
			for (DatabaseSrs refSys : config.getProject().getDatabase().getReferenceSystems())
				databaseAdapter.getUtil().getSrsInfo(refSys);

		} catch (SQLException e) {
			try {
				poolManager.destroyConnectionPool(poolName);
			} catch (UniversalConnectionPoolException e1) {
				//
			}

			poolDataSource = null;

			// try and get some meaningful error message
			Throwable cause = null;
			Iterator<Throwable> iter = e.iterator();
			while (iter.hasNext())
				cause = iter.next();

			throw (cause != null) ? new SQLException(cause.getMessage()) : e;
		}

		// fire property change events
		databaseAdapter.setConnectionDetails(conn);
		eventDispatcher.triggerSyncEvent(new DatabaseConnectionStateEventImpl(false, true, this));
	}
	
	public AbstractDatabaseAdapter getActiveDatabaseAdapter() {
		return databaseAdapter;
	}

	public Connection getConnectionWithTimeout() throws SQLException {
		if (poolDataSource == null)
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
				return poolDataSource.getConnection();
			}
		});

		Connection connection = null;
		service.execute(connectTask);

		try {
			connection = connectTask.get(LOGIN_TIMEOUT, TimeUnit.SECONDS);
			connection.setAutoCommit(true);

			service.shutdown();
		} catch (Exception e) {
			service.shutdownNow();
			forceDisconnect();

			if (e instanceof ExecutionException)
				throw (SQLException)e.getCause();
			else 
				throw new SQLException("A connection to the database could not be established.\nThe database did not respond for " + LOGIN_TIMEOUT + " seconds.");
		}

		return connection;
	}

	public Connection getConnection() throws SQLException {
		if (poolDataSource == null)
			throw new SQLException("Database is not connected.");

		Connection connection = poolDataSource.getConnection();
		connection.setAutoCommit(true);

		return connection;
	}

	public UniversalConnectionPoolLifeCycleState getLifeCyleState() {
		try {
			if (isManagedConnectionPool(poolName))
				return poolManager.getConnectionPool(poolName).getLifeCycleState();
		} catch (UniversalConnectionPoolException e) {
			//
		}

		return UniversalConnectionPoolLifeCycleState.LIFE_CYCLE_FAILED;
	}

	public boolean isConnected() {
		return getLifeCyleState().equals(UniversalConnectionPoolLifeCycleState.LIFE_CYCLE_RUNNING);
	}

	public int getBorrowedConnectionsCount() throws SQLException {
		return poolDataSource != null ? poolDataSource.getBorrowedConnectionsCount() : 0;
	}

	public int getAvailableConnectionsCount() throws SQLException {
		return poolDataSource != null ? poolDataSource.getAvailableConnectionsCount() : 0;
	}

	public int getMinPoolSize() throws SQLException {
		return poolDataSource != null ? poolDataSource.getMinPoolSize() : 0;
	}

	public int getMaxPoolSize() throws SQLException {
		return poolDataSource != null ? poolDataSource.getMaxPoolSize() : 0;
	}

	public int getInitialPoolSize() throws SQLException {
		return poolDataSource != null ? poolDataSource.getInitialPoolSize() : 0;
	}

	public void setMinPoolSize(int minPoolSize) throws SQLException {
		if (poolDataSource != null)
			poolDataSource.setMinPoolSize(minPoolSize);
	}

	public void setMaxPoolSize(int maxPoolSize) throws SQLException {
		if (poolDataSource != null)
			poolDataSource.setMaxPoolSize(maxPoolSize);
	}

	public void setInitialPoolSize(int initialPoolSize) throws SQLException {
		if (poolDataSource != null)
			poolDataSource.setInitialPoolSize(initialPoolSize);
	}

	public synchronized void refresh() throws UniversalConnectionPoolException {
		if (isManagedConnectionPool(poolName))
			poolManager.refreshConnectionPool(poolName);
	}

	public synchronized void recylce() throws UniversalConnectionPoolException {
		if (isManagedConnectionPool(poolName))
			poolManager.recycleConnectionPool(poolName);
	}

	public synchronized void purge() throws UniversalConnectionPoolException {
		if (isManagedConnectionPool(poolName))
			poolManager.purgeConnectionPool(poolName);
	}

	public synchronized void stop() throws UniversalConnectionPoolException {
		if (isManagedConnectionPool(poolName))
			poolManager.stopConnectionPool(poolName);
	}

	public synchronized void start() throws UniversalConnectionPoolException {
		if (isManagedConnectionPool(poolName))
			poolManager.startConnectionPool(poolName);
	}

	public synchronized void disconnect() throws SQLException {
		boolean wasConnected = isConnected();

		try {			
			if (isManagedConnectionPool(poolName))
				poolManager.destroyConnectionPool(poolName);
		} catch (UniversalConnectionPoolException e) {
			throw new SQLException(e.getMessage());
		}

		if (databaseAdapter != null)
			databaseAdapter = null;

		// fire property change events
		eventDispatcher.triggerSyncEvent(new DatabaseConnectionStateEventImpl(wasConnected, false, this));
	}

	public synchronized void forceDisconnect() {
		boolean wasConnected = isConnected();

		try {
			if (isManagedConnectionPool(poolName))
				poolManager.destroyConnectionPool(poolName);
		} catch (UniversalConnectionPoolException e) {
			//
		}

		if (databaseAdapter != null)
			databaseAdapter = null;

		// fire property change events
		eventDispatcher.triggerSyncEvent(new DatabaseConnectionStateEventImpl(wasConnected, false, this));
	}

	private boolean isManagedConnectionPool(String poolName) throws UniversalConnectionPoolException {
		for (String managedPool : poolManager.getConnectionPoolNames()) {
			if (managedPool.equals(poolName))
				return true;
		}

		return false;
	}

}
