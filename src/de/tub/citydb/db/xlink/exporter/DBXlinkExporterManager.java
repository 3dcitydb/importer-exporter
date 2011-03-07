package de.tub.citydb.db.xlink.exporter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

import de.tub.citydb.config.Config;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;

public class DBXlinkExporterManager {
	private final Connection connection;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private HashMap<DBXlinkExporterEnum, DBXlinkExporter> dbExporterMap;

	public DBXlinkExporterManager(Connection connection, Config config, EventDispatcher eventDispatcher) {
		this.connection = connection;
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
				dbExporter = new DBXlinkExporterLibraryObject(connection, config);
				break;
			}

			if (dbExporter != null)
				dbExporterMap.put(dbXlinkExporterType, dbExporter);
		}

		return dbExporter;
	}

	public void propagateEvent(Event event) {
		eventDispatcher.triggerEvent(event);
	}
	
	public void close() throws SQLException {
		for (DBXlinkExporter exporter : dbExporterMap.values())
			exporter.close();
	}
	
}
