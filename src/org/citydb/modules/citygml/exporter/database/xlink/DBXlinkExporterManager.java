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
package org.citydb.modules.citygml.exporter.database.xlink;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.config.Config;
import org.citydb.database.adapter.AbstractDatabaseAdapter;

public class DBXlinkExporterManager {
	private final Connection connection;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private HashMap<DBXlinkExporterEnum, DBXlinkExporter> dbExporterMap;

	public DBXlinkExporterManager(Connection connection, AbstractDatabaseAdapter databaseAdapter, Config config, EventDispatcher eventDispatcher) {
		this.connection = connection;
		this.databaseAdapter = databaseAdapter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		dbExporterMap = new HashMap<DBXlinkExporterEnum, DBXlinkExporter>();
	}

	public DBXlinkExporter getDBXlinkExporter(DBXlinkExporterEnum dbXlinkExporterType) throws SQLException {
		DBXlinkExporter dbExporter = dbExporterMap.get(dbXlinkExporterType);

		if (dbExporter == null) {
			switch (dbXlinkExporterType) {
			case TEXTURE_IMAGE:
				dbExporter = new DBXlinkExporterTextureImage(connection, config, this);
				break;
			case LIBRARY_OBJECT:
				dbExporter = new DBXlinkExporterLibraryObject(connection, config, this);
				break;
			}

			if (dbExporter != null)
				dbExporterMap.put(dbXlinkExporterType, dbExporter);
		}

		return dbExporter;
	}
	
	public AbstractDatabaseAdapter getDatabaseAdapter() {
		return databaseAdapter;
	}

	public void propagateEvent(Event event) {
		eventDispatcher.triggerEvent(event);
	}
	
	public void close() throws SQLException {
		for (DBXlinkExporter exporter : dbExporterMap.values())
			exporter.close();
	}
	
}
