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
package org.citydb.citygml.importer.concurrent;

import org.citydb.citygml.common.cache.IdCacheManager;
import org.citydb.citygml.common.xlink.DBXlink;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.database.content.CityGMLImportManager;
import org.citydb.citygml.importer.filter.CityGMLFilter;
import org.citydb.citygml.importer.util.AffineTransformer;
import org.citydb.citygml.importer.util.ImportLogger;
import org.citydb.citygml.importer.util.ImportLogger.ImportLogEntry;
import org.citydb.citygml.importer.util.InternalConfig;
import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.project.global.LogLevel;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
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
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.model.gml.feature.AbstractFeature;
import org.citygml4j.util.bbox.BoundingBoxOptions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

public class DBImportWorker extends Worker<CityGML> implements EventHandler {
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;

	private final Connection connection;
	private final boolean isManagedTransaction;
	private final CityGMLFilter filter;
	private final ImportLogger importLogger;
	private final EventDispatcher eventDispatcher;

	private final BoundingBoxOptions bboxOptions;
	private final CityGMLImportManager importer;

	private int globalAppearanceCounter = 0;
	private int topLevelFeatureCounter = 0;
	private int commitAfter;

	public DBImportWorker(Connection connection,
			boolean isManagedTransaction,
			AbstractDatabaseAdapter databaseAdapter,
			SchemaMapping schemaMapping,
			CityGMLBuilder cityGMLBuilder,
			WorkerPool<DBXlink> xlinkPool,
			IdCacheManager idCacheManager,
			CityGMLFilter filter,
			AffineTransformer affineTransformer,
			ImportLogger importLogger,
			InternalConfig internalConfig,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.connection = connection;
		this.isManagedTransaction = isManagedTransaction;
		this.filter = filter;
		this.importLogger = importLogger;
		this.eventDispatcher = eventDispatcher;

		importer = new CityGMLImportManager(connection,
				databaseAdapter,
				schemaMapping,
				cityGMLBuilder,
				xlinkPool,
				idCacheManager,
				affineTransformer,
				internalConfig,
				config);

		commitAfter = config.getDatabaseConfig().getImportBatching().getFeatureBatchSize();
		if (commitAfter > databaseAdapter.getMaxBatchSize())
			commitAfter = databaseAdapter.getMaxBatchSize();

		bboxOptions = BoundingBoxOptions.defaults()				
				.useExistingEnvelopes(true)
				.assignResultToFeatures(true)
				.useReferencePointAsFallbackForImplicitGeometries(true);

		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
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
					CityGML work = workQueue.take();
					doWork(work);
				} catch (InterruptedException ie) {
					// re-check state
				}
			}

			try {
				if (shouldWork) {
					importer.executeBatch();					
					if (!isManagedTransaction)
						connection.commit();

					updateImportContext();
				}
			} catch (IOException e) {
				eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred during update of import log.", LogLevel.ERROR, e, eventChannel, this));
			} catch (Throwable e) {
				if (!isManagedTransaction) {
					try {
						connection.rollback();
					} catch (SQLException sql) {
						//
					}
				}

				eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred during import.", LogLevel.ERROR, e, eventChannel, this));
			}
		} finally {
			try {
				importer.close();
			} catch (CityGMLImportException | SQLException e) {
				// 
			}

			if (!isManagedTransaction) {
				try {
					connection.close();
				} catch (SQLException e) {
					//
				}
			}

			eventDispatcher.removeEventHandler(this);
		}
	}

	private void doWork(CityGML work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			if (!shouldWork)
				return;

			long id;

			if (work instanceof Appearance) {
				// global appearances
				Appearance appearance = (Appearance)work;
				id = importer.importGlobalAppearance(appearance);

				if (id != 0)
					globalAppearanceCounter++;
			} 

			else if (work instanceof AbstractFeature) {
				AbstractFeature feature = (AbstractFeature)work;

				// compute bounding box
				feature.calcBoundedBy(bboxOptions);

				// check import filter
				if (!filter.getSelectionFilter().isSatisfiedBy(feature))
					return;			

				id = importer.importObject(feature);
				if (id == 0)
					importer.logOrThrowErrorMessage("Failed to import object " + importer.getObjectSignature(feature) + ".");
				else
					topLevelFeatureCounter++;
			}

			else {
				String msg = (work instanceof AbstractGML ?
						importer.getObjectSignature((AbstractGML) work) : work.getCityGMLClass()) +
						": Unsupported top-level object type. Skipping import.";

				if (!importer.isFailOnError())
					Logger.getInstance().error(msg);
				else
					throw new CityGMLImportException(msg);
			}

			if (globalAppearanceCounter + topLevelFeatureCounter == commitAfter) {
				importer.executeBatch();
				if (!isManagedTransaction)
					connection.commit();

				updateImportContext();
			}

		} catch (IOException e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred during update of import log.", LogLevel.ERROR, e, eventChannel, this));
		} catch (Throwable e) {
			try {
				connection.rollback();
			} catch (SQLException sql) {
				//
			}

			eventDispatcher.triggerSyncEvent(new InterruptEvent("A fatal error occurred during import.", LogLevel.ERROR, e, eventChannel, this));
		} finally {
			runLock.unlock();
		}
	}

	private void updateImportContext() throws IOException {
		eventDispatcher.triggerEvent(new ObjectCounterEvent(importer.getAndResetObjectCounter(), this));
		eventDispatcher.triggerEvent(new GeometryCounterEvent(importer.getAndResetGeometryCounter(), this));
		eventDispatcher.triggerEvent(new CounterEvent(CounterType.GLOBAL_APPEARANCE, globalAppearanceCounter, this));
		eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, topLevelFeatureCounter, this));
		globalAppearanceCounter = 0;
		topLevelFeatureCounter = 0;

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
