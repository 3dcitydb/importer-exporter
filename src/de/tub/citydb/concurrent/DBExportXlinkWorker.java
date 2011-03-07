package de.tub.citydb.concurrent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import oracle.jdbc.driver.OracleConnection;

import de.tub.citydb.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.config.Config;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.db.xlink.DBXlinkEnum;
import de.tub.citydb.db.xlink.DBXlinkExternalFile;
import de.tub.citydb.db.xlink.DBXlinkExternalFileEnum;
import de.tub.citydb.db.xlink.exporter.DBXlinkExporterEnum;
import de.tub.citydb.db.xlink.exporter.DBXlinkExporterLibraryObject;
import de.tub.citydb.db.xlink.exporter.DBXlinkExporterManager;
import de.tub.citydb.db.xlink.exporter.DBXlinkExporterTextureImage;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.info.LogMessageEnum;
import de.tub.citydb.event.info.LogMessageEvent;

public class DBExportXlinkWorker implements Worker<DBXlink> {
	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<DBXlink> workQueue = null;
	private DBXlink firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final DBConnectionPool dbConnectionPool;
	private final Config config;
	private Connection connection;
	private final EventDispatcher eventDispatcher;

	private DBXlinkExporterManager xlinkExporterManager;

	public DBExportXlinkWorker(DBConnectionPool dbConnectionPool, Config config, EventDispatcher eventDispatcher) throws SQLException {
		this.dbConnectionPool = dbConnectionPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		init();
	}

	private void init() throws SQLException {
		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(true);
		((OracleConnection)connection).setDefaultRowPrefetch(50);

		xlinkExporterManager = new DBXlinkExporterManager(connection, config, eventDispatcher);
	}

	@Override
	public Thread getThread() {
		return workerThread;
	}

	@Override
	public void interrupt() {
		shouldRun = false;
		workerThread.interrupt();
	}

	@Override
	public void interruptIfIdle() {
		final ReentrantLock runLock = this.runLock;
		shouldRun = false;

		if (runLock.tryLock()) {
			try {
				workerThread.interrupt();
			} finally {
				runLock.unlock();
			}
		}
	}

	@Override
	public void setFirstWork(DBXlink firstWork) {
		this.firstWork = firstWork;
	}

	@Override
	public void setThread(Thread workerThread) {
		this.workerThread = workerThread;
	}

	@Override
	public void setWorkQueue(WorkQueue<DBXlink> workQueue) {
		this.workQueue = workQueue;
	}

	@Override
	public void run() {
		if (firstWork != null) {
			doWork(firstWork);
			firstWork = null;
		}

		while (shouldRun) {
			try {
				DBXlink work = workQueue.take();
				doWork(work);
			} catch (InterruptedException ie) {
				// re-check state
			}
		}

		if (connection != null) {
        	try {
        		connection.close();
        	} catch (SQLException e) {
        		//
        	}

        	connection = null;
        }
	}

	private void doWork(DBXlink work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			try {
				boolean success = false;
				DBXlinkEnum type = work.getXlinkType();

				switch (type) {
				case EXTERNAL_FILE:
					DBXlinkExternalFile xlink = (DBXlinkExternalFile)work;

					if (xlink.getType() == DBXlinkExternalFileEnum.TEXTURE_IMAGE) {
						DBXlinkExporterTextureImage imageExporter = (DBXlinkExporterTextureImage)xlinkExporterManager.getDBXlinkExporter(DBXlinkExporterEnum.TEXTURE_IMAGE);
						if (imageExporter != null)
							success = imageExporter.export(xlink);
					}

					else if (xlink.getType() == DBXlinkExternalFileEnum.LIBRARY_OBJECT) {
						DBXlinkExporterLibraryObject libraryObject = (DBXlinkExporterLibraryObject)xlinkExporterManager.getDBXlinkExporter(DBXlinkExporterEnum.LIBRARY_OBJECT);
						if (libraryObject != null)
							success = libraryObject.export(xlink);
					}

					break;
				}

				if (!success)
					; // do sth reasonable

			} catch (SQLException sqlEx) {
				LogMessageEvent log = new LogMessageEvent(
						"SQL-Fehler: " + sqlEx,
						LogMessageEnum.ERROR);
				eventDispatcher.triggerEvent(log);
				return;
			}

		} finally {
			runLock.unlock();
		}
	}
}
