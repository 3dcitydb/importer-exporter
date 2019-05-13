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
package org.citydb.citygml.exporter.concurrent;

import org.citydb.citygml.common.database.xlink.DBXlink;
import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerFactory;
import org.citydb.config.Config;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.event.EventDispatcher;
import org.citydb.file.OutputFile;
import org.citydb.log.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class DBExportXlinkWorkerFactory implements WorkerFactory<DBXlink> {
	private final Logger log = Logger.getInstance();

	private final OutputFile outputFile;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBExportXlinkWorkerFactory(OutputFile outputFile, Config config, EventDispatcher eventDispatcher) {
		this.outputFile = outputFile;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<DBXlink> createWorker() {
		DBExportXlinkWorker dbWorker = null;

		try {
			Connection connection = DatabaseConnectionPool.getInstance().getConnection();
			connection.setAutoCommit(false);

			// try and change workspace for the connection if needed
			AbstractDatabaseAdapter databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
			if (databaseAdapter.hasVersioningSupport()) {
				databaseAdapter.getWorkspaceManager().gotoWorkspace(
						connection,
						config.getProject().getDatabase().getWorkspaces().getExportWorkspace());
			}

			dbWorker = new DBExportXlinkWorker(outputFile, connection, databaseAdapter, config, eventDispatcher);
		} catch (SQLException e) {
			log.error("Failed to create XLink export worker: " + e.getMessage());
		}

		return dbWorker;
	}
}
