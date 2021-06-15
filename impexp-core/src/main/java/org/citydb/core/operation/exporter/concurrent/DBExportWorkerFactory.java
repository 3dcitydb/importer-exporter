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
package org.citydb.core.operation.exporter.concurrent;

import org.citydb.util.concurrent.Worker;
import org.citydb.util.concurrent.WorkerFactory;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.log.Logger;
import org.citydb.core.operation.common.cache.CacheTableManager;
import org.citydb.core.operation.common.cache.IdCacheManager;
import org.citydb.core.operation.common.xlink.DBXlink;
import org.citydb.core.operation.exporter.CityGMLExportException;
import org.citydb.core.operation.exporter.database.content.DBSplittingResult;
import org.citydb.core.operation.exporter.util.InternalConfig;
import org.citydb.core.operation.exporter.writer.FeatureWriter;
import org.citydb.core.query.Query;
import org.citygml4j.builder.jaxb.CityGMLBuilder;

import java.sql.Connection;
import java.sql.SQLException;

public class DBExportWorkerFactory implements WorkerFactory<DBSplittingResult> {
	private final Logger log = Logger.getInstance();

	private final SchemaMapping schemaMapping;
	private final CityGMLBuilder cityGMLBuilder;
	private final FeatureWriter featureWriter;
	private final WorkerPool<DBXlink> xlinkExporterPool;
	private final IdCacheManager idCacheManager;
	private final CacheTableManager cacheTableManager;
	private final Query query;
	private final InternalConfig internalConfig;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBExportWorkerFactory(SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			FeatureWriter featureWriter,
			WorkerPool<DBXlink> xlinkExporterPool,
			IdCacheManager idCacheManager,
			CacheTableManager cacheTableManager,
			Query query,
			InternalConfig internalConfig,
			Config config,
			EventDispatcher eventDispatcher) {
		this.schemaMapping = schemaMapping;
		this.cityGMLBuilder = cityGMLBuilder;
		this.featureWriter = featureWriter;
		this.xlinkExporterPool = xlinkExporterPool;
		this.idCacheManager = idCacheManager;
		this.cacheTableManager = cacheTableManager;
		this.query = query;
		this.internalConfig = internalConfig;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<DBSplittingResult> createWorker() {
		DBExportWorker dbWorker = null;

		try {
			AbstractDatabaseAdapter databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
			Connection connection = DatabaseConnectionPool.getInstance().getConnection();
			connection.setAutoCommit(false);

			dbWorker = new DBExportWorker(connection, databaseAdapter, schemaMapping, cityGMLBuilder, featureWriter,
					xlinkExporterPool, idCacheManager, cacheTableManager, query, internalConfig, config, eventDispatcher);
		} catch (CityGMLExportException | SQLException e) {
			log.error("Failed to create export worker.", e);
		}

		return dbWorker;
	}

}
