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
package org.citydb.citygml.importer.controller;

import org.apache.tika.exception.TikaException;
import org.citydb.citygml.common.database.cache.CacheTableManager;
import org.citydb.citygml.common.database.uid.UIDCacheManager;
import org.citydb.citygml.common.database.uid.UIDCacheType;
import org.citydb.citygml.common.database.xlink.DBXlink;
import org.citydb.citygml.importer.CityGMLImportException;
import org.citydb.citygml.importer.concurrent.DBImportWorkerFactory;
import org.citydb.citygml.importer.concurrent.DBImportXlinkResolverWorkerFactory;
import org.citydb.citygml.importer.concurrent.DBImportXlinkWorkerFactory;
import org.citydb.citygml.importer.concurrent.FeatureReaderWorkerFactory;
import org.citydb.citygml.importer.database.uid.FeatureGmlIdCache;
import org.citydb.citygml.importer.database.uid.GeometryGmlIdCache;
import org.citydb.citygml.importer.database.uid.TextureImageCache;
import org.citydb.citygml.importer.database.xlink.resolver.DBXlinkSplitter;
import org.citydb.citygml.importer.file.AbstractArchiveInputFile;
import org.citydb.citygml.importer.file.DirectoryScanner;
import org.citydb.citygml.importer.filter.CityGMLFilter;
import org.citydb.citygml.importer.filter.CityGMLFilterBuilder;
import org.citydb.citygml.importer.filter.type.FeatureTypeFilter;
import org.citydb.citygml.importer.util.AffineTransformer;
import org.citydb.citygml.importer.util.ImportLogger;
import org.citydb.concurrent.PoolSizeAdaptationStrategy;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.config.internal.FileType;
import org.citydb.config.internal.InputFile;
import org.citydb.config.internal.Internal;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.database.Workspace;
import org.citydb.config.project.global.LogLevel;
import org.citydb.config.project.importer.ImportGmlId;
import org.citydb.config.project.importer.ImportResources;
import org.citydb.config.project.importer.Index;
import org.citydb.config.project.importer.XMLValidation;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.AbstractUtilAdapter;
import org.citydb.database.adapter.IndexStatusInfo;
import org.citydb.database.adapter.IndexStatusInfo.IndexInfoObject;
import org.citydb.database.adapter.IndexStatusInfo.IndexStatus;
import org.citydb.database.adapter.IndexStatusInfo.IndexType;
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
import org.citydb.event.global.StatusDialogMessage;
import org.citydb.event.global.StatusDialogProgressBar;
import org.citydb.event.global.StatusDialogTitle;
import org.citydb.log.Logger;
import org.citydb.query.filter.FilterException;
import org.citydb.util.CoreConstants;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.gml.GMLClass;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;
import org.citygml4j.xml.io.reader.CityGMLReadException;
import org.citygml4j.xml.io.reader.CityGMLReader;
import org.citygml4j.xml.io.reader.FeatureReadMode;
import org.citygml4j.xml.io.reader.XMLChunk;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.namespace.QName;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

public class Importer implements EventHandler {
	private final Logger log = Logger.getInstance();

	private final CityGMLBuilder cityGMLBuilder;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final SchemaMapping schemaMapping;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);
	private HashMap<Integer, Long> objectCounter;
	private EnumMap<GMLClass, Long> geometryCounter;
	private DirectoryScanner directoryScanner;
	private long xmlValidationErrorCounter;

	public Importer(CityGMLBuilder cityGMLBuilder, 
			SchemaMapping schemaMapping,
			Config config, 
			EventDispatcher eventDispatcher) {
		this.cityGMLBuilder = cityGMLBuilder;
		this.schemaMapping = schemaMapping;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		databaseAdapter = DatabaseConnectionPool.getInstance().getActiveDatabaseAdapter();
		objectCounter = new HashMap<>();
		geometryCounter = new EnumMap<>(GMLClass.class);
	}

	public void cleanup() {
		eventDispatcher.removeEventHandler(this);
	}

	public boolean doProcess() throws CityGMLImportException {
		// adding listeners
		eventDispatcher.addEventHandler(EventType.OBJECT_COUNTER, this);
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
		if (shouldRun && databaseAdapter.hasVersioningSupport() && 
				!databaseAdapter.getWorkspaceManager().equalsDefaultWorkspaceName(workspace.getName()) &&
				!databaseAdapter.getWorkspaceManager().existsWorkspace(workspace, true))
			return false;

		// deactivate database indexes
		if (shouldRun && (indexConfig.isSpatialIndexModeDeactivate() || indexConfig.isSpatialIndexModeDeactivateActivate() 
				|| indexConfig.isNormalIndexModeDeactivate() || indexConfig.isNormalIndexModeDeactivateActivate())) {
			try {
				if (shouldRun && (indexConfig.isSpatialIndexModeDeactivate() || indexConfig.isSpatialIndexModeDeactivateActivate()))
					manageIndexes(false, true);
				else
					databaseAdapter.getUtil().getIndexStatus(IndexType.SPATIAL).printStatusToConsole();

				if (shouldRun && (indexConfig.isNormalIndexModeDeactivate() || indexConfig.isNormalIndexModeDeactivateActivate()))
					manageIndexes(false, false);
				else
					databaseAdapter.getUtil().getIndexStatus(IndexType.NORMAL).printStatusToConsole();

			} catch (SQLException e) {
				throw new CityGMLImportException("Database error while deactivating indexes.", e);
			}
		} else {
			try {
				for (IndexType type : IndexType.values())
					databaseAdapter.getUtil().getIndexStatus(type).printStatusToConsole();
			} catch (SQLException e) {
				throw new CityGMLImportException("Database error while querying index status.", e);
			}
		}

		// build list of import files
		List<InputFile> importFiles;
		try {
			log.info("Creating list of CityGML files to be imported...");
			directoryScanner = new DirectoryScanner(true);
			importFiles = directoryScanner.listFiles(internalConfig.getImportFiles());
			if (importFiles.isEmpty()) {
				log.warn("Failed to find CityGML files at the specified locations.");
				return false;
			}
		} catch (TikaException | IOException e) {
			throw new CityGMLImportException("Fatal error while searching for CityGML files.", e);
		}

		if (!shouldRun)
			return false;

		int fileCounter = 0;
		int remainingFiles = importFiles.size();
		log.info("List of import files successfully created.");
		log.info(remainingFiles + " file(s) will be imported.");

		// prepare CityGML input factory
		CityGMLInputFactory in = null;
		try {
			in = cityGMLBuilder.createCityGMLInputFactory();
			in.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_COLLECTION_MEMBER);
			in.setProperty(CityGMLInputFactory.FAIL_ON_MISSING_ADE_SCHEMA, false);
			in.setProperty(CityGMLInputFactory.PARSE_SCHEMA, false);
			in.setProperty(CityGMLInputFactory.SPLIT_AT_FEATURE_PROPERTY, new QName("generalizesTo"));
			in.setProperty(CityGMLInputFactory.EXCLUDE_FROM_SPLITTING, new QName("CityModel"));
		} catch (CityGMLBuilderException e) {
			throw new CityGMLImportException("Failed to initialize CityGML parser. Aborting.", e);
		}

		// prepare XML validation 
		XMLValidation xmlValidation = importerConfig.getXMLValidation();
		if (xmlValidation.isSetUseXMLValidation()) {
			log.info("Using XML validation during database import.");

			in.setProperty(CityGMLInputFactory.USE_VALIDATION, true);
			in.setProperty(CityGMLInputFactory.PARSE_SCHEMA, true);

			ValidationErrorHandler validationHandler = new ValidationErrorHandler();
			validationHandler.allErrors = !xmlValidation.isSetReportOneErrorPerFeature();
			in.setValidationEventHandler(validationHandler);
		}

		// build XSLT transformer chain
		if (importerConfig.getXSLTransformation().isEnabled()
				&& importerConfig.getXSLTransformation().isSetStylesheets()) {
			try {
				log.info("Applying XSL transformations on input data.");

				List<String> stylesheets = config.getProject().getImporter().getXSLTransformation().getStylesheets();
				SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
				Templates[] templates = new Templates[stylesheets.size()];

				for (int i = 0; i < stylesheets.size(); i++) {
					Templates template = factory.newTemplates(new StreamSource(new File(stylesheets.get(i))));
					templates[i] = template;
				}

				in.setTransformationTemplates(templates);
			} catch (CityGMLWriteException | TransformerConfigurationException e) {
				throw new CityGMLImportException("Failed to configure the XSL transformation.", e);
			}
		}

		// affine transformation
		AffineTransformer affineTransformer = null;
		if (importerConfig.getAffineTransformation().isEnabled()) {
			try {
				log.info("Applying affine coordinates transformation.");
				affineTransformer = new AffineTransformer(config);
			} catch (Exception e) {
				throw new CityGMLImportException("Failed to create affine transformer.", e);
			}
		}

		// build CityGML filter
		CityGMLFilter filter;
		try {
			CityGMLFilterBuilder builder = new CityGMLFilterBuilder(schemaMapping, databaseAdapter);
			filter = builder.buildCityGMLFilter(config.getProject().getImporter().getFilter());
		} catch (FilterException e) {
			throw new CityGMLImportException("Failed to build the import filter.", e);
		}

		// prepare feature filter
		final FeatureTypeFilter typeFilter = filter.getFeatureTypeFilter();
		CityGMLInputFilter inputFilter = name -> {
			Module module = Modules.getModule(name.getNamespaceURI());
			if (module != null && module.getType() == CityGMLModuleType.APPEARANCE && name.getLocalPart().equals("Appearance"))
				return importerConfig.getAppearances().isSetImportAppearance();
			else
				return typeFilter.isSatisfiedBy(name, true);
		};

		CacheTableManager cacheTableManager = null;
		UIDCacheManager uidCacheManager = null;
		WorkerPool<CityGML> dbWorkerPool = null;
		WorkerPool<XMLChunk> featureWorkerPool = null;
		WorkerPool<DBXlink> tmpXlinkPool = null;
		WorkerPool<DBXlink> xlinkResolverPool = null;
		DBXlinkSplitter tmpSplitter = null;
		ImportLogger importLogger = null;

		long elementCounter = 0;
		long start = System.currentTimeMillis();

		while (shouldRun && fileCounter < importFiles.size()) {
			// check whether we reached the counter limit
			if (filter.isSetCounterFilter() && elementCounter > filter.getCounterFilter().getUpperLimit())
				break;

			try (InputFile file = importFiles.get(fileCounter++)) {
				internalConfig.setCurrentImportFile(file);
				Path contentFile = file.getType() != FileType.ARCHIVE ?
						file.getFile() : Paths.get(file.getFile().toString(), ((AbstractArchiveInputFile) file).getContentFile());

				eventDispatcher.triggerEvent(new StatusDialogTitle(contentFile.getFileName().toString(), this));
				eventDispatcher.triggerEvent(new StatusDialogMessage(Language.I18N.getString("import.dialog.cityObj.msg"), this));
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(true, this));
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.FILE, --remainingFiles, this));
				log.info("Importing file: " + contentFile.toString());

				// set gml:id codespace starting from version 3.1
				if (databaseAdapter.getConnectionMetaData().getCityDBVersion().compareTo(3, 1, 0) >= 0) {
					if (gmlIdConfig.isSetNoneCodeSpaceMode())
						internalConfig.setCurrentGmlIdCodespace(null);
					else if (gmlIdConfig.isSetRelativeCodeSpaceMode())
						internalConfig.setCurrentGmlIdCodespace(file.getFile().getFileName().toString());
					else if (gmlIdConfig.isSetAbsoluteCodeSpaceMode())
						internalConfig.setCurrentGmlIdCodespace(file.getFile().toString());
					else if (gmlIdConfig.isSetUserCodeSpaceMode()) {
						String codespace = gmlIdConfig.getCodeSpace();
						if (codespace != null && codespace.length() > 0)
							internalConfig.setCurrentGmlIdCodespace(codespace);
					}
				} else
					internalConfig.setCurrentGmlIdCodespace(null);

				// create import logger
				if (importerConfig.getImportLog().isSetLogImportedFeatures()) {
					try {
						String logPath = importerConfig.getImportLog().isSetLogPath() ? importerConfig.getImportLog().getLogPath()
								: CoreConstants.IMPEXP_DATA_DIR.resolve(CoreConstants.IMPORT_LOG_DIR).toString();
						importLogger = new ImportLogger(logPath, contentFile, databaseConfig.getActiveConnection());
						log.info("Log file of imported top-level features: " + importLogger.getLogFilePath().toString());
					} catch (IOException e) {
						throw new CityGMLImportException("Failed to create log file for imported top-level features. Aborting.", e);
					}
				}

				// create instance of the cache table manager
				try {
					cacheTableManager = new CacheTableManager(maxThreads, config);
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
							UIDCacheType.OBJECT,
							new FeatureGmlIdCache(cacheTableManager, 
									resourcesConfig.getGmlIdCache().getFeature().getPartitions(),
									lookupCacheBatchSize),
							resourcesConfig.getGmlIdCache().getFeature().getCacheSize(),
							resourcesConfig.getGmlIdCache().getFeature().getPageFactor(),
							maxThreads);

					if (config.getProject().getImporter().getAppearances().isSetImportAppearance() &&
							config.getProject().getImporter().getAppearances().isSetImportTextureFiles()) {
						uidCacheManager.initCache(
								UIDCacheType.TEXTURE_IMAGE,
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
				tmpXlinkPool = new WorkerPool<>(
						"xlink_importer_pool",
						minThreads,
						maxThreads,
						PoolSizeAdaptationStrategy.AGGRESSIVE,
						new DBImportXlinkWorkerFactory(cacheTableManager, config, eventDispatcher),
						queueSize,
						false);

				// this pool basically works on the data import
				dbWorkerPool = new WorkerPool<>(
						"db_importer_pool",
						minThreads,
						maxThreads,
						PoolSizeAdaptationStrategy.AGGRESSIVE,
						new DBImportWorkerFactory(schemaMapping,
								cityGMLBuilder,
								tmpXlinkPool,
								uidCacheManager,
								filter,
								affineTransformer,
								importLogger,
								config,
								eventDispatcher),
						queueSize,
						false);

				// this worker pool unmarshals the input file and passes xml chunks to the dbworker pool
				featureWorkerPool = new WorkerPool<>(
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
					log.error("Failed to start database import worker pool. Check the database connection pool settings.");
					return false;
				}

				// ok, preparation done. start parsing the input file
				CityGMLReader reader = null;
				try {
					reader = in.createFilteredCityGMLReader(
							in.createCityGMLReader(file.getFile().toString(), file.openStream()), inputFilter);

					while (shouldRun && reader.hasNext()) {
						XMLChunk chunk = reader.nextChunk();

						if (filter.isSetCounterFilter()) {
							elementCounter++;

							if (elementCounter < filter.getCounterFilter().getLowerLimit())
								continue;

							if (elementCounter > filter.getCounterFilter().getUpperLimit())
								break;
						}

						featureWorkerPool.addWork(chunk);
					}					
				} catch (CityGMLReadException | IOException e) {
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
					log.info("Resolving XLink references.");
					xlinkResolverPool = new WorkerPool<>(
							"xlink_resolver_pool",
							minThreads,
							maxThreads,
							PoolSizeAdaptationStrategy.AGGRESSIVE,
							new DBImportXlinkResolverWorkerFactory(tmpXlinkPool,
									uidCacheManager,
									cacheTableManager,
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
					log.warn(xmlValidationErrorCounter + " error(s) encountered while validating the document.");

				xmlValidationErrorCounter = 0;
			} catch (CityGMLImportException e) {
				throw e;
			} catch (IOException e) {
				throw new CityGMLImportException("Failed to process import file.", e);
			} catch (Throwable e) {
				throw new CityGMLImportException("An unexpected error occurred.", e);
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
						log.error("Failed to shutdown gml:id cache: " + e.getMessage());
						shouldRun = false;
					}
				}

				if (cacheTableManager != null) {
					try {
						log.info("Cleaning temporary cache.");
						cacheTableManager.dropAll();
					} catch (SQLException e) {
						log.error("SQL error while cleaning temporary cache: " + e.getMessage());
						shouldRun = false;
					}
				}

				if (importLogger != null) {
					try {
						importLogger.close(shouldRun);
					} catch (IOException e) {
						log.error("Failed to finish logging of imported top-level features.");
						log.warn("The feature import log is most likely corrupt.");
						shouldRun = false;
					}
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
					log.error("Database error while activating indexes: " + e.getMessage());
					return false;
				}
			}
		}

		// show imported features
		if (!objectCounter.isEmpty()) {
			log.info("Imported city objects:");			
			Map<String, Long> typeNames = Util.mapObjectCounter(objectCounter, schemaMapping);
			typeNames.keySet().forEach(object -> log.info(object + ": " + typeNames.get(object)));			
		}

		// show processed geometries
		if (!geometryCounter.isEmpty())
			log.info("Processed geometry objects: " + geometryCounter.values().stream().reduce(0L, Long::sum));

		if (shouldRun)
			log.info("Total import time: " + Util.formatElapsedTime(System.currentTimeMillis() - start) + ".");

		return shouldRun;
	}

	private void manageIndexes(boolean enable, boolean workOnSpatialIndexes) throws SQLException {
		AbstractUtilAdapter utilAdapter = databaseAdapter.getUtil();
		log.info((enable ? "Activating " : "Deactivating ") + (workOnSpatialIndexes ? "spatial" : "normal") + " indexes...");

		IndexStatusInfo indexStatus;
		if (enable) {
			indexStatus = workOnSpatialIndexes ? utilAdapter.createSpatialIndexes() : utilAdapter.createNormalIndexes();
		} else {
			indexStatus = workOnSpatialIndexes ? utilAdapter.dropSpatialIndexes() : utilAdapter.dropNormalIndexes();
		}

		if (indexStatus != null) {
			IndexStatus expectedStatus = enable ? IndexStatus.VALID : IndexStatus.DROPPED;
			for (IndexInfoObject indexObj : indexStatus.getIndexObjects()) {
				if (indexObj.getStatus() != expectedStatus) {
					log.error("FAILED: " + indexObj.toString());
					if (indexObj.hasErrorMessage())
						log.error("Error cause: " + indexObj.getErrorMessage());
				}
			}
		}
	}

	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.OBJECT_COUNTER) {
			Map<Integer, Long> counter = ((ObjectCounterEvent)e).getCounter();

			for (Entry<Integer, Long> entry : counter.entrySet()) {
				Long tmp = objectCounter.get(entry.getKey());
				objectCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());
			}
		}

		else if (e.getEventType() == EventType.GEOMETRY_COUNTER) {
			Map<GMLClass, Long> counter = ((GeometryCounterEvent)e).getCounter();

			for (Entry<GMLClass, Long> entry : counter.entrySet()) {
				Long tmp = geometryCounter.get(entry.getKey());
				geometryCounter.put(entry.getKey(), tmp == null ? entry.getValue() : tmp + entry.getValue());
			}
		}

		else if (e.getEventType() == EventType.INTERRUPT) {
			if (isInterrupted.compareAndSet(false, true)) {
				shouldRun = false;
				InterruptEvent interruptEvent = (InterruptEvent)e;

				if (interruptEvent.getCause() != null) {
					Throwable cause = interruptEvent.getCause();

					if (cause instanceof SQLException) {
						Iterator<Throwable> iter = ((SQLException)cause).iterator();
						log.error("A SQL error occurred: " + iter.next().getMessage());
						while (iter.hasNext())
							log.error("Cause: " + iter.next().getMessage());
					} else {
						log.error("An error occurred: " + cause.getMessage());
						while ((cause = cause.getCause()) != null)
							log.error(cause.getClass().getTypeName() + ": " + cause.getMessage());
					}
				}

				String msg = interruptEvent.getLogMessage();
				if (msg != null)
					log.log(interruptEvent.getLogLevelType(), msg);

				if (directoryScanner != null)
					directoryScanner.cancel();
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
			log.log(type, msg.toString());

			xmlValidationErrorCounter++;
			return allErrors;
		}
	}

}
