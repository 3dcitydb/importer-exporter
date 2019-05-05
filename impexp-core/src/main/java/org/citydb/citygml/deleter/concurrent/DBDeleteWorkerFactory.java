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
package org.citydb.citygml.deleter.concurrent;

import java.sql.SQLException;

import org.citydb.citygml.deleter.util.BundledDBConnection;
import org.citydb.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerFactory;
import org.citydb.event.EventDispatcher;
import org.citydb.log.Logger;

public class DBDeleteWorkerFactory implements WorkerFactory<DBSplittingResult>{
	private final Logger log = Logger.getInstance();
	private final EventDispatcher eventDispatcher;
	private final BundledDBConnection bundledConnection;

	public DBDeleteWorkerFactory(EventDispatcher eventDispatcher, BundledDBConnection bundledConnection) {
		this.eventDispatcher = eventDispatcher;
		this.bundledConnection = bundledConnection;
	}
	
	@Override
	public Worker<DBSplittingResult> createWorker() {	
		DBDeleteWorker dbWorker = null;
	
		try {	
			dbWorker = new DBDeleteWorker(eventDispatcher, bundledConnection);
		} catch (SQLException e) {
			log.error("Failed to create delete worker: " + e.getMessage());
		}
		
		return dbWorker;
	}
}
