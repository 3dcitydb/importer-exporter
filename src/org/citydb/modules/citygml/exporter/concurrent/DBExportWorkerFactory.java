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
package org.citydb.modules.citygml.exporter.concurrent;

import java.sql.SQLException;

import org.citydb.api.concurrent.Worker;
import org.citydb.api.concurrent.WorkerFactory;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.EventDispatcher;
import org.citydb.config.Config;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.modules.citygml.exporter.util.FeatureProcessorFactory;
import org.citydb.modules.common.filter.ExportFilter;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.xml.sax.SAXException;

public class DBExportWorkerFactory implements WorkerFactory<DBSplittingResult> {
	private final Logger LOG = Logger.getInstance();
	
	private final DatabaseConnectionPool dbConnectionPool;
	private final JAXBBuilder jaxbBuilder;
	private final FeatureProcessorFactory featureProcessorFactory;
	private final WorkerPool<DBXlink> xlinkExporterPool;
	private final UIDCacheManager uidCacheManager;
	private final CacheTableManager cacheTableManager;
	private final ExportFilter exportFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBExportWorkerFactory(
			DatabaseConnectionPool dbConnectionPool,
			JAXBBuilder jaxbBuilder,
			FeatureProcessorFactory featureProcessorFactory,
			WorkerPool<DBXlink> xlinkExporterPool,
			UIDCacheManager uidCacheManager,
			CacheTableManager cacheTableManager,
			ExportFilter exportFilter,
			Config config,
			EventDispatcher eventDispatcher) {
		this.dbConnectionPool = dbConnectionPool;
		this.jaxbBuilder = jaxbBuilder;
		this.featureProcessorFactory = featureProcessorFactory;
		this.xlinkExporterPool = xlinkExporterPool;
		this.uidCacheManager = uidCacheManager;
		this.cacheTableManager = cacheTableManager;
		this.exportFilter = exportFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<DBSplittingResult> createWorker() {
		DBExportWorker dbWorker = null;

		try {
			dbWorker = new DBExportWorker(
					dbConnectionPool,
					jaxbBuilder,
					featureProcessorFactory.createFeatureProcessor(),
					xlinkExporterPool,
					uidCacheManager,
					cacheTableManager,
					exportFilter,
					config,
					eventDispatcher);
		} catch (SQLException e) {
			LOG.error("Failed to create export worker: " + e.getMessage());
		} catch (SAXException e) {
			LOG.error("Failed to create export worker: " + e.getMessage());
		}

		return dbWorker;
	}

}
