package org.citydb.citygml.deleter.concurrent;

import java.sql.SQLException;

import org.citydb.citygml.deleter.database.BundledDBConnection;
import org.citydb.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerFactory;
import org.citydb.event.EventDispatcher;
import org.citydb.log.Logger;

public class DBDeleteWorkerFactory implements WorkerFactory<DBSplittingResult>{
	private final Logger LOG = Logger.getInstance();
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
			LOG.error("Failed to create delete worker: " + e.getMessage());
		}
		
		return dbWorker;
	}
}
