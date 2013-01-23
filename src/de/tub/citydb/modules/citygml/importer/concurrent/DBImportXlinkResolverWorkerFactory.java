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
package de.tub.citydb.modules.citygml.importer.concurrent;

import java.sql.SQLException;

import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerFactory;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.modules.citygml.common.database.cache.CacheManager;
import de.tub.citydb.modules.citygml.common.database.gmlid.DBGmlIdLookupServerManager;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlink;
import de.tub.citydb.modules.common.filter.ImportFilter;

public class DBImportXlinkResolverWorkerFactory implements WorkerFactory<DBXlink> {
	private final DatabaseConnectionPool dbPool;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final CacheManager cacheManager;
	private final ImportFilter importFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBImportXlinkResolverWorkerFactory(DatabaseConnectionPool dbPool, 
			WorkerPool<DBXlink> tmpXlinkPool, 
			DBGmlIdLookupServerManager lookupServerManager, 
			CacheManager cacheManager, 
			ImportFilter importFilter, 
			Config config, 
			EventDispatcher eventDispatcher) {
		this.dbPool = dbPool;
		this.tmpXlinkPool = tmpXlinkPool;
		this.lookupServerManager = lookupServerManager;
		this.cacheManager = cacheManager;
		this.importFilter = importFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<DBXlink> createWorker() {
		DBImportXlinkResolverWorker dbWorker = null;

		try {
			dbWorker = new DBImportXlinkResolverWorker(dbPool, 
					tmpXlinkPool, 
					lookupServerManager, 
					cacheManager, 
					importFilter,
					config, 
					eventDispatcher);
		} catch (SQLException sqlEx) {
			// could not instantiate DBWorker
		}

		return dbWorker;
	}
}
