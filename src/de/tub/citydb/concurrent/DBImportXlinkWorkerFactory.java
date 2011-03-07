package de.tub.citydb.concurrent;

import de.tub.citydb.config.Config;
import de.tub.citydb.db.cache.CacheManager;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.event.EventDispatcher;

public class DBImportXlinkWorkerFactory implements WorkerFactory<DBXlink> {
	private final CacheManager cacheManager;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBImportXlinkWorkerFactory(CacheManager cacheManager, Config config, EventDispatcher eventDispatcher) {
		this.cacheManager = cacheManager;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<DBXlink> getWorker() {
		return new DBImportXlinkWorker(cacheManager, config, eventDispatcher);
	}
}
