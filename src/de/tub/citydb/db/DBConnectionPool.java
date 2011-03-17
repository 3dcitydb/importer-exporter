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
package de.tub.citydb.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import oracle.jdbc.pool.OracleConnectionCacheManager;
import oracle.jdbc.pool.OracleDataSource;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.DBConnection;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.util.DBUtil;

public class DBConnectionPool {
	private static HashMap<String, DBConnectionPool> managerMap = new HashMap<String, DBConnectionPool>();
	private final Config config;

	private String cacheName;
	private int minLimit  = 1;
	private int initLimit = 5;
	private int maxLimit  = -1;

	private OracleConnectionCacheManager connMgr = null;
	private OracleDataSource ods = null;

	private DBConnectionPool(Config config) {
		// just to thwart instantiation
		this.config = config;
	}

	public static synchronized DBConnectionPool getInstance(String cacheName, Config config) {
		DBConnectionPool manager = managerMap.get(cacheName);

		if (manager == null) {
			manager = new DBConnectionPool(config);
			manager.cacheName = cacheName;
			managerMap.put(cacheName, manager);
		}

		return manager;
	}

	public synchronized void init() throws SQLException {
		if (ods == null) {
			// connect to database
			Database database = config.getProject().getDatabase();
			DBConnection dbConnection = config.getProject().getDatabase().getActiveConnection();
			if (dbConnection == null) {
				List<DBConnection> dbConnectionList = database.getConnections();
				if (dbConnectionList == null || dbConnectionList.isEmpty())
					throw new SQLException("No database connection configured.");

				dbConnection = dbConnectionList.get(0);
				database.setActiveConnection(dbConnection);
			}

			try {
				ods = new OracleDataSource();
				ods.setConnectionCachingEnabled(true);
				ods.setConnectionCacheName(cacheName);

				ods.setDriverType("thin");
				ods.setServerName(dbConnection.getServer());
				ods.setServiceName(dbConnection.getSid());
				ods.setPortNumber(dbConnection.getPort());
				ods.setUser(dbConnection.getUser());
				ods.setPassword(config.getInternal().getCurrentDbPassword());

				connMgr = OracleConnectionCacheManager.getConnectionCacheManagerInstance();

				Properties cacheProperties = new Properties();
				cacheProperties.put("MinLimit", minLimit);
				cacheProperties.put("InitialLimit", initLimit);
				cacheProperties.put("MaxLimit", maxLimit);

				connMgr.createCache(cacheName, ods, cacheProperties);
			} catch (SQLException sqlEx) {
				ods = null;
				throw sqlEx;
			}

			// set internal connection info
			DBUtil dbUtil = DBUtil.getInstance(this);
			dbConnection.setMetaData(dbUtil.getDatabaseInfo());

			// fire property change events
			config.getInternal().setOpenConnection(dbConnection);
		}
	}

	public synchronized void init(int minLimit, int initLimit, int maxLimit, Config config) throws SQLException {
		if (ods == null) {
			this.minLimit = minLimit;
			this.initLimit = initLimit;
			this.maxLimit = maxLimit;

			init();
		}
	}

	public OracleDataSource getDataSource() throws SQLException {
		if (ods == null)
			throw new SQLException("OracleDataSource is null.");

		return ods;
	}

	public Connection getConnection() throws SQLException {
		if (ods == null)
			throw new SQLException("OracleDataSource is null.");

		return ods.getConnection();
	}

	public int getNumActive() throws SQLException {
		if (connMgr == null)
			return 0;

		return connMgr.getNumberOfActiveConnections(cacheName);
	}

	public int getNumIdle() throws SQLException {
		if (connMgr == null)
			return 0;

		return connMgr.getNumberOfAvailableConnections(cacheName);
	}

	public int getCacheSize() throws SQLException {
		if (connMgr == null)
			return 0;

		return getNumActive() + getNumIdle();
	}

	public int getCacheMinLimit() {
		return minLimit;
	}

	public int getCacheInitLimit() {
		return initLimit;
	}

	public int getCacheMaxLimit() {
		return maxLimit;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheProperties(Properties properties) throws SQLException {
		if (connMgr != null) {
			connMgr.reinitializeCache(cacheName, properties);

			String minLimit = properties.getProperty("MinLimit");
			String initLimit = properties.getProperty("InitialLimit");
			String maxLimit = properties.getProperty("MaxLimit");

			if (minLimit != null && minLimit.trim().length() > 0)
				this.minLimit = Integer.parseInt(minLimit);

			if (initLimit != null && initLimit.trim().length() > 0)
				this.initLimit = Integer.parseInt(initLimit);

			if (maxLimit != null && maxLimit.trim().length() > 0)
				this.maxLimit = Integer.parseInt(maxLimit);
		}
	}

	public Properties getCacheProperties() throws SQLException {
		if (ods == null)
			return null;

		return ods.getConnectionCacheProperties() ;
	}

	public synchronized void refresh() throws SQLException {
		if (connMgr != null)
			connMgr.refreshCache(cacheName, OracleConnectionCacheManager.REFRESH_ALL_CONNECTIONS);
	}

	public synchronized void close() throws SQLException {
		if (ods != null) {
			ods.close();
			ods = null;

			// fire property change events
			config.getInternal().unsetOpenConnection();
		}
	}

	public synchronized void forceClose() {
		try {
			close();
		} catch (SQLException sqlEx) {
			//
		}

		ods = null;
	}

	public boolean changeWorkspace(Connection conn, Workspace workspace) {
		String name = workspace.getName().trim();
		String timestamp = workspace.getTimestamp().trim();
		CallableStatement stmt = null;

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

	public boolean checkWorkspace(Workspace workspace) {
		Connection conn = null;

		try {
			conn = getConnection();
			return changeWorkspace(conn, workspace);
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
}
