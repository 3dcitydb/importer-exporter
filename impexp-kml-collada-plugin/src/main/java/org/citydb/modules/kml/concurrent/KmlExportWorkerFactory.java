/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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

import net.opengis.kml._2.ObjectFactory;
import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerFactory;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.event.EventDispatcher;
import org.citydb.log.Logger;
import org.citydb.modules.kml.database.KmlSplittingResult;
import org.citydb.modules.kml.util.ExportTracker;
import org.citydb.query.Query;
import org.citygml4j.util.xml.SAXEventBuffer;

import javax.xml.bind.JAXBContext;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public class KmlExportWorkerFactory implements WorkerFactory<KmlSplittingResult> {
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

	public KmlExportWorkerFactory(Path outputFile,
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
	public Worker<KmlSplittingResult> createWorker() {
		KmlExportWorker kmlWorker = null;

		try {
			Connection connection = DatabaseConnectionPool.getInstance().getConnection();
			connection.setAutoCommit(false);

			// try and change workspace if needed
			AbstractDatabaseAdapter databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
			if (databaseAdapter.hasVersioningSupport()) {
				Workspace workspace = config.getDatabaseConfig().getWorkspaces().getKmlExportWorkspace();
				databaseAdapter.getWorkspaceManager().gotoWorkspace(connection, workspace);
			}

			kmlWorker = new KmlExportWorker(outputFile, connection, databaseAdapter, jaxbKmlContext, jaxbColladaContext,
					writerPool, tracker, query, kmlFactory, config, eventDispatcher);
		} catch (SQLException e) {
			log.error("Failed to create export worker.", e);
		}

		return kmlWorker;
	}

}
