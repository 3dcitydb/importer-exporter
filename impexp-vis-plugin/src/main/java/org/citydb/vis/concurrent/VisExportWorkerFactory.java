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
package org.citydb.vis.concurrent;

import net.opengis.kml._2.ObjectFactory;
import org.citydb.config.Config;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.query.Query;
import org.citydb.util.concurrent.Worker;
import org.citydb.util.concurrent.WorkerFactory;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.log.Logger;
import org.citydb.vis.database.DBSplittingResult;
import org.citydb.vis.util.ExportTracker;
import org.citygml4j.util.xml.SAXEventBuffer;

import javax.xml.bind.JAXBContext;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class VisExportWorkerFactory implements WorkerFactory<DBSplittingResult> {
	private final Logger log = Logger.getInstance();

	private final Path outputFile;
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final WorkerPool<SAXEventBuffer> writerPool;
	private final Query query;
	private final ExportTracker tracker;
	private final ObjectFactory kmlFactory;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public VisExportWorkerFactory(Path outputFile,
								  JAXBContext jaxbKmlContext,
								  JAXBContext jaxbColladaContext,
								  WorkerPool<SAXEventBuffer> writerPool,
								  ExportTracker tracker,
								  Query query,
								  ObjectFactory kmlFactory,
								  Config config,
								  EventDispatcher eventDispatcher) {
		this.outputFile = outputFile;
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.writerPool = writerPool;
		this.tracker = tracker;
		this.query = query;
		this.kmlFactory = kmlFactory;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<DBSplittingResult> createWorker() {
		VisExportWorker visWorker = null;

		try {
			AbstractDatabaseAdapter databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
			Connection connection = DatabaseConnectionPool.getInstance().getConnection();
			connection.setAutoCommit(false);

			visWorker = new VisExportWorker(outputFile, connection, databaseAdapter, jaxbKmlContext, jaxbColladaContext,
					writerPool, tracker, query, kmlFactory, config, eventDispatcher);
		} catch (SQLException e) {
			log.error("Failed to create export worker.", e);
		}

		return visWorker;
	}

}
