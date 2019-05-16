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
import org.citydb.config.project.deleter.DeleteConfig;
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
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class DBDeleteWorker extends Worker<DBSplittingResult> implements EventHandler {
	private final ReentrantLock mainLock = new ReentrantLock();
	private final Logger log = Logger.getInstance();

	private final PreparedStatement stmt;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final DeleteConfig config;
	private final EventDispatcher eventDispatcher;

	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;
	
	public DBDeleteWorker(Connection connection, AbstractDatabaseAdapter databaseAdapter, DeleteConfig config, EventDispatcher eventDispatcher) throws SQLException {
		this.databaseAdapter = databaseAdapter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		if (config.getMode() == DeleteMode.TERMINATE) {
			StringBuilder update = new StringBuilder("update cityobject set termination_date = ?, last_modification_date = ?, updating_person = ? ");
			if (config.isSetReasonForUpdate()) update.append(", reason_for_update = ? ");
			if (config.isSetLineage()) update.append(", lineage = ? ");
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
				lockAndDoWork(firstWork);
				firstWork = null;
			}

			while (shouldRun) {
				try {
					DBSplittingResult work = workQueue.take();
					lockAndDoWork(work);					
				} catch (InterruptedException ie) {
					// re-check state
				}
			}
		} finally {
			shutdown();
		}
	}
	
	private void lockAndDoWork(DBSplittingResult work) {
		final ReentrantLock lock = this.mainLock;
		lock.lock();
		
		try {
			doWork(work);
		} finally {
			lock.unlock();
		}
	}
	
	public void doWork(DBSplittingResult work) {
		if (!shouldWork)
			return;
		
		long objectId = work.getId();
		int objectclassId = work.getObjectType().getObjectClassId();
		String objectclassName = work.getObjectType().getPath(); 
		boolean accept = false;  
		
		try {
			long deletedObjectId;

			if (config.getMode() == DeleteMode.TERMINATE) {
				LocalDateTime terminationDate = config.isSetTerminationDate() ? config.getTerminationDate() : LocalDateTime.now();
				String updatingPerson = config.isSetUpdatingPerson() ? config.getUpdatingPerson() : databaseAdapter.getConnectionDetails().getUser();

				int i = 1;
				stmt.setTimestamp(i++, Timestamp.valueOf(terminationDate));
				stmt.setTimestamp(i++, Timestamp.valueOf(LocalDateTime.now()));
				stmt.setString(i++, updatingPerson);
				if (config.isSetReasonForUpdate()) stmt.setString(i++, config.getReasonForUpdate());
				if (config.isSetLineage()) stmt.setString(i++, config.getLineage());
				stmt.setLong(i, objectId);

				stmt.executeUpdate();
				deletedObjectId = objectId;
			} else {
				stmt.setObject(2, objectId, Types.INTEGER);
				stmt.executeUpdate();
				deletedObjectId = ((CallableStatement) stmt).getInt(1);
			}

			if (deletedObjectId == objectId) {
				log.debug(objectclassName + " (ID = " + objectId + ") deleted.");
				accept = true;
			} 				
			else {
				log.warn("Failed to delete " + objectclassName + " (ID = " + objectId + ").");
			}
		} catch (SQLException e) {
			eventDispatcher.triggerEvent(new InterruptEvent(
					"Failed to delete " + objectclassName + " (ID = " + objectId + "). Abort and rollback transactions.",
					LogLevel.WARN, e, eventChannel, this));	
		} finally {
			updateDeleteContext(objectclassId, accept);
		}		
	}

	public void shutdown() {
		try {
			if (stmt != null)
				stmt.close();
		} catch (SQLException e) {
			log.logStackTrace(e);
		} 
		
		eventDispatcher.removeEventHandler(this);
	}

	private void updateDeleteContext(int objectclassId, boolean accept) {
		if (accept) {
			Map<Integer, Long> objectCounter = new HashMap<>();
			objectCounter.put(objectclassId, 1L);
			eventDispatcher.triggerEvent(new ObjectCounterEvent(objectCounter, this));
		}

		eventDispatcher.triggerEvent(new StatusDialogProgressBar(ProgressBarEventType.UPDATE, 1, this));
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel) 
			shouldWork = false;		 			
	}

}
