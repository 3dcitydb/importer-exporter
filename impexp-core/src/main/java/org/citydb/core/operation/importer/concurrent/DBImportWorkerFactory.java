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
package org.citydb.core.operation.importer.concurrent;

import org.citydb.core.concurrent.Worker;
import org.citydb.core.concurrent.WorkerFactory;
import org.citydb.core.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.connection.ConnectionManager;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.event.EventDispatcher;
import org.citydb.core.log.Logger;
import org.citydb.core.operation.common.cache.IdCacheManager;
import org.citydb.core.operation.common.xlink.DBXlink;
import org.citydb.core.operation.importer.filter.CityGMLFilter;
import org.citydb.core.operation.importer.util.AffineTransformer;
import org.citydb.core.operation.importer.util.ImportLogger;
import org.citydb.core.operation.importer.util.InternalConfig;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.citygml.CityGML;

import java.sql.Connection;
import java.sql.SQLException;

public class DBImportWorkerFactory implements WorkerFactory<CityGML> {
	private final Logger log = Logger.getInstance();

	private final ConnectionManager connectionManager;
	private final boolean isManagedTransaction;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final SchemaMapping schemaMapping;
	private final CityGMLBuilder cityGMLBuilder;
	private final WorkerPool<DBXlink> xlinkWorkerPool;
	private final IdCacheManager idCacheManager;
	private final CityGMLFilter filter;
	private final AffineTransformer affineTransformer;
	private final ImportLogger importLogger;
	private final InternalConfig internalConfig;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBImportWorkerFactory(ConnectionManager connectionManager,
			boolean isManagedTransaction,
			AbstractDatabaseAdapter databaseAdapter,
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkWorkerPool,
			IdCacheManager idCacheManager,
			CityGMLFilter filter,
			AffineTransformer affineTransformer,
			ImportLogger importLogger,
			InternalConfig internalConfig,
			Config config,
			EventDispatcher eventDispatcher) {
		this.connectionManager = connectionManager;
		this.isManagedTransaction = isManagedTransaction;
		this.databaseAdapter = databaseAdapter;
		this.schemaMapping = schemaMapping;
		this.cityGMLBuilder = cityGMLBuilder;
		this.xlinkWorkerPool = xlinkWorkerPool;
		this.idCacheManager = idCacheManager;
		this.filter = filter;
		this.affineTransformer = affineTransformer;
		this.importLogger = importLogger;
		this.internalConfig = internalConfig;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	public DBImportWorkerFactory(SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkWorkerPool,
			IdCacheManager idCacheManager,
			CityGMLFilter filter,
			AffineTransformer affineTransformer,
			ImportLogger importLogger,
			InternalConfig internalConfig,
			Config config,
			EventDispatcher eventDispatcher) {
		this(DatabaseConnectionPool.getInstance(), false, DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter(),
				schemaMapping, cityGMLBuilder, xlinkWorkerPool, idCacheManager, filter, affineTransformer, importLogger,
				internalConfig, config, eventDispatcher);
	}

	public DBImportWorkerFactory(ConnectionManager connectionManager,
			AbstractDatabaseAdapter databaseAdapter,
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkWorkerPool,
			IdCacheManager idCacheManager,
			CityGMLFilter filter,
			AffineTransformer affineTransformer,
			ImportLogger importLogger,
			InternalConfig internalConfig,
			Config config,
			EventDispatcher eventDispatcher) {
		this(connectionManager, true, databaseAdapter, schemaMapping, cityGMLBuilder, xlinkWorkerPool, idCacheManager,
				filter, affineTransformer, importLogger, internalConfig, config, eventDispatcher);
	}

	@Override
	public Worker<CityGML> createWorker() {
		DBImportWorker dbWorker = null;

		try {
			Connection connection = connectionManager.getConnection();
			if (!isManagedTransaction) {
				connection.setAutoCommit(false);
			}

			dbWorker = new DBImportWorker(connection, isManagedTransaction, databaseAdapter, schemaMapping, cityGMLBuilder,
					xlinkWorkerPool, idCacheManager, filter, affineTransformer, importLogger, internalConfig, config, eventDispatcher);
		} catch (SQLException e) {
			log.error("Failed to create import worker.", e);
		}

		return dbWorker;
	}
}
