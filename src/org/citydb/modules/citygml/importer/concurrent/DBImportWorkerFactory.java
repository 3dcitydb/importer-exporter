/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.citygml.importer.concurrent;

import java.sql.SQLException;

import org.citydb.api.concurrent.Worker;
import org.citydb.api.concurrent.WorkerFactory;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.EventDispatcher;
import org.citydb.config.Config;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.importer.util.ImportLogger;
import org.citydb.modules.common.filter.ImportFilter;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.model.citygml.CityGML;

public class DBImportWorkerFactory implements WorkerFactory<CityGML> {
	private final Logger LOG = Logger.getInstance();
	
	private final DatabaseConnectionPool dbConnectionPool;
	private final JAXBBuilder jaxbBuilder;
	private final WorkerPool<DBXlink> xlinkWorkerPool;
	private final UIDCacheManager uidCacheManager;
	private final ImportFilter importFilter;
	private final ImportLogger importLogger;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBImportWorkerFactory(DatabaseConnectionPool dbConnectionPool,
			JAXBBuilder jaxbBuilder,
			WorkerPool<DBXlink> xlinkWorkerPool,
			UIDCacheManager uidCacheManager,
			ImportFilter importFilter,
			ImportLogger importLogger,
			Config config,
			EventDispatcher eventDispatcher) {
		this.dbConnectionPool = dbConnectionPool;
		this.jaxbBuilder = jaxbBuilder;
		this.xlinkWorkerPool = xlinkWorkerPool;
		this.uidCacheManager = uidCacheManager;
		this.importFilter = importFilter;
		this.importLogger = importLogger;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<CityGML> createWorker() {
		DBImportWorker dbWorker = null;

		try {
			dbWorker = new DBImportWorker(dbConnectionPool,
					jaxbBuilder,
					xlinkWorkerPool, 
					uidCacheManager,
					importFilter,
					importLogger,
					config, 
					eventDispatcher);
		} catch (SQLException e) {
			LOG.error("Failed to create import worker: " + e.getMessage());
		}

		return dbWorker;
	}
}
