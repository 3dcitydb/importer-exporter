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
package de.tub.citydb.modules.citygml.common.database.cache;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

import org.citygml4j.util.gmlid.DefaultGMLIdManager;

import de.tub.citydb.config.Config;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.database.adapter.AbstractDatabaseAdapter;
import de.tub.citydb.database.adapter.h2.H2Adapter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;

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

			cacheDir = tempDir.getAbsolutePath() + File.separator + DefaultGMLIdManager.getInstance().generateUUID("");		
			cacheConnection = DriverManager.getConnection(cacheAdapter.getJDBCUrl(cacheDir + File.separator + "tmp", -1, null), "sa", "");			
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
