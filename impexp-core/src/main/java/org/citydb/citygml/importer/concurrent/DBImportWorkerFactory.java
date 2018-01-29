/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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

import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.xlink.DBXlink;
import org.citydb.citygml.importer.filter.CityGMLFilter;
import org.citydb.citygml.importer.util.ImportLogger;
import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerFactory;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.EventDispatcher;
import org.citydb.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.citygml.CityGML;

public class DBImportWorkerFactory implements WorkerFactory<CityGML> {
	private final Logger LOG = Logger.getInstance();

	private final Connection connection;
	private final SchemaMapping schemaMapping;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final CityGMLBuilder cityGMLBuilder;
	private final WorkerPool<DBXlink> xlinkWorkerPool;
	private final UIDCacheManager uidCacheManager;
	private final CityGMLFilter filter;
	private final ImportLogger importLogger;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBImportWorkerFactory(SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkWorkerPool,
			UIDCacheManager uidCacheManager,
			CityGMLFilter filter,
			ImportLogger importLogger,
			Config config,
			EventDispatcher eventDispatcher) {
		this.schemaMapping = schemaMapping;
		this.cityGMLBuilder = cityGMLBuilder;
		this.xlinkWorkerPool = xlinkWorkerPool;
		this.uidCacheManager = uidCacheManager;
		this.filter = filter;
		this.importLogger = importLogger;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
		
		connection = null;
		databaseAdapter = null;
	}

	public DBImportWorkerFactory(Connection connection,
			AbstractDatabaseAdapter databaseAdapter,
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkWorkerPool,
			UIDCacheManager uidCacheManager,
			CityGMLFilter filter,
			ImportLogger importLogger,
			Config config,
			EventDispatcher eventDispatcher) {
		this.connection = connection;
		this.databaseAdapter = databaseAdapter;
		this.schemaMapping = schemaMapping;
		this.cityGMLBuilder = cityGMLBuilder;
		this.xlinkWorkerPool = xlinkWorkerPool;
		this.uidCacheManager = uidCacheManager;
		this.filter = filter;
		this.importLogger = importLogger;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<CityGML> createWorker() {
		DBImportWorker dbWorker = null;

		try {
			dbWorker = connection == null ? new DBImportWorker(schemaMapping, cityGMLBuilder, xlinkWorkerPool, uidCacheManager, filter, importLogger, config, eventDispatcher) : 
				new DBImportWorker(connection, databaseAdapter, schemaMapping, cityGMLBuilder, xlinkWorkerPool, uidCacheManager, filter, importLogger, config, eventDispatcher);
		} catch (SQLException e) {
			LOG.error("Failed to create import worker: " + e.getMessage());
		}

		return dbWorker;
	}
}
