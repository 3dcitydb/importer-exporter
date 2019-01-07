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
package org.citydb.modules.kml.concurrent;

import java.sql.SQLException;

import javax.xml.bind.JAXBContext;

import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerFactory;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.EventDispatcher;
import org.citydb.log.Logger;
import org.citydb.modules.kml.database.KmlSplittingResult;
import org.citydb.modules.kml.util.ExportTracker;
import org.citydb.query.Query;
import org.citygml4j.util.xml.SAXEventBuffer;

import net.opengis.kml._2.ObjectFactory;

public class KmlExportWorkerFactory implements WorkerFactory<KmlSplittingResult> {
	private final Logger LOG = Logger.getInstance();
	
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final SchemaMapping schemaMapping;
	private final WorkerPool<SAXEventBuffer> writerPool;
	private final Query query;
	private final ExportTracker tracker;
	private final ObjectFactory kmlFactory;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public KmlExportWorkerFactory(
			JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			SchemaMapping schemaMapping,
			WorkerPool<SAXEventBuffer> writerPool,
			ExportTracker tracker,
			Query query,
			ObjectFactory kmlFactory,
			Config config,
			EventDispatcher eventDispatcher) {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.schemaMapping = schemaMapping;
		this.writerPool = writerPool;
		this.tracker = tracker;
		this.query = query;
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
					schemaMapping,
					writerPool,
					tracker,
					query,
					kmlFactory,
					config,
					eventDispatcher);
		} catch (SQLException e) {
			LOG.error("Failed to create export worker: " + e.getMessage());
		}

		return kmlWorker;
	}

}
