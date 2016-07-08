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
