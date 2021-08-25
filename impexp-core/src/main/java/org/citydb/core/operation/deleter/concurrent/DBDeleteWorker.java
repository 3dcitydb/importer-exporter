/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.operation.deleter.concurrent;

import org.citydb.config.Config;
import org.citydb.config.project.deleter.DeleteMode;
import org.citydb.config.project.global.LogLevel;
import org.citydb.config.project.global.UpdatingPersonMode;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.operation.deleter.database.DBSplittingResult;
import org.citydb.core.operation.deleter.util.DeleteLogger;
import org.citydb.core.operation.deleter.util.InternalConfig;
import org.citydb.util.concurrent.Worker;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.*;
import org.citydb.util.log.Logger;

import java.io.IOException;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class DBDeleteWorker extends Worker<DBSplittingResult> implements EventHandler {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger log = Logger.getInstance();

	private final PreparedStatement stmt;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final DeleteLogger deleteLogger;
	private final InternalConfig internalConfig;
	private final EventDispatcher eventDispatcher;
	private final DeleteMode mode;

	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;
	private int idType;

	public DBDeleteWorker(
			Connection connection,
			AbstractDatabaseAdapter databaseAdapter,
			DeleteLogger deleteLogger,
			InternalConfig internalConfig,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.databaseAdapter = databaseAdapter;
		this.deleteLogger = deleteLogger;
		this.internalConfig = internalConfig;
		this.eventDispatcher = eventDispatcher;

		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		mode = config.getDeleteConfig().getMode();
		if (mode == DeleteMode.TERMINATE) {
			StringBuilder update = new StringBuilder("update cityobject set termination_date = ?, last_modification_date = ?, updating_person = ? ");
			if (internalConfig.getReasonForUpdate() != null) {
				update.append(", reason_for_update = '").append(internalConfig.getReasonForUpdate()).append("'");
			}

			if (internalConfig.getLineage() != null) {
				update.append(", lineage = '").append(internalConfig.getLineage()).append("' ");
			}

			update.append("where id = ?");

			stmt = connection.prepareStatement(update.toString());
		} else {
			idType = databaseAdapter.getConnectionMetaData().getCityDBVersion().compareTo(4, 2, 0) < 0 ?
					Types.INTEGER :
					Types.BIGINT;

			stmt = connection.prepareCall("{? = call "
					+ databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("citydb_delete.delete_cityobject")
					+ "(?)}");
			((CallableStatement) stmt).registerOutParameter(1, idType);
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

			if (mode == DeleteMode.TERMINATE) {
				OffsetDateTime now = OffsetDateTime.now();

				OffsetDateTime terminationDate = internalConfig.getTerminationDate() != null ?
						internalConfig.getTerminationDate() :
						now;

				String updatingPerson = internalConfig.getUpdatingPersonMode() == UpdatingPersonMode.USER ?
						internalConfig.getUpdatingPerson() :
						databaseAdapter.getConnectionDetails().getUser();

				stmt.setObject(1, terminationDate);
				stmt.setObject(2, now);
				stmt.setString(3, updatingPerson);
				stmt.setLong(4, objectId);

				stmt.executeUpdate();
				deletedObjectId = objectId;
			} else {
				stmt.setObject(2, objectId, idType);
				stmt.executeUpdate();
				deletedObjectId = idType == Types.INTEGER ?
						((CallableStatement) stmt).getInt(1) :
						((CallableStatement) stmt).getLong(1);
			}

			if (deletedObjectId == objectId) {
				log.debug(work.getObjectType() + " (ID = " + objectId + ") " + (mode == DeleteMode.TERMINATE ? "terminated." : "deleted."));
				if (deleteLogger != null) {
					deleteLogger.write(work.getObjectType().getPath(), objectId, work.getGmlId());
				}
			} else {
				log.debug(work.getObjectType() + " (ID = " + objectId + ") is already deleted.");
			}

			Map<Integer, Long> objectCounter = Collections.singletonMap(work.getObjectType().getObjectClassId(), 1L);
			eventDispatcher.triggerEvent(new ObjectCounterEvent(objectCounter, eventChannel, this));

			eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1, this));
		} catch (SQLException e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent("Failed to " + mode.value() + " " + work.getObjectType() + " (ID = " + work.getId() + ").", LogLevel.ERROR, e, eventChannel, this));
		} catch (IOException e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred while updating the delete log.", LogLevel.ERROR, e, eventChannel, this));
		} catch (Throwable e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred during " + mode.value() + ".", LogLevel.ERROR, e, eventChannel, this));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel) {
			shouldWork = false;
		}
	}
}
