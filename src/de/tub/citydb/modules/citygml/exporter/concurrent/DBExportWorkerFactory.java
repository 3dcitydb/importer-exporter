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
package de.tub.citydb.modules.citygml.exporter.concurrent;

import java.sql.SQLException;

import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.xml.sax.SAXException;

import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerFactory;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.modules.citygml.common.database.cache.CacheManager;
import de.tub.citydb.modules.citygml.common.database.gmlid.DBGmlIdLookupServerManager;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlink;
import de.tub.citydb.modules.citygml.exporter.database.content.DBSplittingResult;
import de.tub.citydb.modules.common.filter.ExportFilter;

public class DBExportWorkerFactory implements WorkerFactory<DBSplittingResult> {
	private final DatabaseConnectionPool dbConnectionPool;
	private final JAXBBuilder jaxbBuilder;
	private final WorkerPool<SAXEventBuffer> ioWriterPool;
	private final WorkerPool<DBXlink> xlinkExporterPool;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final CacheManager cacheManager;
	private final ExportFilter exportFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBExportWorkerFactory(
			DatabaseConnectionPool dbConnectionPool,
			JAXBBuilder jaxbBuilder,
			WorkerPool<SAXEventBuffer> ioWriterPool,
			WorkerPool<DBXlink> xlinkExporterPool,
			DBGmlIdLookupServerManager lookupServerManager,
			CacheManager cacheManager,
			ExportFilter exportFilter,
			Config config,
			EventDispatcher eventDispatcher) {
		this.dbConnectionPool = dbConnectionPool;
		this.jaxbBuilder = jaxbBuilder;
		this.ioWriterPool = ioWriterPool;
		this.xlinkExporterPool = xlinkExporterPool;
		this.lookupServerManager = lookupServerManager;
		this.cacheManager = cacheManager;
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
					ioWriterPool,
					xlinkExporterPool,
					lookupServerManager,
					cacheManager,
					exportFilter,
					config,
					eventDispatcher);
		} catch (SQLException sqlEx) {
			// could not instantiate DBWorker
		} catch (SAXException e) {
			// could not instantiate DBWorker
		}

		return dbWorker;
	}

}
