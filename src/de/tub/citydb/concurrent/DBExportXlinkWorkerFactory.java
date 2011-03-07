package de.tub.citydb.concurrent;

import java.sql.SQLException;

import de.tub.citydb.config.Config;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.event.EventDispatcher;

public class DBExportXlinkWorkerFactory implements WorkerFactory<DBXlink> {
	private final DBConnectionPool dbConnectionPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBExportXlinkWorkerFactory(DBConnectionPool dbConnectionPool, Config config, EventDispatcher eventDispatcher) {
		this.dbConnectionPool = dbConnectionPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<DBXlink> getWorker() {
		DBExportXlinkWorker dbWorker = null;

		try {
			dbWorker = new DBExportXlinkWorker(dbConnectionPool, config, eventDispatcher);
		} catch (SQLException sqlEx) {
			// could not instantiate DBWorker
		}

		return dbWorker;
	}
}
