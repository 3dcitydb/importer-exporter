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
package org.citydb.operation.importer.concurrent;

import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerFactory;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.ConnectionManager;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.event.EventDispatcher;
import org.citydb.file.InputFile;
import org.citydb.log.Logger;
import org.citydb.operation.common.cache.CacheTableManager;
import org.citydb.operation.common.cache.IdCacheManager;
import org.citydb.operation.common.xlink.DBXlink;

import java.sql.Connection;
import java.sql.SQLException;

public class DBImportXlinkResolverWorkerFactory implements WorkerFactory<DBXlink> {
	private final Logger log = Logger.getInstance();

	private final InputFile inputFile;
	private final ConnectionManager connectionManager;
	private final boolean isManagedTransaction;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final IdCacheManager idCacheManager;
	private final CacheTableManager cacheTableManager;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBImportXlinkResolverWorkerFactory(InputFile inputFile,
			ConnectionManager connectionManager,
			boolean isManagedTransaction,
			AbstractDatabaseAdapter databaseAdapter,
			WorkerPool<DBXlink> tmpXlinkPool,
			IdCacheManager idCacheManager,
			CacheTableManager cacheTableManager,
			Config config,
			EventDispatcher eventDispatcher) {
		this.inputFile = inputFile;
		this.connectionManager = connectionManager;
		this.isManagedTransaction = isManagedTransaction;
		this.databaseAdapter = databaseAdapter;
		this.tmpXlinkPool = tmpXlinkPool;
		this.idCacheManager = idCacheManager;
		this.cacheTableManager = cacheTableManager;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	public DBImportXlinkResolverWorkerFactory(InputFile inputFile,
			WorkerPool<DBXlink> tmpXlinkPool,
			IdCacheManager idCacheManager,
			CacheTableManager cacheTableManager, 
			Config config, 
			EventDispatcher eventDispatcher) {
		this(inputFile, DatabaseConnectionPool.getInstance(), false, DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter(),
				tmpXlinkPool, idCacheManager, cacheTableManager, config, eventDispatcher);
	}
	
	public DBImportXlinkResolverWorkerFactory(InputFile inputFile,
			ConnectionManager connectionManager,
			AbstractDatabaseAdapter databaseAdapter,
			WorkerPool<DBXlink> tmpXlinkPool, 
			IdCacheManager idCacheManager,
			CacheTableManager cacheTableManager, 
			Config config, 
			EventDispatcher eventDispatcher) {
		this(inputFile, connectionManager, true, databaseAdapter, tmpXlinkPool, idCacheManager, cacheTableManager, config, eventDispatcher);
	}

	@Override
	public Worker<DBXlink> createWorker() {
		DBImportXlinkResolverWorker dbWorker = null;

		try {
			Connection connection = connectionManager.getConnection();
			if (!isManagedTransaction) {
				connection.setAutoCommit(false);
			}

			dbWorker = new DBImportXlinkResolverWorker(inputFile, connection, isManagedTransaction, databaseAdapter,
					tmpXlinkPool, idCacheManager, cacheTableManager, config, eventDispatcher);
		} catch (SQLException e) {
			log.error("Failed to create XLink resolver worker.", e);
		}

		return dbWorker;
	}
}
