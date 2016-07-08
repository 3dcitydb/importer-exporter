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
package org.citydb.modules.citygml.common.database.cache;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import org.citydb.config.Config;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.h2.H2Adapter;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;
import org.citygml4j.util.gmlid.DefaultGMLIdManager;

public class CacheTableManager {
	private final Logger LOG = Logger.getInstance();
	private final AbstractDatabaseAdapter cacheAdapter;	
	private final Connection cacheConnection;
	private final Config config;

	private String cacheDir;
	private AbstractDatabaseAdapter databaseAdapter;
	private Connection databaseConnection;

	private ConcurrentHashMap<CacheTableModelEnum, CacheTable> cacheTables;
	private ConcurrentHashMap<CacheTableModelEnum, BranchCacheTable> branchCacheTables;

	public CacheTableManager(DatabaseConnectionPool dbPool, int concurrencyLevel, Config config) throws SQLException, IOException {		
		if (config.getProject().getGlobal().getCache().isUseDatabase()) {
			cacheAdapter = dbPool.getActiveDatabaseAdapter();
			cacheConnection = dbPool.getConnection();
		}

		else {
			File tempDir = checkTempDir(config.getProject().getGlobal().getCache().getLocalCachePath());
			LOG.debug("Local cache directory is '" + tempDir.getAbsolutePath() + "'.");
			cacheAdapter = new H2Adapter();

			try {
				Class.forName(cacheAdapter.getConnectionFactoryClassName());
			} catch (ClassNotFoundException e) {
				throw new SQLException(e);
			}

			try {
				cacheDir = tempDir.getAbsolutePath() + File.separator + DefaultGMLIdManager.getInstance().generateUUID("");		
				cacheConnection = DriverManager.getConnection(cacheAdapter.getJDBCUrl(cacheDir + File.separator + "tmp", -1, null), "sa", "");
			} catch (SQLException e) {
				deleteTempFiles(new File(cacheDir));
				throw e;
			}
		}

		cacheConnection.setAutoCommit(false);

		cacheTables = new ConcurrentHashMap<CacheTableModelEnum, CacheTable>(CacheTableModelEnum.values().length, 0.75f, concurrencyLevel);
		branchCacheTables = new ConcurrentHashMap<CacheTableModelEnum, BranchCacheTable>(CacheTableModelEnum.values().length, 0.75f, concurrencyLevel);
		this.config = config;
	}

	public AbstractDatabaseAdapter getDatabaseAdapter() {
		return cacheAdapter;
	}

	public CacheTable createCacheTable(CacheTableModelEnum model) throws SQLException {
		return createCacheTable(model, cacheConnection, cacheAdapter);		
	}

	public CacheTable createCacheTableInDatabase(CacheTableModelEnum model) throws SQLException {
		initDatabaseConnection();
		return createCacheTable(model, databaseConnection, databaseAdapter);
	}

	private CacheTable createCacheTable(CacheTableModelEnum model, Connection connection, AbstractDatabaseAdapter adapter) throws SQLException {
		CacheTable cacheTable = getOrCreateCacheTable(model, adapter, connection);		
		if (!cacheTable.isCreated())
			cacheTable.create();

		return cacheTable;
	}

	public CacheTable createAndIndexCacheTable(CacheTableModelEnum model) throws SQLException {
		CacheTable cacheTable = getOrCreateCacheTable(model, cacheAdapter, cacheConnection);
		if (!cacheTable.isCreated())
			cacheTable.createAndIndex();

		return cacheTable;
	}

	public BranchCacheTable createBranchCacheTable(CacheTableModelEnum model) throws SQLException {
		BranchCacheTable branchCacheTable = gerOrCreateBranchCacheTable(model, cacheAdapter, cacheConnection);		
		if (!branchCacheTable.isCreated())
			branchCacheTable.create();

		return branchCacheTable;
	}

	public BranchCacheTable createAndIndexBranchCacheTable(CacheTableModelEnum model) throws SQLException {
		BranchCacheTable branchCacheTable = gerOrCreateBranchCacheTable(model, cacheAdapter, cacheConnection);
		if (!branchCacheTable.isCreated())
			branchCacheTable.createAndIndex();

		return branchCacheTable;
	}

	public CacheTable getCacheTable(CacheTableModelEnum type) {		
		return cacheTables.get(type);
	}

	public boolean existsCacheTable(CacheTableModelEnum type) {
		CacheTable cacheTable = cacheTables.get(type);
		return (cacheTable != null && cacheTable.isCreated());
	}

	public void drop(AbstractCacheTable cacheTable) throws SQLException {
		cacheTable.drop();

		if (cacheTable instanceof CacheTable)	
			cacheTables.remove(cacheTable.getModelType());
		else
			branchCacheTables.remove(cacheTable.getModelType());
	}

	public void dropAll() throws SQLException {
		try {
			for (CacheTable cacheTable : cacheTables.values())
				cacheTable.drop();

			for (BranchCacheTable branchCacheTable : branchCacheTables.values())
				branchCacheTable.drop();

		} finally  {
			// clean up
			cacheTables.clear();
			branchCacheTables.clear();

			try {
				cacheConnection.close();
			} catch (SQLException e) {
				//
			}

			if (databaseConnection != null) {
				try {
					databaseConnection.close();
				} catch (SQLException e) {
					//
				} finally {
					databaseConnection = null;
					databaseAdapter = null;
				}
			}

			if (cacheDir != null) {
				try {
					deleteTempFiles(new File(cacheDir));
				} catch (IOException e) {
					LOG.error("Failed to delete temp directory: " + e.getMessage());
				}
			}			
		}
	}

	private CacheTable getOrCreateCacheTable(CacheTableModelEnum model, AbstractDatabaseAdapter adapter, Connection connection) {
		CacheTable cacheTable = cacheTables.get(model);
		if (cacheTable == null) {
			CacheTable tmp = new CacheTable(model, connection, adapter.getSQLAdapter());
			cacheTable = cacheTables.putIfAbsent(model, tmp);
			if (cacheTable == null)
				cacheTable = tmp;
		}

		return cacheTable;
	}

	private BranchCacheTable gerOrCreateBranchCacheTable(CacheTableModelEnum model, AbstractDatabaseAdapter adapter, Connection connection) {
		BranchCacheTable branchCacheTable = branchCacheTables.get(model);
		if (branchCacheTable == null) {
			BranchCacheTable tmp = new BranchCacheTable(model, connection, adapter.getSQLAdapter());
			branchCacheTable = branchCacheTables.putIfAbsent(model, tmp);
			if (branchCacheTable == null)
				branchCacheTable = tmp;
		}

		return branchCacheTable;
	}

	private File checkTempDir(String tempDir) throws IOException {
		if (tempDir == null || tempDir.trim().length() == 0)
			throw new IOException("No temp directory for local cache provided.");

		File dir = new File(tempDir);

		if (!dir.exists() && !dir.mkdirs())
			throw new IOException("Failed to create temp directory '" + dir.getAbsolutePath() + "'.");

		if (!dir.isDirectory())
			throw new IOException("The local cache setting '" + dir.getAbsolutePath() + "' is not a directory.");

		if (!dir.canRead() && !dir.setReadable(true, true))
			throw new IOException("Lacking read permissions on temp directory '" + dir.getAbsolutePath() + "'.");

		if (!dir.canWrite() && !dir.setWritable(true, true))
			throw new IOException("Lacking write permissions on temp directory '" + dir.getAbsolutePath() + "'.");

		return dir;
	}

	private void deleteTempFiles(File file) throws IOException {
		if (file.isDirectory()) {
			for (File nested : file.listFiles())
				deleteTempFiles(nested);
		}

		file.delete();
	}

	private void initDatabaseConnection() throws SQLException {
		// some cache tables need to be created in the database
		// hence, if we use a local cache, we must create a database adapter and connection 
		if (databaseConnection == null) {
			if (config.getProject().getGlobal().getCache().isUseDatabase()) {
				databaseConnection = cacheConnection;
				databaseAdapter = cacheAdapter;
			} else {
				databaseConnection = DatabaseConnectionPool.getInstance().getConnection();
				databaseConnection.setAutoCommit(false);
				databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
			}
		}
	}

}
