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
package org.citydb.citygml.deleter.controller;

import org.citydb.citygml.deleter.DeleteException;
import org.citydb.citygml.deleter.concurrent.DBDeleteWorkerFactory;
import org.citydb.citygml.deleter.database.BundledConnection;
import org.citydb.citygml.deleter.database.DBSplitter;
import org.citydb.citygml.deleter.util.DeleteListParser;
import org.citydb.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.connection.DatabaseConnectionPool;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.event.global.InterruptEvent;
import org.citydb.event.global.ObjectCounterEvent;
import org.citydb.log.Logger;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.config.ConfigQueryBuilder;
import org.citydb.registry.ObjectRegistry;
import org.citydb.util.CoreConstants;
import org.citydb.util.Util;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class Deleter implements EventHandler {
	private final Logger log = Logger.getInstance();
	private final SchemaMapping schemaMapping;
	private final EventDispatcher eventDispatcher;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final Config config;
	private final AtomicBoolean isInterrupted = new AtomicBoolean(false);
	private final Map<Integer, Long> objectCounter;

	private DBSplitter dbSplitter;
	private WorkerPool<DBSplittingResult> dbWorkerPool;
	private BundledConnection bundledConnection;

	private volatile boolean shouldRun = true;
	private DeleteException exception;

	public Deleter() {
		config = ObjectRegistry.getInstance().getConfig();
		schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		objectCounter = new HashMap<>();
	}

	public boolean doDelete() throws DeleteException {
		try {
			// build query from configuration
			ConfigQueryBuilder queryBuilder = new ConfigQueryBuilder(schemaMapping, databaseAdapter);
			Query query = config.getDeleteConfig().isUseSimpleQuery() ?
					queryBuilder.buildQuery(config.getDeleteConfig().getSimpleQuery(), config.getNamespaceFilter()) :
					queryBuilder.buildQuery(config.getDeleteConfig().getQuery(), config.getNamespaceFilter());

			return doDelete(Collections.singletonList(query).iterator());
		} catch (QueryBuildException e) {
			throw new DeleteException("Failed to build the delete query expression.", e);
		}
	}

	public boolean doDelete(DeleteListParser parser) throws DeleteException {
		log.info("Using delete list from CSV file: " + parser.getFile());
		return doDelete(parser.queryIterator(schemaMapping, databaseAdapter));
	}

	private boolean doDelete(Iterator<Query> queries) throws DeleteException {
		eventDispatcher.addEventHandler(EventType.OBJECT_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		try {
			return process(queries);
		} finally {
			eventDispatcher.removeEventHandler(this);
		}
	}

	private boolean process(Iterator<Query> queries) throws DeleteException {
		long start = System.currentTimeMillis();

		// Multithreading may cause DB-Deadlock. It may occur when deleting a CityObjectGroup within
		// one thread, and the cityObjectMembers are being deleted within other threads at the same time.
		// Hence, we use single thread per-default to avoid this issue.
		int minThreads = 1;
		int maxThreads = 1;
		
		// checking workspace
		Workspace workspace = config.getDatabaseConfig().getWorkspaces().getDeleteWorkspace();
		if (shouldRun && databaseAdapter.hasVersioningSupport()
				&& !databaseAdapter.getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getName())) {
			try {
				log.info("Switching to database workspace " + workspace + ".");
				databaseAdapter.getWorkspaceManager().checkWorkspace(workspace);
			} catch (SQLException e) {
				throw new DeleteException("Failed to switch to database workspace.", e);
			}
		}
		
		bundledConnection = new BundledConnection();
		try {				
			dbWorkerPool = new WorkerPool<>(
					"db_deleter_pool",
					minThreads,
					maxThreads,
					PoolSizeAdaptationStrategy.AGGRESSIVE,
					new DBDeleteWorkerFactory(bundledConnection, config, eventDispatcher),
					300,
					false);

			dbWorkerPool.prestartCoreWorkers();
			if (dbWorkerPool.getPoolSize() == 0) {
				throw new DeleteException("Failed to start database delete worker pool. Check the database connection pool settings.");
			}

			log.info("Deleting city objects from database.");

			while (shouldRun && queries.hasNext()) {
				// get database splitter and start query
				try {
					dbSplitter = new DBSplitter(schemaMapping, dbWorkerPool, queries.next(), config, eventDispatcher);
					dbSplitter.setCalculateNumberMatched(CoreConstants.IS_GUI_MODE);
					dbSplitter.startQuery();
				} catch (SQLException | QueryBuildException e) {
					throw new DeleteException("Failed to query the database.", e);
				}
			}

			try {
				dbWorkerPool.shutdownAndWait();
			} catch (InterruptedException e) {
				throw new DeleteException("Failed to shutdown worker pools.", e);
			}
		} catch (DeleteException e) {
			throw e;
		} catch (Throwable e) {
			throw new DeleteException("An unexpected error occurred.", e);
		} finally {
			try {
				bundledConnection.close();
			} catch (SQLException e) {
				//
			}
			
			if (dbWorkerPool != null) {
				dbWorkerPool.shutdownNow();
			}

			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e) {
				//
			}
		}		
		
		// show deleted features
		if (!objectCounter.isEmpty()) {
			log.info("Deleted city objects:");
			Map<String, Long> typeNames = Util.mapObjectCounter(objectCounter, schemaMapping);					
			typeNames.keySet().stream().sorted().forEach(object -> log.info(object + ": " + typeNames.get(object)));			
		}

		if (shouldRun) {
			log.info("Total delete time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");
		} else if (exception != null) {
			throw exception;
		}

		return shouldRun;
	}
	
	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.OBJECT_COUNTER) {
			Map<Integer, Long> counter = ((ObjectCounterEvent) e).getCounter();
			for (Entry<Integer, Long> entry : counter.entrySet()) {
				Long tmp = objectCounter.get(entry.getKey());
				objectCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());
			}
		} else if (e.getEventType() == EventType.INTERRUPT) {
			if (isInterrupted.compareAndSet(false, true)) {
				shouldRun = false;
				bundledConnection.setShouldRollback(true);
				InterruptEvent event = (InterruptEvent) e;

				log.log(event.getLogLevelType(), event.getLogMessage());
				if (event.getCause() != null) {
					exception = new DeleteException("Aborting delete due to errors.", event.getCause());
				}

				if (dbSplitter != null) {
					dbSplitter.shutdown();
				}

				if (dbWorkerPool != null) {
					dbWorkerPool.drainWorkQueue();
				}
			}
		}
	}
}
