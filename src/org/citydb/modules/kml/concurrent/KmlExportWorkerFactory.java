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
package org.citydb.modules.kml.concurrent;

import java.sql.SQLException;

import javax.xml.bind.JAXBContext;

import net.opengis.kml._2.ObjectFactory;

import org.citydb.api.concurrent.Worker;
import org.citydb.api.concurrent.WorkerFactory;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.EventDispatcher;
import org.citydb.config.Config;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.kml.database.KmlSplittingResult;
import org.citydb.modules.kml.util.ExportTracker;
import org.citygml4j.util.xml.SAXEventBuffer;

public class KmlExportWorkerFactory implements WorkerFactory<KmlSplittingResult> {
	private final Logger LOG = Logger.getInstance();
	
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final DatabaseConnectionPool dbConnectionPool;
	private final WorkerPool<SAXEventBuffer> ioWriterPool;
	private final ExportTracker tracker;
	private final ObjectFactory kmlFactory;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public KmlExportWorkerFactory(
			JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			DatabaseConnectionPool dbConnectionPool,
			WorkerPool<SAXEventBuffer> ioWriterPool,
			ExportTracker tracker,
			ObjectFactory kmlFactory,
			Config config,
			EventDispatcher eventDispatcher) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.dbConnectionPool = dbConnectionPool;
		this.ioWriterPool = ioWriterPool;
		this.tracker = tracker;
		this.kmlFactory = kmlFactory;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<KmlSplittingResult> createWorker() {
		KmlExportWorker kmlWorker = null;

		try {
			kmlWorker = new KmlExportWorker(
					jaxbKmlContext,
					jaxbColladaContext,
					dbConnectionPool,
					ioWriterPool,
					tracker,
					kmlFactory,
					config,
					eventDispatcher);
		} catch (SQLException e) {
			LOG.error("Failed to create export worker: " + e.getMessage());
		}

		return kmlWorker;
	}

}
