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
package org.citydb.core.operation.deleter.controller;

import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.project.common.IdList;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.deleter.DeleteMode;
import org.citydb.config.project.global.CacheMode;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.adapter.IndexStatusInfo;
import org.citydb.core.database.connection.DatabaseConnectionPool;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.operation.common.cache.CacheTableManager;
import org.citydb.core.operation.common.cache.model.CacheTableModel;
import org.citydb.core.operation.common.csv.IdListException;
import org.citydb.core.operation.common.csv.IdListImporter;
import org.citydb.core.operation.common.csv.IdListParser;
import org.citydb.core.operation.deleter.DeleteException;
import org.citydb.core.operation.deleter.DeleteException.ErrorCode;
import org.citydb.core.operation.deleter.concurrent.DBDeleteWorkerFactory;
import org.citydb.core.operation.deleter.database.BundledConnection;
import org.citydb.core.operation.deleter.database.DBSplittingResult;
import org.citydb.core.operation.deleter.database.DeleteManager;
import org.citydb.core.operation.deleter.util.DeleteLogger;
import org.citydb.core.operation.deleter.util.InternalConfig;
import org.citydb.core.query.Query;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.builder.config.ConfigQueryBuilder;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.core.util.CoreConstants;
import org.citydb.core.util.Util;
import org.citydb.util.concurrent.SingleWorkerPool;
import org.citydb.util.concurrent.WorkerPool;
import org.citydb.util.event.Event;
import org.citydb.util.event.EventDispatcher;
import org.citydb.util.event.EventHandler;
import org.citydb.util.event.global.*;
import org.citydb.util.log.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class Deleter implements EventHandler {
	private final Logger log = Logger.getInstance();
	private final SchemaMapping schemaMapping;
	private final EventDispatcher eventDispatcher;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final Config config;

	private final Object eventChannel = new Object();
	private final AtomicBoolean isInterrupted = new AtomicBoolean(false);
	private final Map<Integer, Long> objectCounter = new HashMap<>();

	private DeleteManager deleteManager;
	private WorkerPool<DBSplittingResult> dbWorkerPool;
	private BundledConnection bundledConnection;
	private GlobalAppearanceCleaner globalAppearanceCleaner;
	private volatile boolean shouldRun = true;
	private DeleteException exception;
	private DeleteLogger deleteLogger;

	public Deleter() {
		config = ObjectRegistry.getInstance().getConfig();
		schemaMapping = ObjectRegistry.getInstance().getSchemaMapping();
		eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
	}

	public boolean doDelete(boolean preview) throws DeleteException {
		eventDispatcher.addEventHandler(EventType.OBJECT_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		try {
			return process(preview);
		} catch (DeleteException e) {
			throw e;
		} catch (Throwable e) {
			throw new DeleteException("An unexpected error occurred.", e);
		} finally {
			objectCounter.clear();
			eventDispatcher.removeEventHandler(this);

			if (deleteLogger != null) {
				try {
					deleteLogger.close(shouldRun);
				} catch (IOException e) {
					log.error("Failed to close the feature delete log. It is most likely corrupt.", e);
				}
			}
		}
	}

	private boolean process(boolean preview) throws DeleteException {
		long start = System.currentTimeMillis();
		DeleteMode mode = config.getDeleteConfig().getMode();

		// log workspace
		if (databaseAdapter.hasVersioningSupport() && databaseAdapter.getConnectionDetails().isSetWorkspace()) {
			Workspace workspace = databaseAdapter.getConnectionDetails().getWorkspace();
			if (!databaseAdapter.getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getName())) {
				log.info((mode == DeleteMode.TERMINATE ? "Terminating" : "Deleting") +
						" from workspace " + databaseAdapter.getConnectionDetails().getWorkspace() + ".");
			}
		}

		// create delete logger
		deleteLogger = null;
		if (!preview && config.getDeleteConfig().getDeleteLog().isSetLogDeletedFeatures()) {
			try {
				Path logFile = config.getDeleteConfig().getDeleteLog().isSetLogFile() ?
						Paths.get(config.getDeleteConfig().getDeleteLog().getLogFile()) :
						CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.DELETE_LOG_DIR);
				deleteLogger = new DeleteLogger(logFile,
						config.getDeleteConfig().getMode(),
						config.getDatabaseConfig().getActiveConnection());
				log.info("Log file of deleted top-level features: " + deleteLogger.getLogFilePath().toString());
			} catch (IOException e) {
				throw new DeleteException("Failed to create log file for deleted top-level features.", e);
			}
		}

		// build query from configuration
		Query query;
		try {
			ConfigQueryBuilder queryBuilder = new ConfigQueryBuilder(schemaMapping, databaseAdapter);
			query = config.getDeleteConfig().isUseSimpleQuery() ?
					queryBuilder.buildQuery(config.getDeleteConfig().getSimpleQuery(), config.getNamespaceFilter()) :
					queryBuilder.buildQuery(config.getDeleteConfig().getQuery(), config.getNamespaceFilter());
		} catch (QueryBuildException e) {
			throw new DeleteException("Failed to build the delete query expression.", e);
		}

		// check and log index status
		try {
			if (query.isSetSelection()
					&& query.getSelection().containsSpatialOperators()
					&& !databaseAdapter.getUtil().isIndexEnabled("CITYOBJECT", "ENVELOPE")) {
				throw new DeleteException(ErrorCode.SPATIAL_INDEXES_NOT_ACTIVATED, "Spatial indexes are not activated.");
			}

			for (IndexStatusInfo.IndexType type : IndexStatusInfo.IndexType.values()) {
				databaseAdapter.getUtil().getIndexStatus(type).printStatusToConsole();
			}
		} catch (SQLException e) {
			throw new DeleteException("Database error while querying index status.", e);
		}

		bundledConnection = new BundledConnection().withSingleConnection(true);
		CacheTableManager cacheTableManager = null;
		CacheTable cacheTable = null;

		try {
			if (config.getDeleteConfig().isUseDeleteList()
					&& config.getDeleteConfig().isSetDeleteList()
					&& config.getDeleteConfig().getDeleteList().hasFiles()) {
				log.info("Loading delete list into temporary database table...");

				// create instance of the cache table manager
				try {
					cacheTableManager = new CacheTableManager(config.getGlobalConfig().getCache());
					cacheTable = cacheTableManager.createCacheTable(CacheTableModel.ID_LIST, CacheMode.DATABASE);
				} catch (SQLException | IOException e) {
					throw new DeleteException("Failed to initialize temporary delete list cache.", e);
				}

				IdList deleteList = config.getDeleteConfig().getDeleteList();
				int maxBatchSize = config.getDatabaseConfig().getImportBatching().getTempBatchSize();
				IdListImporter importer = new IdListImporter(cacheTable, maxBatchSize);

				for (String file : deleteList.getFiles()) {
					log.debug("Loading CSV file '" + file + "' into database.");
					try (IdListParser parser = IdListParser.of(file, deleteList)) {
						try {
							importer.doImport(parser);
						} catch (IdListException e) {
							throw new DeleteException("Failed to parse delete list.", e);
						} catch (SQLException e) {
							throw new DeleteException("Failed to load delete list into temporary database table.", e);
						}
					} catch (IdListException e) {
						throw new DeleteException("Failed to create delete list parser.", e);
					}
				}
			}

			if (preview) {
				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("delete.dialog.title.preview")));
				log.info("Running " + mode.value() + " in preview mode. Affected city objects will not be " +
						(mode == DeleteMode.TERMINATE ? "terminated." : "deleted."));
			} else {
				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString(mode == DeleteMode.TERMINATE ?
						"delete.dialog.title.terminate" :
						"delete.dialog.title.delete")));
				log.info((mode == DeleteMode.TERMINATE ? "Terminating" : "Deleting") + " city objects from database.");
			}

			// create internal config and set metadata
			InternalConfig internalConfig = new InternalConfig();
			internalConfig.setMetadata(config.getDeleteConfig().getContinuation());

			// multi-threading may cause a database deadlock when deleting a CityObjectGroup within
			// one thread and its groupMembers within another threads at the same time.
			// Hence, we use a single thread pool to avoid this issue.
			dbWorkerPool = new SingleWorkerPool<>(
					"db_deleter_pool",
					new DBDeleteWorkerFactory(bundledConnection, deleteLogger, internalConfig, config, eventDispatcher),
					300,
					false);

			// set channel for events triggered by workers
			dbWorkerPool.setEventSource(eventChannel);

			dbWorkerPool.prestartCoreWorkers();
			if (dbWorkerPool.getPoolSize() == 0) {
				throw new DeleteException("Failed to start database delete worker pool. Check the database connection pool settings.");
			}

			// get delete manager and execute delete operation
			try {
				deleteManager = new DeleteManager(bundledConnection,
						schemaMapping,
						dbWorkerPool,
						query,
						cacheTable,
						deleteLogger,
						internalConfig,
						config,
						eventDispatcher,
						eventChannel,
						preview);
				deleteManager.deleteObjects();
			} catch (SQLException | IOException | QueryBuildException e) {
				throw new DeleteException("Failed to execute the " + mode.value() + " operation.", e);
			}

			try {
				dbWorkerPool.shutdownAndWait();
			} catch (InterruptedException e) {
				throw new DeleteException("Failed to shutdown worker pools.", e);
			}

			if (shouldRun
					&& config.getDeleteConfig().isCleanupGlobalAppearances()
					&& !preview) {
				try {
					if (databaseAdapter.getUtil().containsGlobalAppearances()) {
						eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("delete.dialog.title.cleanupGlobalAppearances")));
						eventDispatcher.triggerEvent(new StatusDialogProgressBar(true));
						log.info("Cleaning up unreferenced global appearances.");

						globalAppearanceCleaner = new GlobalAppearanceCleaner(bundledConnection, databaseAdapter);
						int deleted = globalAppearanceCleaner.doCleanup();
						log.info("Deleted global appearances: " + deleted);
					} else {
						log.debug("The database does not contain global appearances.");
					}
				} catch (SQLException e) {
					throw new DeleteException("Failed to query global appearances from database.", e);
				}
			}
		} finally {
			if (dbWorkerPool != null) {
				dbWorkerPool.shutdownNow();
			}

			try {
				bundledConnection.close();
			} catch (SQLException e) {
				//
			}

			try {
				eventDispatcher.flushEvents();
			} catch (InterruptedException e) {
				//
			}

			if (cacheTableManager != null) {
				try {
					log.info("Cleaning temporary cache.");
					cacheTableManager.close();
				} catch (SQLException e) {
					setException("Failed to clean the temporary cache.", e);
					shouldRun = false;
				}
			}
		}

		if (shouldRun) {
			// show deleted features
			if (!objectCounter.isEmpty()) {
				if (preview) {
					log.info("The " + mode.value() + " operation would affect the following city objects:");
				} else {
					log.info((mode == DeleteMode.TERMINATE ? "Terminated" : "Deleted") + " city objects:");
				}

				Map<String, Long> typeNames = Util.mapObjectCounter(objectCounter, schemaMapping);
				typeNames.keySet().forEach(object -> log.info(object + ": " + typeNames.get(object)));
			}

			log.info("Total processing time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");
		} else if (exception != null) {
			throw exception;
		}

		return shouldRun;
	}

	private void setException(String message, Throwable cause) {
		if (exception == null) {
			exception = new DeleteException(message, cause);
		}
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.OBJECT_COUNTER && e.getChannel() == eventChannel) {
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

				if (event.canConsumeLogMessage()) {
					log.log(event.getLogLevelType(), event.getLogMessage());
				}

				if (event.getCause() != null) {
					setException("Aborting delete due to errors.", event.getCause());
				}

				if (deleteManager != null) {
					deleteManager.shutdown();
				}

				if (dbWorkerPool != null) {
					dbWorkerPool.drainWorkQueue();
				}

				if (globalAppearanceCleaner != null) {
					globalAppearanceCleaner.interrupt();
				}
			}
		}
	}
}
