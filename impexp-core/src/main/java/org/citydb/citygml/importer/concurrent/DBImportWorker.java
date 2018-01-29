/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.citygml.importer.concurrent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.xlink.DBXlink;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.database.content.CityGMLImportManager;
import org.citydb.citygml.importer.filter.CityGMLFilter;
import org.citydb.citygml.importer.util.ImportLogger;
import org.citydb.citygml.importer.util.ImportLogger.ImportLogEntry;
import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.internal.Internal;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.global.LogLevel;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.CounterEvent;
import org.citydb.event.global.CounterType;
import org.citydb.event.global.EventType;
import org.citydb.event.global.GeometryCounterEvent;
import org.citydb.event.global.InterruptEvent;
import org.citydb.event.global.ObjectCounterEvent;
import org.citydb.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.util.bbox.BoundingBoxOptions;

public class DBImportWorker extends Worker<CityGML> implements EventHandler {
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;

	private final CityGMLFilter filter;
	private final ImportLogger importLogger;
	private final EventDispatcher eventDispatcher;

	private Connection connection;
	private int updateCounter = 0;
	private int commitAfter = 20;
	private boolean globalTransaction;

	private BoundingBoxOptions bboxOptions;
	private CityGMLImportManager importer;	

	public DBImportWorker(SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkPool,
			UIDCacheManager uidCacheManager,
			CityGMLFilter filter,
			ImportLogger importLogger,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.filter = filter;
		this.importLogger = importLogger;
		this.eventDispatcher = eventDispatcher;

		AbstractDatabaseAdapter databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		connection = DatabaseConnectionPool.getInstance().getConnection();
		connection.setAutoCommit(false);
		globalTransaction = false;

		// try and change workspace for both connections if needed
		if (databaseAdapter.hasVersioningSupport()) {
			Workspace workspace = config.getProject().getDatabase().getWorkspaces().getImportWorkspace();
			databaseAdapter.getWorkspaceManager().gotoWorkspace(connection, workspace);
		}

		init(databaseAdapter, schemaMapping, cityGMLBuilder, xlinkPool, uidCacheManager, config);
	}

	public DBImportWorker(Connection connection,
			AbstractDatabaseAdapter databaseAdapter,
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkPool,
			UIDCacheManager uidCacheManager,
			CityGMLFilter filter,
			ImportLogger importLogger,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.connection = connection;
		this.filter = filter;
		this.importLogger = importLogger;
		this.eventDispatcher = eventDispatcher;

		globalTransaction = true;
		init(databaseAdapter, schemaMapping, cityGMLBuilder, xlinkPool, uidCacheManager, config);
	}

	private void init(AbstractDatabaseAdapter databaseAdapter, 
			SchemaMapping schemaMapping, 
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkPool,
			UIDCacheManager uidCacheManager,
			Config config) throws SQLException {
		importer = new CityGMLImportManager(connection, 
				databaseAdapter,
				schemaMapping,
				cityGMLBuilder,
				xlinkPool,
				uidCacheManager, 
				config);

		Integer commitAfterProp = config.getProject().getDatabase().getUpdateBatching().getFeatureBatchValue();
		if (commitAfterProp != null && commitAfterProp > 0)
			commitAfter = commitAfterProp;

		bboxOptions = BoundingBoxOptions.defaults()				
				.useExistingEnvelopes(true)
				.assignResultToFeatures(true)
				.useReferencePointAsFallbackForImplicitGeometries(true);

		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
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
	public void run() {
		try {
			if (firstWork != null) {
				doWork(firstWork);
				firstWork = null;
			}

			while (shouldRun) {
				try {
					CityGML work = workQueue.take();
					doWork(work);
				} catch (InterruptedException ie) {
					// re-check state
				}
			}

			try {
				if (shouldWork) {
					importer.executeBatch();					
					if (!globalTransaction)
						connection.commit();

					updateImportContext();
				}
			} catch (CityGMLImportException | SQLException e) {
				if (!globalTransaction) {
					try {
						connection.rollback();
					} catch (SQLException sql) {
						//
					}
				}

				eventDispatcher.triggerEvent(new InterruptEvent("Aborting import due to errors.", LogLevel.WARN, e, eventChannel, this));
			} catch (IOException e) {
				eventDispatcher.triggerEvent(new InterruptEvent("Aborting import due to I/O errors.", LogLevel.WARN, e, eventChannel, this));
			}

		} finally {
			try {
				importer.close();
			} catch (CityGMLImportException | SQLException e) {
				// 
			}

			if (!globalTransaction) {
				try {
					connection.close();
				} catch (SQLException e) {
					//
				}
			}

			connection = null;
			eventDispatcher.removeEventHandler(this);
		}
	}

	private void doWork(CityGML work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			if (!shouldWork)
				return;

			long id = 0;

			if (work instanceof Appearance) {
				// global appearances
				Appearance appearance = (Appearance)work;
				appearance.setLocalProperty(Internal.IS_TOP_LEVEL, true);
				id = importer.importGlobalAppearance(appearance);
			} 

			else if (work instanceof AbstractCityObject) {
				AbstractCityObject cityObject = (AbstractCityObject)work;

				// compute bounding box for cityobject
				cityObject.calcBoundedBy(bboxOptions);

				// check filter
				if (!filter.getSelectionFilter().isSatisfiedBy(cityObject))
					return;			

				cityObject.setLocalProperty(Internal.IS_TOP_LEVEL, true);
				id = importer.importObject(cityObject);

				// import local appearances
				if (id != 0)
					importer.importLocalAppearance();
				else
					importer.logOrThrowErrorMessage(new StringBuilder("Failed to import object ")
							.append(importer.getObjectSignature(cityObject)).append(".").toString());
			}

			else {
				String msg = new StringBuilder()
						.append(work instanceof AbstractGML ? importer.getObjectSignature((AbstractGML)work) : work.getCityGMLClass())
						.append(": Unsupported top-level object type. Skipping import.").toString();

				if (!importer.isFailOnError())
					Logger.getInstance().error(msg);
				else
					throw new CityGMLImportException(msg);
			}

			if (id != 0)
				updateCounter++;

			if (updateCounter == commitAfter) {
				importer.executeBatch();
				if (!globalTransaction)
					connection.commit();

				updateImportContext();
			}

		} catch (CityGMLImportException | SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException sql) {
				//
			}

			eventDispatcher.triggerSyncEvent(new InterruptEvent("Aborting import due to errors.", LogLevel.WARN, e, eventChannel, this));
		} catch (Throwable e) {
			// this is to catch general exceptions that may occur during the import
			eventDispatcher.triggerSyncEvent(new InterruptEvent("Aborting due to an unexpected " + e.getClass().getName() + " error.", LogLevel.ERROR, e, eventChannel, this));
		} finally {
			runLock.unlock();
		}
	}

	private void updateImportContext() throws IOException {
		eventDispatcher.triggerEvent(new ObjectCounterEvent(importer.getAndResetObjectCounter(), this));
		eventDispatcher.triggerEvent(new GeometryCounterEvent(importer.getAndResetGeometryCounter(), this));
		eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, updateCounter, this));
		updateCounter = 0;

		// log imported top-level features
		if (importLogger != null) {
			for (ImportLogEntry entry : importer.getAndResetImportLogEntries())
				importLogger.write(entry);
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel)
			shouldWork = false;
	}

}
