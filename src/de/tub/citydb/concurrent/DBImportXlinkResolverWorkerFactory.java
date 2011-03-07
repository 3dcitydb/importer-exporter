package de.tub.citydb.concurrent;

import java.sql.SQLException;

import de.tub.citydb.config.Config;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.cache.DBGmlIdLookupServerManager;
import de.tub.citydb.db.temp.DBTempTableManager;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.filter.ImportFilter;

public class DBImportXlinkResolverWorkerFactory implements WorkerFactory<DBXlink> {
	private final DBConnectionPool dbPool;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final DBTempTableManager dbTempTableManager;
	private final ImportFilter importFilter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	public DBImportXlinkResolverWorkerFactory(DBConnectionPool dbPool, 
			WorkerPool<DBXlink> tmpXlinkPool, 
			DBGmlIdLookupServerManager lookupServerManager, 
			DBTempTableManager dbTempTableManager, 
			ImportFilter importFilter, 
			Config config, 
			EventDispatcher eventDispatcher) {
		this.dbPool = dbPool;
		this.tmpXlinkPool = tmpXlinkPool;
		this.lookupServerManager = lookupServerManager;
		this.dbTempTableManager = dbTempTableManager;
		this.importFilter = importFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
	}

	@Override
	public Worker<DBXlink> getWorker() {
		DBImportXlinkResolverWorker dbWorker = null;

		try {
			dbWorker = new DBImportXlinkResolverWorker(dbPool, 
					tmpXlinkPool, 
					lookupServerManager, 
					dbTempTableManager, 
					importFilter,
					config, 
					eventDispatcher);
		} catch (SQLException sqlEx) {
			// could not instantiate DBWorker
		}

		return dbWorker;
	}
}
