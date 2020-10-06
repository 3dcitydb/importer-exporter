/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.importer.concurrent;

import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.xlink.DBXlink;
import org.citydb.citygml.importer.filter.CityGMLFilter;
import org.citydb.citygml.importer.util.AffineTransformer;
import org.citydb.citygml.importer.util.ImportLogger;
import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerFactory;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.ConnectionManager;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.EventDispatcher;
import org.citydb.file.InputFile;
import org.citydb.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.citygml.CityGML;

import java.sql.Connection;
import java.sql.SQLException;

public class DBImportWorkerFactory implements WorkerFactory<CityGML> {
	private final Logger log = Logger.getInstance();

	private final InputFile inputFile;
	private final ConnectionManager connectionManager;
	private final boolean isManagedTransaction;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final SchemaMapping schemaMapping;
	private final CityGMLBuilder cityGMLBuilder;
	private final WorkerPool<DBXlink> xlinkWorkerPool;
	private final UIDCacheManager uidCacheManager;
	private final CityGMLFilter filter;
	private final AffineTransformer affineTransformer;
	private final ImportLogger importLogger;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBImportWorkerFactory(InputFile inputFile,
			ConnectionManager connectionManager,
			boolean isManagedTransaction,
			AbstractDatabaseAdapter databaseAdapter,
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkWorkerPool,
			UIDCacheManager uidCacheManager,
			CityGMLFilter filter,
			AffineTransformer affineTransformer,
			ImportLogger importLogger,
			Config config,
			EventDispatcher eventDispatcher) {
		this.inputFile = inputFile;
		this.connectionManager = connectionManager;
		this.isManagedTransaction = isManagedTransaction;
		this.databaseAdapter = databaseAdapter;
		this.schemaMapping = schemaMapping;
		this.cityGMLBuilder = cityGMLBuilder;
		this.xlinkWorkerPool = xlinkWorkerPool;
		this.uidCacheManager = uidCacheManager;
		this.filter = filter;
		this.affineTransformer = affineTransformer;
		this.importLogger = importLogger;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	public DBImportWorkerFactory(InputFile inputFile,
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkWorkerPool,
			UIDCacheManager uidCacheManager,
			CityGMLFilter filter,
			AffineTransformer affineTransformer,
			ImportLogger importLogger,
			Config config,
			EventDispatcher eventDispatcher) {
		this(inputFile, DatabaseConnectionPool.getInstance(), false, DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter(), schemaMapping,
				cityGMLBuilder, xlinkWorkerPool, uidCacheManager, filter, affineTransformer, importLogger, config, eventDispatcher);
	}

	public DBImportWorkerFactory(InputFile inputFile,
			ConnectionManager connectionManager,
			AbstractDatabaseAdapter databaseAdapter,
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkWorkerPool,
			UIDCacheManager uidCacheManager,
			CityGMLFilter filter,
			AffineTransformer affineTransformer,
			ImportLogger importLogger,
			Config config,
			EventDispatcher eventDispatcher) {
		this(inputFile, connectionManager, true, databaseAdapter, schemaMapping, cityGMLBuilder, xlinkWorkerPool, uidCacheManager,
				filter, affineTransformer, importLogger, config, eventDispatcher);
	}

	@Override
	public Worker<CityGML> createWorker() {
		DBImportWorker dbWorker = null;

		try {
			Connection connection = connectionManager.getConnection();
			if (!isManagedTransaction)
				connection.setAutoCommit(false);

			// try and change workspace for both connections if needed
			if (databaseAdapter.hasVersioningSupport()) {
				Workspace workspace = config.getProject().getDatabase().getWorkspaces().getImportWorkspace();
				databaseAdapter.getWorkspaceManager().gotoWorkspace(connection, workspace);
			}

			dbWorker = new DBImportWorker(inputFile, connection, isManagedTransaction, databaseAdapter, schemaMapping, cityGMLBuilder,
					xlinkWorkerPool, uidCacheManager, filter, affineTransformer, importLogger, config, eventDispatcher);
		} catch (SQLException e) {
			log.error("Failed to create import worker.", e);
		}

		return dbWorker;
	}
}
