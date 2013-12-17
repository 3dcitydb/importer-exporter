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

import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.database.adapter.AbstractSQLAdapter;
import de.tub.citydb.database.adapter.h2.H2Adapter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.cache.model.CacheTableModelEnum;

public class CacheManager {
	private final Logger LOG = Logger.getInstance();
	private final AbstractSQLAdapter sqlAdapter;	
	private final Connection connection;	

	private final String tempDir = "C:\\Users\\cnagel\\3DCityDB-Importer-Exporter\\tmp";
	private String cacheDir;

	private ConcurrentHashMap<CacheTableModelEnum, CacheTable> cacheTables;
	private ConcurrentHashMap<CacheTableModelEnum, BranchCacheTable> branchCacheTables;

	public CacheManager(DatabaseConnectionPool dbPool, int concurrencyLevel) throws SQLException, IOException {
		//		sqlAdapter = dbPool.getActiveDatabaseAdapter().getSQLAdapter();
		//		connection = dbPool.getConnection();
		//		connection.setAutoCommit(false);

		H2Adapter h2Adapter = new H2Adapter();
		sqlAdapter = h2Adapter.getSQLAdapter();

		try {
			Class.forName(h2Adapter.getConnectionFactoryClassName());
		} catch (ClassNotFoundException e) {
			throw new SQLException(e);
		}

		cacheDir = tempDir + File.separator + DefaultGMLIdManager.getInstance().generateUUID("");		
		connection = DriverManager.getConnection(h2Adapter.getJDBCUrl(cacheDir + File.separator + "tmp", -1, null), "sa", "");			
		connection.setAutoCommit(false);

		cacheTables = new ConcurrentHashMap<CacheTableModelEnum, CacheTable>(CacheTableModelEnum.values().length, 0.75f, concurrencyLevel);
		branchCacheTables = new ConcurrentHashMap<CacheTableModelEnum, BranchCacheTable>(CacheTableModelEnum.values().length, 0.75f, concurrencyLevel);
	}

	public CacheTable createCacheTable(CacheTableModelEnum model) throws SQLException {
		CacheTable cacheTable = getOrCreateCacheTable(model);		
		if (!cacheTable.isCreated())
			cacheTable.create();

		return cacheTable;
	}

	public CacheTable createAndIndexCacheTable(CacheTableModelEnum model) throws SQLException {
		CacheTable cacheTable = getOrCreateCacheTable(model);
		if (!cacheTable.isCreated())
			cacheTable.createAndIndex();

		return cacheTable;
	}

	public BranchCacheTable createBranchCacheTable(CacheTableModelEnum model) throws SQLException {
		BranchCacheTable branchCacheTable = gerOrCreateBranchCacheTable(model);		
		if (!branchCacheTable.isCreated())
			branchCacheTable.create();

		return branchCacheTable;
	}

	public BranchCacheTable createAndIndexBranchCacheTable(CacheTableModelEnum model) throws SQLException {
		BranchCacheTable branchCacheTable = gerOrCreateBranchCacheTable(model);
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

			connection.close();
			
			try {
				deleteTempFiles(new File(cacheDir));
			} catch (SecurityException e) {
				LOG.error("Failed to delete temp directory: " + e.getMessage());
			}
		}
	}

	private CacheTable getOrCreateCacheTable(CacheTableModelEnum model) {
		CacheTable cacheTable = cacheTables.get(model);
		if (cacheTable == null) {
			CacheTable tmp = new CacheTable(model, connection, sqlAdapter);
			cacheTable = cacheTables.putIfAbsent(model, tmp);
			if (cacheTable == null)
				cacheTable = tmp;
		}

		return cacheTable;
	}

	private BranchCacheTable gerOrCreateBranchCacheTable(CacheTableModelEnum model) {
		BranchCacheTable branchCacheTable = branchCacheTables.get(model);
		if (branchCacheTable == null) {
			BranchCacheTable tmp = new BranchCacheTable(model, connection, sqlAdapter);
			branchCacheTable = branchCacheTables.putIfAbsent(model, tmp);
			if (branchCacheTable == null)
				branchCacheTable = tmp;
		}

		return branchCacheTable;
	}
	
	private void deleteTempFiles(File file) throws SecurityException {
		if (file.isDirectory()) {
			for (File nested : file.listFiles())
				deleteTempFiles(nested);
		}
		
		file.delete();
	}

}
