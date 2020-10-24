/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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
package org.citydb.citygml.deleter.concurrent;

import org.citydb.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.concurrent.Worker;
import org.citydb.config.Config;
import org.citydb.config.project.deleter.Continuation;
import org.citydb.config.project.deleter.DeleteMode;
import org.citydb.config.project.global.LogLevel;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.event.global.InterruptEvent;
import org.citydb.event.global.ObjectCounterEvent;
import org.citydb.event.global.ProgressBarEventType;
import org.citydb.event.global.StatusDialogProgressBar;
import org.citydb.log.Logger;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class DBDeleteWorker extends Worker<DBSplittingResult> implements EventHandler {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger log = Logger.getInstance();

	private final PreparedStatement stmt;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;

	public DBDeleteWorker(Connection connection, AbstractDatabaseAdapter databaseAdapter, Config config, EventDispatcher eventDispatcher) throws SQLException {
		this.databaseAdapter = databaseAdapter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		if (config.getProject().getDeleteConfig().getMode() == DeleteMode.TERMINATE) {
			Continuation metadata = config.getProject().getDeleteConfig().getContinuation();
			StringBuilder update = new StringBuilder("update cityobject set termination_date = ?, last_modification_date = ?, updating_person = ? ");
			if (metadata.isSetReasonForUpdate()) update.append(", reason_for_update = '").append(metadata.getReasonForUpdate()).append("'");
			if (metadata.isSetLineage()) update.append(", lineage = '").append(metadata.getLineage()).append("' ");
			update.append("where id = ?");

			stmt = connection.prepareStatement(update.toString());
		} else {
			stmt = connection.prepareCall("{? = call "
					+ databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_delete.delete_cityobject")
					+ "(?)}");
			((CallableStatement) stmt).registerOutParameter(1, Types.INTEGER);
		}
	}

	@Override
	public void interrupt() {
		shouldRun = false;
	}

	@Override
	public void run() {
		try {
			if (firstWork != null) {
				doWork(firstWork);
				firstWork = null;
			}

			while (shouldRun) {
				try {
					DBSplittingResult work = workQueue.take();
					doWork(work);
				} catch (InterruptedException ie) {
					// re-check state
				}
			}
		} finally {
			try {
				if (stmt != null)
					stmt.close();
			} catch (SQLException e) {
				log.logStackTrace(e);
			}

			eventDispatcher.removeEventHandler(this);
		}
	}

	private void doWork(DBSplittingResult work) {
		final ReentrantLock lock = this.mainLock;
		lock.lock();

		try {
			if (!shouldWork)
				return;

			long objectId = work.getId();
			long deletedObjectId;

			if (config.getProject().getDeleteConfig().getMode() == DeleteMode.TERMINATE) {
				OffsetDateTime now = OffsetDateTime.now();

				Continuation metadata = config.getProject().getDeleteConfig().getContinuation();
				OffsetDateTime terminationDate = metadata.isSetTerminationDate() ? metadata.getTerminationDate() : now;
				String updatingPerson = metadata.isUpdatingPersonModeDatabase() || !metadata.isSetUpdatingPerson() ?
						databaseAdapter.getConnectionDetails().getUser() : metadata.getUpdatingPerson();

				stmt.setObject(1, terminationDate);
				stmt.setObject(2, now);
				stmt.setString(3, updatingPerson);
				stmt.setLong(4, objectId);

				stmt.executeUpdate();
				deletedObjectId = objectId;
			} else {
				stmt.setObject(2, objectId, Types.INTEGER);
				stmt.executeUpdate();
				deletedObjectId = ((CallableStatement) stmt).getInt(1);
			}

			if (deletedObjectId == objectId) {
				log.debug(work.getObjectType().getPath() + " (ID = " + objectId + ") deleted.");
				Map<Integer, Long> objectCounter = new HashMap<>();
				objectCounter.put(work.getObjectType().getObjectClassId(), 1L);
				eventDispatcher.triggerEvent(new ObjectCounterEvent(objectCounter, eventChannel, this));
			} else
				log.warn("Failed to delete " + work.getObjectType().getPath() + " (ID = " + objectId + ").");

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1, this));
		} catch (SQLException e) {
			eventDispatcher.triggerEvent(new InterruptEvent("Failed to delete " + work.getObjectType().getPath() + " (ID = " + work.getId() + ").", LogLevel.WARN, e, eventChannel, this));
		} catch (Throwable e) {
			// this is to catch general exceptions that may occur during the export
			eventDispatcher.triggerSyncEvent(new InterruptEvent("Aborting due to an unexpected " + e.getClass().getName() + " error.", LogLevel.ERROR, e, eventChannel, this));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel) 
			shouldWork = false;		 			
	}

}
