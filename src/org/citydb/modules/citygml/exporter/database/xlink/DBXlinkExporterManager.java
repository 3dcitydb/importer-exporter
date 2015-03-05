/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
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
