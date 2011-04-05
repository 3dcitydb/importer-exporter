/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import oracle.ucp.UniversalConnectionPoolAdapter;
import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.UniversalConnectionPoolLifeCycleState;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.util.DBUtil;

public class DBConnectionPool {
	public static final String PROPERTY_DB_IS_CONNECTED = "isConnected";
	private static DBConnectionPool instance = new DBConnectionPool();

	private final String poolName = "oracle.pool";
	private UniversalConnectionPoolManager poolManager;
	private PoolDataSource poolDataSource;
	private DBConnection activeConnection;
	private PropertyChangeSupport changes;

	private DBConnectionPool() {
		// just to thwart instantiation
		try {
			poolManager = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
		} catch (UniversalConnectionPoolException e) {
			throw new IllegalStateException("Failed to initialize Oracle Universal Pool Manager.");
		}

		changes = new PropertyChangeSupport(this);
	}

	public static DBConnectionPool getInstance() {
		return instance;
	}

	public synchronized void connect(DBConnection conn) throws SQLException {
		if (conn == null)
			throw new SQLException("No database connection details configured.");

		try {
			if (isManagedConnectionPool(poolName))
				disconnect();

			poolDataSource = PoolDataSourceFactory.getPoolDataSource();
			poolDataSource.setConnectionPoolName(poolName);

			poolDataSource.setConnectionFactoryClassName("oracle.jdbc.pool.OracleDataSource");
			poolDataSource.setURL("jdbc:oracle:thin:@//" + conn.getServer() + ":" + conn.getPort()+ "/" + conn.getSid());
			poolDataSource.setUser(conn.getUser());
			poolDataSource.setPassword(conn.getInternalPassword());

			poolManager.createConnectionPool((UniversalConnectionPoolAdapter)poolDataSource);		
			poolManager.startConnectionPool(poolName);	
		} catch (UniversalConnectionPoolException e) {
			poolDataSource = null;
			throw new SQLException("Failed to connect to the database: " + e.getMessage());
		}

		// set internal connection info
		conn.setMetaData(DBUtil.getDatabaseInfo());

		// fire property change events
		activeConnection = conn;
		changes.firePropertyChange(PROPERTY_DB_IS_CONNECTED, false, true);
	}

	public Connection getConnection() throws SQLException {
		if (poolDataSource == null)
			throw new SQLException("Failed to retrieve connection to database due to missing PoolDataSource.");

		return poolDataSource.getConnection();
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

	public DBConnection getActiveConnection() {
		return activeConnection;
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
		boolean isConnected = isConnected();

		try {			
			if (isManagedConnectionPool(poolName))
				poolManager.destroyConnectionPool(poolName);
		} catch (UniversalConnectionPoolException e) {
			throw new SQLException(e.getMessage());
		}

		if (activeConnection != null) {
			activeConnection.setMetaData(null);
			activeConnection = null;
		}

		// fire property change events
		changes.firePropertyChange(PROPERTY_DB_IS_CONNECTED, isConnected, false);
	}

	public synchronized void forceDisconnect() {
		boolean isConnected = isConnected();

		try {
			disconnect();
		} catch (SQLException e) {
			//
		}

		if (activeConnection != null) {
			activeConnection.setMetaData(null);
			activeConnection = null;
		}

		// fire property change events
		changes.firePropertyChange(PROPERTY_DB_IS_CONNECTED, isConnected, false);
	}

	public boolean gotoWorkspace(Connection conn, Workspace workspace) {
		String name = workspace.getName().trim();
		String timestamp = workspace.getTimestamp().trim();
		CallableStatement stmt = null;

		if (!name.equals(Internal.ORACLE_DEFAULT_WORKSPACE) && 
				(name.length() == 0 || name.toUpperCase().equals(Internal.ORACLE_DEFAULT_WORKSPACE)))
			name = Internal.ORACLE_DEFAULT_WORKSPACE;

		try {
			stmt = conn.prepareCall("{call dbms_wm.GotoWorkspace('" + name + "')}");
			stmt.executeQuery();

			if (timestamp.length() > 0) {
				stmt.close();
				stmt = conn.prepareCall("{call dbms_wm.GotoDate('" + timestamp + "', 'DD.MM.YYYY')}");
				stmt.executeQuery();
			}

			return true;
		} catch (SQLException sqlEx) {
			return false;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException sqlEx) {
					//
				}

				stmt = null;
			}
		}
	}

	public boolean existsWorkspace(Workspace workspace) {
		Connection conn = null;

		try {
			conn = getConnection();
			return gotoWorkspace(conn, workspace);
		} catch (SQLException sqlEx) {
			return false;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException sqlEx) {
					//
				}

				conn = null;
			}
		}
	}

	public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
		changes.addPropertyChangeListener(propertyName, l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}

	private boolean isManagedConnectionPool(String poolName) throws UniversalConnectionPoolException {
		for (String managedPool : poolManager.getConnectionPoolNames()) {
			if (managedPool.equals(poolName))
				return true;
		}

		return false;
	}
}
