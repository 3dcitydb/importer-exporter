/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.citygml.importer.controller;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;

import org.citydb.api.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.event.EventHandler;
import org.citydb.api.log.LogLevel;
import org.citydb.config.Config;
import org.citydb.config.internal.Internal;
import org.citydb.config.language.Language;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.general.AffineTransformation;
import org.citydb.config.project.importer.ImportGmlId;
import org.citydb.config.project.importer.ImportResources;
import org.citydb.config.project.importer.Index;
import org.citydb.config.project.importer.XMLValidation;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.database.IndexStatusInfo;
import org.citydb.database.IndexStatusInfo.IndexInfoObject;
import org.citydb.database.IndexStatusInfo.IndexStatus;
import org.citydb.database.IndexStatusInfo.IndexType;
import org.citydb.database.adapter.AbstractUtilAdapter;
import org.citydb.io.DirectoryScanner;
import org.citydb.io.DirectoryScanner.CityGMLFilenameFilter;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.common.database.uid.UIDCacheType;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.importer.concurrent.DBImportWorkerFactory;
import org.citydb.modules.citygml.importer.concurrent.DBImportXlinkResolverWorkerFactory;
import org.citydb.modules.citygml.importer.concurrent.DBImportXlinkWorkerFactory;
import org.citydb.modules.citygml.importer.concurrent.FeatureReaderWorkerFactory;
import org.citydb.modules.citygml.importer.database.uid.FeatureGmlIdCache;
import org.citydb.modules.citygml.importer.database.uid.GeometryGmlIdCache;
import org.citydb.modules.citygml.importer.database.uid.TextureImageCache;
import org.citydb.modules.citygml.importer.database.xlink.resolver.DBXlinkSplitter;
import org.citydb.modules.citygml.importer.util.AffineTransformer;
import org.citydb.modules.citygml.importer.util.ImportLogger;
import org.citydb.modules.common.event.CounterEvent;
import org.citydb.modules.common.event.CounterType;
import org.citydb.modules.common.event.EventType;
import org.citydb.modules.common.event.FeatureCounterEvent;
import org.citydb.modules.common.event.GeometryCounterEvent;
import org.citydb.modules.common.event.InterruptEvent;
import org.citydb.modules.common.event.InterruptReason;
import org.citydb.modules.common.event.StatusDialogMessage;
import org.citydb.modules.common.event.StatusDialogProgressBar;
import org.citydb.modules.common.event.StatusDialogTitle;
import org.citydb.modules.common.filter.FilterMode;
import org.citydb.modules.common.filter.ImportFilter;
import org.citydb.modules.common.filter.statistic.FeatureCounterFilter;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.CityModel;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.CityGMLReader;
import org.citygml4j.xml.io.reader.FeatureReadMode;
import org.citygml4j.xml.io.reader.XMLChunk;


public class Importer implements EventHandler {
	private final Logger LOG = Logger.getInstance();

	private final JAXBBuilder jaxbBuilder;
	private final DatabaseConnectionPool dbPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);
	private InterruptReason interruptReason;
	private EnumMap<CityGMLClass, Long> featureCounterMap;
	private EnumMap<GMLClass, Long> geometryCounterMap;
	private DirectoryScanner directoryScanner;
	private long xmlValidationErrorCounter;

	public Importer(JAXBBuilder jaxbBuilder, 
			DatabaseConnectionPool dbPool, 
			Config config, 
			EventDispatcher eventDispatcher) {
		this.jaxbBuilder = jaxbBuilder;
		this.dbPool = dbPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		featureCounterMap = new EnumMap<CityGMLClass, Long>(CityGMLClass.class);
		geometryCounterMap = new EnumMap<GMLClass, Long>(GMLClass.class);
	}

	public void cleanup() {
		eventDispatcher.removeEventHandler(this);
	}

	public boolean doProcess() throws CityGMLImportException {
		// adding listeners
		eventDispatcher.addEventHandler(EventType.FEATURE_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.GEOMETRY_COUNTER, this);
		eventDispatcher.addEventHandler(EventType.INTERRUPT, this);

		// get config shortcuts
		final org.citydb.config.project.importer.Importer importerConfig = config.getProject().getImporter();
		Database databaseConfig = config.getProject().getDatabase();
		Internal internalConfig = config.getInternal();		
		ImportResources resourcesConfig = importerConfig.getResources();
		Index indexConfig = importerConfig.getIndexes();
		ImportGmlId gmlIdConfig = importerConfig.getGmlId();

		// worker pool settings 
		int minThreads = resourcesConfig.getThreadPool().getDefaultPool().getMinThreads();
		int maxThreads = resourcesConfig.getThreadPool().getDefaultPool().getMaxThreads();
		int queueSize = maxThreads * 2;

		// gml:id lookup cache update
		int lookupCacheBatchSize = databaseConfig.getUpdateBatching().getGmlIdCacheBatchValue();

		// check database workspace
		Workspace workspace = databaseConfig.getWorkspaces().getImportWorkspace();
		if (shouldRun && dbPool.getActiveDatabaseAdapter().hasVersioningSupport() && 
				!dbPool.getActiveDatabaseAdapter().getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getName()) &&
				!dbPool.getActiveDatabaseAdapter().getWorkspaceManager().existsWorkspace(workspace, true))
			return false;

		// deactivate database indexes
		if (shouldRun && (indexConfig.isSpatialIndexModeDeactivate() || indexConfig.isSpatialIndexModeDeactivateActivate() 
				|| indexConfig.isNormalIndexModeDeactivate() || indexConfig.isNormalIndexModeDeactivateActivate())) {
			try {
				if (shouldRun && (indexConfig.isSpatialIndexModeDeactivate() || indexConfig.isSpatialIndexModeDeactivateActivate()))
					manageIndexes(false, true);
				else
					dbPool.getActiveDatabaseAdapter().getUtil().getIndexStatus(IndexType.SPATIAL).printStatusToConsole();

				if (shouldRun && (indexConfig.isNormalIndexModeDeactivate() || indexConfig.isNormalIndexModeDeactivateActivate()))
					manageIndexes(false, false);
				else
					dbPool.getActiveDatabaseAdapter().getUtil().getIndexStatus(IndexType.NORMAL).printStatusToConsole();

			} catch (SQLException e) {
				throw new CityGMLImportException("Database error while deactivating indexes.", e);
			}
		} else {
			try {
				for (IndexType type : IndexType.values())
					dbPool.getActiveDatabaseAdapter().getUtil().getIndexStatus(type).printStatusToConsole();
			} catch (SQLException e) {
				throw new CityGMLImportException("Database error while querying index status.", e);
			}
		}

		// build list of import files
		LOG.info("Creating list of CityGML files to be imported...");	
		directoryScanner = new DirectoryScanner(true);
		directoryScanner.addFilenameFilter(new CityGMLFilenameFilter());
		List<File> importFiles = directoryScanner.getFiles(internalConfig.getImportFiles());

		if (importFiles.size() == 0) {
			LOG.warn("Failed to find CityGML files at the specified locations.");
			return false;
		}

		int fileCounter = 0;
		int remainingFiles = importFiles.size();
		LOG.info("List of import files successfully created.");
		LOG.info(remainingFiles + " file(s) will be imported.");

		// prepare CityGML input factory
		CityGMLInputFactory in = null;
		try {
			in = jaxbBuilder.createCityGMLInputFactory();
			in.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_COLLECTION_MEMBER);
			in.setProperty(CityGMLInputFactory.FAIL_ON_MISSING_ADE_SCHEMA, false);
			in.setProperty(CityGMLInputFactory.PARSE_SCHEMA, false);
			in.setProperty(CityGMLInputFactory.SPLIT_AT_FEATURE_PROPERTY, new QName("generalizesTo"));
			in.setProperty(CityGMLInputFactory.EXCLUDE_FROM_SPLITTING, CityModel.class);
		} catch (CityGMLReadException e) {
			throw new CityGMLImportException("Failed to initialize CityGML parser. Aborting.", e);
		}

		// prepare XML validation 
		XMLValidation xmlValidation = importerConfig.getXMLValidation();
		if (xmlValidation.isSetUseXMLValidation()) {
			LOG.info("Using XML validation during database import.");

			in.setProperty(CityGMLInputFactory.USE_VALIDATION, true);
			in.setProperty(CityGMLInputFactory.PARSE_SCHEMA, true);

			ValidationErrorHandler validationHandler = new ValidationErrorHandler();
			validationHandler.allErrors = !xmlValidation.isSetReportOneErrorPerFeature();
			in.setValidationEventHandler(validationHandler);
		}

		// affine transformation
		AffineTransformation affineTransformation = importerConfig.getAffineTransformation();
		if (affineTransformation.isSetUseAffineTransformation()) {
			LOG.info("Applying affine coordinates transformation.");

			try {
				internalConfig.setAffineTransformer(new AffineTransformer(config));
			} catch (Exception e) {
				throw new CityGMLImportException("The provided homogeneous transformation matrix is singular.", e);
			}
		}

		// prepare counter filter
		FeatureCounterFilter counterFilter = new FeatureCounterFilter(config, FilterMode.IMPORT);
		Long counterFirstElement = counterFilter.getFilterState().get(0);
		Long counterLastElement = counterFilter.getFilterState().get(1);
		long elementCounter = 0;

		// prepare feature filter
		final ImportFilter importFilter = new ImportFilter(config);
		CityGMLInputFilter inputFilter = new CityGMLInputFilter() {
			public boolean accept(CityGMLClass type) {
				return type != CityGMLClass.APPEARANCE ? 
						!importFilter.getFeatureClassFilter().filter(type) : importerConfig.getAppearances().isSetImportAppearance();
			}
		};

		CacheTableManager cacheTableManager = null;
		UIDCacheManager uidCacheManager = null;
		WorkerPool<CityGML> dbWorkerPool = null;
		WorkerPool<XMLChunk> featureWorkerPool = null;
		WorkerPool<DBXlink> tmpXlinkPool = null;
		WorkerPool<DBXlink> xlinkResolverPool = null;
		DBXlinkSplitter tmpSplitter = null;
		ImportLogger importLogger = null;
		long start = System.currentTimeMillis();

		while (shouldRun && fileCounter < importFiles.size()) {
			try {
				// check whether we reached the counter limit
				if (counterLastElement != null && elementCounter > counterLastElement)
					break;

				File file = importFiles.get(fileCounter++);
				internalConfig.setImportPath(file.getParent());
				internalConfig.setCurrentImportFile(file);

				eventDispatcher.triggerEvent(new StatusDialogTitle(file.getName(), this));
				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.cityObj.msg"), this));
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(true, this));
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.FILE, --remainingFiles, this));
				LOG.info("Importing file: " + file.toString());	

				// set gml:id codespace
				if (gmlIdConfig.isSetRelativeCodeSpaceMode())
					internalConfig.setCurrentGmlIdCodespace(file.getName());
				else if (gmlIdConfig.isSetAbsoluteCodeSpaceMode())
					internalConfig.setCurrentGmlIdCodespace(file.toString());
				else if (gmlIdConfig.isSetUserCodeSpaceMode())
					internalConfig.setCurrentGmlIdCodespace(gmlIdConfig.getCodeSpace());
				else if (!gmlIdConfig.isSetUserCodeSpaceMode())
					internalConfig.setCurrentGmlIdCodespace(null);
				
				// create import logger
				if (importerConfig.getImportLog().isSetLogImportedFeatures()) {
					try {
						String logPath = importerConfig.getImportLog().isSetLogPath() ? importerConfig.getImportLog().getLogPath() : Internal.DEFAULT_IMPORT_LOG_PATH;
						importLogger = new ImportLogger(logPath, file, databaseConfig.getActiveConnection());
						LOG.info("Log file of imported top-level features: " + importLogger.getLogFilePath().toString());
					} catch (IOException e) {
						throw new CityGMLImportException("Failed to create log file for imported top-level features. Aborting.", e);
					}
				}

				// create instance of the cache table manager
				try {
					cacheTableManager = new CacheTableManager(dbPool, maxThreads, config);
				} catch (SQLException e) {
					throw new CityGMLImportException("SQL error while initializing cache manager.", e);
				} catch (IOException e) {
					throw new CityGMLImportException("I/O error while initializing cache manager.", e);
				}

				// create instance of gml:id lookup server manager...
				uidCacheManager = new UIDCacheManager();

				// ...and start servers
				try {
					uidCacheManager.initCache(
							UIDCacheType.GEOMETRY,
							new GeometryGmlIdCache(cacheTableManager, 
									resourcesConfig.getGmlIdCache().getGeometry().getPartitions(), 
									lookupCacheBatchSize),
									resourcesConfig.getGmlIdCache().getGeometry().getCacheSize(),
									resourcesConfig.getGmlIdCache().getGeometry().getPageFactor(),
									maxThreads);

					uidCacheManager.initCache(
							UIDCacheType.FEATURE,
							new FeatureGmlIdCache(cacheTableManager, 
									resourcesConfig.getGmlIdCache().getFeature().getPartitions(),
									lookupCacheBatchSize),
									resourcesConfig.getGmlIdCache().getFeature().getCacheSize(),
									resourcesConfig.getGmlIdCache().getFeature().getPageFactor(),
									maxThreads);

					if (config.getProject().getImporter().getAppearances().isSetImportAppearance() &&
							config.getProject().getImporter().getAppearances().isSetImportTextureFiles()) {
						uidCacheManager.initCache(
								UIDCacheType.TEX_IMAGE,
								new TextureImageCache(cacheTableManager, 
										resourcesConfig.getTexImageCache().getPartitions(),
										lookupCacheBatchSize),
										resourcesConfig.getTexImageCache().getCacheSize(),
										resourcesConfig.getTexImageCache().getPageFactor(),
										maxThreads);
					}
				} catch (SQLException e) {
					throw new CityGMLImportException("SQL error while initializing database import.", e);
				}

				// creating worker pools needed for data import
				// this pool is for registering xlinks
				tmpXlinkPool = new WorkerPool<DBXlink>(
						"xlink_importer_pool",
						minThreads,
						maxThreads,
						PoolSizeAdaptationStrategy.AGGRESSIVE,
						new DBImportXlinkWorkerFactory(dbPool, cacheTableManager, config, eventDispatcher),
						queueSize,
						false);

				// this pool basically works on the data import
				dbWorkerPool = new WorkerPool<CityGML>(
						"db_importer_pool",
						minThreads,
						maxThreads,
						PoolSizeAdaptationStrategy.AGGRESSIVE,
						new DBImportWorkerFactory(dbPool, 
								jaxbBuilder,
								tmpXlinkPool, 
								uidCacheManager, 
								importFilter,
								importLogger,
								config, 
								eventDispatcher),
								queueSize,
								false);

				// this worker pool unmarshals the input file and passes xml chunks to the dbworker pool
				featureWorkerPool = new WorkerPool<XMLChunk>(
						"citygml_parser_pool",
						minThreads,
						maxThreads,
						PoolSizeAdaptationStrategy.AGGRESSIVE,
						new FeatureReaderWorkerFactory(dbWorkerPool, config, eventDispatcher),
						queueSize,
						false);

				// prestart threads
				tmpXlinkPool.prestartCoreWorkers();
				dbWorkerPool.prestartCoreWorkers();
				featureWorkerPool.prestartCoreWorkers();

				// fail if we could not start a single import worker
				if (dbWorkerPool.getPoolSize() == 0) {
					LOG.error("Failed to start database import worker pool. Check the database connection pool settings.");
					return false;
				}

				// ok, preparation done. start parsing the input file
				CityGMLReader reader = null;
				try {
					reader = in.createFilteredCityGMLReader(in.createCityGMLReader(file), inputFilter);	

					while (shouldRun && reader.hasNext()) {
						XMLChunk chunk = reader.nextChunk();

						if (counterFilter.isActive()) {
							elementCounter++;

							if (counterFirstElement != null && elementCounter < counterFirstElement)
								continue;

							if (counterLastElement != null && elementCounter > counterLastElement)
								break;
						}

						featureWorkerPool.addWork(chunk);
					}					
				} catch (CityGMLReadException e) {
					throw new CityGMLImportException("Failed to parse CityGML file. Aborting.", e);
				}

				// we are done with parsing. so shutdown the workers.
				// the xlink pool is not shutdown because we need it afterwards
				try {
					featureWorkerPool.shutdownAndWait();
					reader.close();
					dbWorkerPool.shutdownAndWait();
					tmpXlinkPool.join();
				} catch (InterruptedException e) {
					throw new CityGMLImportException("Failed to shutdown worker pools.", e);
				} catch (CityGMLReadException e) {
					throw new CityGMLImportException("Failed to close CityGML reader.", e);
				}

				if (shouldRun) {
					// get an xlink resolver pool
					LOG.info("Resolving XLink references.");
					xlinkResolverPool = new WorkerPool<DBXlink>(
							"xlink_resolver_pool",
							minThreads,
							maxThreads,
							PoolSizeAdaptationStrategy.AGGRESSIVE,
							new DBImportXlinkResolverWorkerFactory(dbPool, 
									tmpXlinkPool, 
									uidCacheManager, 
									cacheTableManager, 
									importFilter,
									config, 
									eventDispatcher),
									queueSize,
									false);

					// prestart its workers
					xlinkResolverPool.prestartCoreWorkers();

					// resolve xlinks based on temp tables
					if (shouldRun) {
						tmpSplitter = new DBXlinkSplitter(cacheTableManager, 
								xlinkResolverPool, 
								tmpXlinkPool,
								Event.GLOBAL_CHANNEL,
								eventDispatcher);

						tmpSplitter.startQuery();
					}

					// shutdown worker pools
					try {
						xlinkResolverPool.shutdownAndWait();
					} catch (InterruptedException e) {
						throw new CityGMLImportException("Failed to shutdown worker pools.", e);
					}
				}

				// shutdown tmp xlink pool
				try {
					tmpXlinkPool.shutdownAndWait();
				} catch (InterruptedException e) {
					throw new CityGMLImportException("Failed to shutdown worker pools.", e);
				}

				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.finish.msg"), this));
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(true, this));

				// show XML validation errors
				if (xmlValidation.isSetUseXMLValidation() && xmlValidationErrorCounter > 0)
					LOG.warn(xmlValidationErrorCounter + " error(s) encountered while validating the document.");

				xmlValidationErrorCounter = 0;
			} finally {
				// clean up
				if (featureWorkerPool != null && !featureWorkerPool.isTerminated())
					featureWorkerPool.shutdownNow();

				if (dbWorkerPool != null && !dbWorkerPool.isTerminated())
					dbWorkerPool.shutdownNow();

				if (xlinkResolverPool != null && !xlinkResolverPool.isTerminated())
					xlinkResolverPool.shutdownNow();

				if (tmpXlinkPool != null && !tmpXlinkPool.isTerminated())
					tmpXlinkPool.shutdownNow();

				try {
					eventDispatcher.flushEvents();
				} catch (InterruptedException e) {
					//
				}

				if (uidCacheManager != null) {
					try {
						uidCacheManager.shutdownAll();
					} catch (SQLException e) {
						LOG.error("Failed to shutdown gml:id cache: " + e.getMessage());
						shouldRun = false;
					}
				}

				if (cacheTableManager != null) {
					try {
						LOG.info("Cleaning temporary cache.");
						cacheTableManager.dropAll();
						cacheTableManager = null;
					} catch (SQLException e) {
						LOG.error("SQL error while cleaning temporary cache: " + e.getMessage());
						shouldRun = false;
					}
				}

				if (importLogger != null) {
					if (interruptReason != InterruptReason.IMPORT_LOG_ERROR) {
						try {
							importLogger.close(shouldRun);
						} catch (IOException e) {
							LOG.error("Failed to finish logging of imported top-level features.");
							LOG.warn("The feature import log is most likely corrupt.");
						}
					} else
						LOG.warn("The feature import log is most likely corrupt.");
				}
			}
		} 

		// reactivate database indexes
		if (shouldRun) {
			if (indexConfig.isSpatialIndexModeDeactivateActivate() || indexConfig.isNormalIndexModeDeactivateActivate()) {
				try {
					if (indexConfig.isSpatialIndexModeDeactivateActivate())
						manageIndexes(true, true);

					if (indexConfig.isNormalIndexModeDeactivateActivate())
						manageIndexes(true, false);

				} catch (SQLException e) {
					LOG.error("Database error while activating indexes: " + e.getMessage());
					return false;
				}
			}
		}

		// show imported features
		if (!featureCounterMap.isEmpty()) {
			LOG.info("Imported CityGML features:");
			for (CityGMLClass type : featureCounterMap.keySet())
				LOG.info(type + ": " + featureCounterMap.get(type));
		}

		long geometryObjects = 0;
		for (long counter : geometryCounterMap.values())
			geometryObjects += counter;

		if (geometryObjects != 0)
			LOG.info("Processed geometry objects: " + geometryObjects);

		if (shouldRun)
			LOG.info("Total import time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");

		return shouldRun;
	}

	private void manageIndexes(boolean enable, boolean workOnSpatialIndexes) throws SQLException {
		AbstractUtilAdapter utilAdapter = dbPool.getActiveDatabaseAdapter().getUtil();
		LOG.info((enable ? "Activating " : "Deactivating ") + (workOnSpatialIndexes ? "spatial" : "normal") + " indexes...");

		IndexStatusInfo indexStatus = null;
		if (enable) {
			indexStatus = workOnSpatialIndexes ? utilAdapter.createSpatialIndexes() : utilAdapter.createNormalIndexes();
		} else {
			indexStatus = workOnSpatialIndexes ? utilAdapter.dropSpatialIndexes() : utilAdapter.dropNormalIndexes();	
		}

		if (indexStatus != null) {
			IndexStatus expectedStatus = enable ? IndexStatus.VALID : IndexStatus.DROPPED;
			for (IndexInfoObject indexObj : indexStatus.getIndexObjects()) {
				if (indexObj.getStatus() != expectedStatus) {
					LOG.error("FAILED: " + indexObj.toString());
					if (indexObj.hasErrorMessage())
						LOG.error("Error cause: " + indexObj.getErrorMessage());
				}
			}
		}
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.FEATURE_COUNTER) {
			HashMap<CityGMLClass, Long> counterMap = ((FeatureCounterEvent)e).getCounter();

			for (CityGMLClass type : counterMap.keySet()) {
				Long counter = featureCounterMap.get(type);
				Long update = counterMap.get(type);

				if (counter == null)
					featureCounterMap.put(type, update);
				else
					featureCounterMap.put(type, counter + update);
			}
		}

		else if (e.getEventType() == EventType.GEOMETRY_COUNTER) {
			HashMap<GMLClass, Long> counterMap = ((GeometryCounterEvent)e).getCounter();

			for (GMLClass type : counterMap.keySet()) {
				Long counter = geometryCounterMap.get(type);
				Long update = counterMap.get(type);

				if (counter == null)
					geometryCounterMap.put(type, update);
				else
					geometryCounterMap.put(type, counter + update);
			}
		}

		else if (e.getEventType() == EventType.INTERRUPT) {
			if (isInterrupted.compareAndSet(false, true)) {
				shouldRun = false;
				InterruptEvent interruptEvent = (InterruptEvent)e;
				interruptReason = interruptEvent.getInterruptReason();

				if (interruptEvent.getCause() != null) {
					Throwable cause = interruptEvent.getCause();

					if (cause instanceof SQLException) {
						Iterator<Throwable> iter = ((SQLException)cause).iterator();
						LOG.error("A SQL error occured: " + iter.next().getMessage().trim());
						while (iter.hasNext())
							LOG.error("Cause: " + iter.next().getMessage().trim());
					} else {
						LOG.error("An error occured: " + cause.getMessage().trim());
						while ((cause = cause.getCause()) != null)
							LOG.error("Cause: " + cause.getMessage().trim());
					}
				}

				String log = interruptEvent.getLogMessage();
				if (log != null)
					LOG.log(interruptEvent.getLogLevelType(), log);

				if (directoryScanner != null)
					directoryScanner.stopScanning();
			}
		}
	}

	private final class ValidationErrorHandler implements ValidationEventHandler {
		boolean allErrors = false;

		@Override
		public boolean handleEvent(ValidationEvent event) {
			StringBuilder msg = new StringBuilder();
			LogLevel type;

			switch (event.getSeverity()) {
			case ValidationEvent.FATAL_ERROR:
			case ValidationEvent.ERROR:
				msg.append("Invalid content");
				type = LogLevel.ERROR;
				break;
			case ValidationEvent.WARNING:
				msg.append("Warning");
				type = LogLevel.WARN;
				break;
			default:
				return allErrors;
			}

			msg.append(": ").append(event.getMessage());
			LOG.log(type, msg.toString());

			xmlValidationErrorCounter++;
			return allErrors;
		}
	}

}
