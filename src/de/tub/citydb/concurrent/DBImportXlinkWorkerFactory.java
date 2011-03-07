package de.tub.citydb.concurrent;

import de.tub.citydb.config.Config;
import de.tub.citydb.db.temp.DBTempTableManager;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.event.EventDispatcher;

public class DBImportXlinkWorkerFactory implements WorkerFactory<DBXlink> {
	private final DBTempTableManager dbTempTableManager;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBImportXlinkWorkerFactory(DBTempTableManager dbTempTableManager, Config config, EventDispatcher eventDispatcher) {
		this.dbTempTableManager = dbTempTableManager;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<DBXlink> getWorker() {
		return new DBImportXlinkWorker(dbTempTableManager, config, eventDispatcher);
	}
}
