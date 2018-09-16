/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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
package org.citydb.citygml.exporter.concurrent;

import java.sql.SQLException;

import org.citydb.citygml.common.database.cache.CacheTableManager;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.xlink.DBXlink;
import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.citygml.exporter.writer.FeatureWriter;
import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerFactory;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.EventDispatcher;
import org.citydb.log.Logger;
import org.citydb.query.Query;
import org.citygml4j.builder.jaxb.CityGMLBuilder;

public class DBExportWorkerFactory implements WorkerFactory<DBSplittingResult> {
	private final Logger LOG = Logger.getInstance();
	
	private final SchemaMapping schemaMapping;
	private final CityGMLBuilder cityGMLBuilder;
	private final FeatureWriter featureWriter;
	private final WorkerPool<DBXlink> xlinkExporterPool;
	private final UIDCacheManager uidCacheManager;
	private final CacheTableManager cacheTableManager;
	private final Query query;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBExportWorkerFactory(
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			FeatureWriter featureWriter,
			WorkerPool<DBXlink> xlinkExporterPool,
			UIDCacheManager uidCacheManager,
			CacheTableManager cacheTableManager,
			Query query,
			Config config,
			EventDispatcher eventDispatcher) {
		this.schemaMapping = schemaMapping;
		this.cityGMLBuilder = cityGMLBuilder;
		this.featureWriter = featureWriter;
		this.xlinkExporterPool = xlinkExporterPool;
		this.uidCacheManager = uidCacheManager;
		this.cacheTableManager = cacheTableManager;
		this.query = query;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<DBSplittingResult> createWorker() {
		DBExportWorker dbWorker = null;

		try {
			dbWorker = new DBExportWorker(
					schemaMapping,
					cityGMLBuilder,
					featureWriter,
					xlinkExporterPool,
					uidCacheManager,
					cacheTableManager,
					query,
					config,
					eventDispatcher);
		} catch (CityGMLExportException e) {
			LOG.error("Failed to create export worker: " + e.getMessage());
		} catch (SQLException e) {
			LOG.error("Failed to create export worker: " + e.getMessage());
		} 

		return dbWorker;
	}

}
