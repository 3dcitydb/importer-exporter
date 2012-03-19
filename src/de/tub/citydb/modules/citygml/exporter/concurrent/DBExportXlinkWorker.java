/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.citygml.exporter.concurrent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlink;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkEnum;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkLibraryObject;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFile;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlinkTextureFileEnum;
import de.tub.citydb.modules.citygml.exporter.database.xlink.DBXlinkExporterEnum;
import de.tub.citydb.modules.citygml.exporter.database.xlink.DBXlinkExporterLibraryObject;
import de.tub.citydb.modules.citygml.exporter.database.xlink.DBXlinkExporterManager;
import de.tub.citydb.modules.citygml.exporter.database.xlink.DBXlinkExporterTextureImage;

public class DBExportXlinkWorker implements Worker<DBXlink> {
	private final Logger LOG = Logger.getInstance();

	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<DBXlink> workQueue = null;
	private DBXlink firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final DatabaseConnectionPool dbConnectionPool;
	private final Config config;
	private Connection connection;
	private final EventDispatcher eventDispatcher;

	private DBXlinkExporterManager xlinkExporterManager;

	public DBExportXlinkWorker(DatabaseConnectionPool dbConnectionPool, Config config, EventDispatcher eventDispatcher) throws SQLException {
		this.dbConnectionPool = dbConnectionPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		init();
	}

	private void init() throws SQLException {
		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(false);

		// try and change workspace for the connection if needed
		Database database = config.getProject().getDatabase();
		dbConnectionPool.gotoWorkspace(
				connection, 
				database.getWorkspaces().getExportWorkspace());
		
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
		try {
			if (firstWork != null && shouldRun) {
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
			
			try {
				xlinkExporterManager.close();
			} catch (SQLException e) {
				//
			}
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					//
				}

				connection = null;
			}
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
				case TEXTURE_FILE:
					DBXlinkTextureFile texFile = (DBXlinkTextureFile)work;

					if (texFile.getType() == DBXlinkTextureFileEnum.TEXTURE_IMAGE) {
						DBXlinkExporterTextureImage imageExporter = (DBXlinkExporterTextureImage)xlinkExporterManager.getDBXlinkExporter(DBXlinkExporterEnum.TEXTURE_IMAGE);
						if (imageExporter != null)
							success = imageExporter.export(texFile);
					}

					break;

				case LIBRARY_OBJECT:
					DBXlinkLibraryObject libObject = (DBXlinkLibraryObject)work;
					DBXlinkExporterLibraryObject libraryObject = (DBXlinkExporterLibraryObject)xlinkExporterManager.getDBXlinkExporter(DBXlinkExporterEnum.LIBRARY_OBJECT);
					if (libraryObject != null)
						success = libraryObject.export(libObject);

					break;
				}

				if (!success)
					; // do sth reasonable

			} catch (SQLException sqlEx) {
				LOG.error("SQL error: " + sqlEx.getMessage());
				return;
			}

		} finally {
			runLock.unlock();
		}
	}
}
