/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.core.CityGMLBase;
import org.citygml4j.model.gml.GMLClass;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.tub.citydb.concurrent.DBImportWorkerFactory;
import de.tub.citydb.concurrent.DBImportXlinkResolverWorkerFactory;
import de.tub.citydb.concurrent.DBImportXlinkWorkerFactory;
import de.tub.citydb.concurrent.FeatureReaderWorkerFactory;
import de.tub.citydb.concurrent.WorkerPool;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.Index;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.config.project.importer.ImportGmlId;
import de.tub.citydb.config.project.importer.LocalXMLSchemaType;
import de.tub.citydb.config.project.importer.XMLValidation;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.cache.CacheManager;
import de.tub.citydb.db.cache.model.CacheTableModelEnum;
import de.tub.citydb.db.gmlId.DBGmlIdLookupServerEnum;
import de.tub.citydb.db.gmlId.DBGmlIdLookupServerManager;
import de.tub.citydb.db.gmlId.DBImportCache;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.db.xlink.resolver.DBXlinkSplitter;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.concurrent.InterruptEvent;
import de.tub.citydb.event.statistic.CounterEvent;
import de.tub.citydb.event.statistic.CounterType;
import de.tub.citydb.event.statistic.FeatureCounterEvent;
import de.tub.citydb.event.statistic.GeometryCounterEvent;
import de.tub.citydb.event.statistic.StatusDialogMessage;
import de.tub.citydb.event.statistic.StatusDialogProgressBar;
import de.tub.citydb.event.statistic.StatusDialogTitle;
import de.tub.citydb.filter.ImportFilter;
import de.tub.citydb.io.InputFileHandler;
import de.tub.citydb.log.Logger;
import de.tub.citydb.sax.SAXBuffer;
import de.tub.citydb.sax.SAXErrorHandler;
import de.tub.citydb.sax.SAXNamespaceMapper;
import de.tub.citydb.sax.SAXSplitter;
import de.tub.citydb.util.DBUtil;

public class Importer implements EventListener {
	private final Logger LOG = Logger.getInstance();

	private final JAXBContext jaxbContext;
	private final DBConnectionPool dbPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private WorkerPool<CityGMLBase> dbWorkerPool;
	private WorkerPool<SAXBuffer> featureWorkerPool;
	private WorkerPool<DBXlink> tmpXlinkPool;
	private WorkerPool<DBXlink> xlinkResolverPool;
	private CacheManager cacheManager;
	private DBXlinkSplitter tmpSplitter;
	private SAXParserFactory factory;

	private FileInputStream fileIn;
	private volatile boolean shouldRun = true;
	private AtomicBoolean isInterrupted = new AtomicBoolean(false);
	private EnumMap<CityGMLClass, Long> featureCounterMap;
	private EnumMap<GMLClass, Long> geometryCounterMap;
	private long xmlValidationErrorCounter;
	private DBGmlIdLookupServerManager lookupServerManager;
	private CityGMLFactory cityGMLFactory;
	private DBUtil dbUtil;

	private int runState;
	private final int PARSING = 1;
	private final int XLINK_RESOLVING = 2;

	public Importer(JAXBContext jaxbContext, 
			DBConnectionPool dbPool, 
			Config config, 
			EventDispatcher eventDispatcher) {
		this.jaxbContext = jaxbContext;
		this.dbPool = dbPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);

		featureCounterMap = new EnumMap<CityGMLClass, Long>(CityGMLClass.class);
		geometryCounterMap = new EnumMap<GMLClass, Long>(GMLClass.class);
	}

	public boolean doProcess() {
		// get config shortcuts
		de.tub.citydb.config.project.system.System system = config.getProject().getImporter().getSystem();
		Database database = config.getProject().getDatabase();
		Index index = database.getIndexes();
		Internal intConfig = config.getInternal();
		ImportGmlId gmlId = config.getProject().getImporter().getGmlId();

		// worker pool settings 
		int minThreads = system.getThreadPool().getDefaultPool().getMinThreads();
		int maxThreads = system.getThreadPool().getDefaultPool().getMaxThreads();
		int queueSize = maxThreads * 2;

		// gml:id lookup cache update
		int lookupCacheBatchSize = database.getUpdateBatching().getGmlIdLookupServerBatchValue();

		// adding listeners
		eventDispatcher.addListener(EventType.FeatureCounter, this);
		eventDispatcher.addListener(EventType.GeometryCounter, this);
		eventDispatcher.addListener(EventType.Interrupt, this);

		// checking workspace... this should be improved in future...
		Workspace workspace = database.getWorkspaces().getImportWorkspace();
		if (shouldRun && !workspace.getName().toUpperCase().equals("LIVE")) {
			boolean workspaceExists = dbPool.checkWorkspace(workspace);

			if (!workspaceExists) {
				LOG.error("Database workspace '" + workspace.getName().trim() + "' is not available.");
				return false;
			} else {
				LOG.info("Switching to database workspace '" + workspace.getName().trim() + "'.");
			}
		}

		// deactivate database indexes
		dbUtil = DBUtil.getInstance(dbPool);		
		if (shouldRun && (index.isSpatialIndexModeDeactivate() || index.isSpatialIndexModeDeactivateActivate() ||
				index.isNormalIndexModeDeactivate() || index.isNormalIndexModeDeactivateActivate())) {
			try {
				if (shouldRun && (index.isSpatialIndexModeDeactivate() || index.isSpatialIndexModeDeactivateActivate())) {
					LOG.info("Deactivating spatial indexes...");
					String[] result = dbUtil.dropSpatialIndexes();

					if (result != null) {
						for (String line : result) {
							String[] parts = line.split(":");

							if (!parts[4].equals("DROPPED")) {
								LOG.error("FAILED: " + parts[0] + " auf " + parts[1] + "(" + parts[2] + ")");
								String errMsg = dbUtil.errorMessage(parts[3]);
								LOG.error("Error cause: " + errMsg);
							}
						}
					}
				}

				if (shouldRun && (index.isNormalIndexModeDeactivate() || index.isNormalIndexModeDeactivateActivate())) {
					LOG.info("Deactivating normal indexes...");
					String[] result = dbUtil.dropNormalIndexes();

					if (result != null) {
						for (String line : result) {
							String[] parts = line.split(":");

							if (!parts[4].equals("DROPPED")) {
								LOG.error("FAILED: " + parts[0] + " auf " + parts[1] + "(" + parts[2] + ")");
								String errMsg = dbUtil.errorMessage(parts[3]);
								LOG.error("Error cause: " + errMsg);
							}
						}
					}
				}

			} catch (SQLException e) {
				LOG.error("Database error while deactivating indexes: " + e.getMessage());
				return false;
			}			
		}

		// build list of import files
		LOG.info("Creating list of CityGML files to be imported...");	
		InputFileHandler fileHandler = new InputFileHandler(eventDispatcher);
		List<File> importFiles = fileHandler.getFiles(intConfig.getImportFileName().trim().split("\n"));

		if (!shouldRun)
			return true;

		if (importFiles.size() == 0) {
			LOG.warn("Failed to find CityGML files at the specified locations.");
			return false;
		}

		int fileCounter = 0;
		int remainingFiles = importFiles.size();
		LOG.info("List of import files successfully created.");
		LOG.info(remainingFiles + " file(s) will be imported.");

		// CityGML object factory
		cityGMLFactory = new CityGMLFactory();

		// import filter
		ImportFilter importFilter = new ImportFilter(config, dbUtil);

		// prepare XML validation 
		XMLValidation xmlValidation = config.getProject().getImporter().getXMLValidation();
		config.getInternal().setUseXMLValidation(xmlValidation.isSetUseXMLValidation());
		if (shouldRun && xmlValidation.isSetUseXMLValidation()) {			
			LOG.info("Using XML validation during database import.");
			eventDispatcher.addListener(EventType.Counter, this);

			if (xmlValidation.getUseLocalSchemas().isSet()) {
				LOG.info("Using local schema documents for XML validation.");
				for (LocalXMLSchemaType schema : xmlValidation.getUseLocalSchemas().getSchemas())
					if (schema != null)
						LOG.info("Reading schema: " + schema.value());
			} else
				LOG.info("Using schema documents from xsi:schemaLocation attribute on root element.");
		}

		while (shouldRun && fileCounter < importFiles.size()) {
			try {
				runState = PARSING;

				File file = importFiles.get(fileCounter++);
				intConfig.setImportPath(file.getParent());
				intConfig.setCurrentImportFileName(file.getAbsolutePath());

				eventDispatcher.triggerEvent(new StatusDialogTitle(file.getName()));
				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("import.dialog.cityObj.msg")));
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(true));
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.FILE, --remainingFiles));

				// set gml:id codespace
				if (gmlId.isSetRelativeCodeSpaceMode())
					intConfig.setCurrentGmlIdCodespace(file.getName());
				else if (gmlId.isSetAbsoluteCodeSpaceMode())
					intConfig.setCurrentGmlIdCodespace(file.toString());
				else if (gmlId.isSetUserCodeSpaceMode())
					intConfig.setCurrentGmlIdCodespace(gmlId.getCodeSpace());
				else if (!gmlId.isSetUserCodeSpaceMode())
					intConfig.setCurrentGmlIdCodespace(null);

				// create instance of temp table manager
				cacheManager = new CacheManager(dbPool, maxThreads);

				// create instance of gml:id lookup server manager...
				lookupServerManager = new DBGmlIdLookupServerManager();

				// ...and start servers
				try {
					lookupServerManager.initServer(
							DBGmlIdLookupServerEnum.GEOMETRY,
							new DBImportCache(cacheManager, 
									CacheTableModelEnum.GMLID_GEOMETRY, 
									system.getGmlIdLookupServer().getGeometry().getPartitions(), 
									lookupCacheBatchSize),
									system.getGmlIdLookupServer().getGeometry().getCacheSize(),
									system.getGmlIdLookupServer().getGeometry().getPageFactor(),
									maxThreads);

					lookupServerManager.initServer(
							DBGmlIdLookupServerEnum.FEATURE,
							new DBImportCache(cacheManager, 
									CacheTableModelEnum.GMLID_FEATURE, 
									system.getGmlIdLookupServer().getFeature().getPartitions(),
									lookupCacheBatchSize),
									system.getGmlIdLookupServer().getFeature().getCacheSize(),
									system.getGmlIdLookupServer().getFeature().getPageFactor(),
									maxThreads);
				} catch (SQLException sqlEx) {
					LOG.error("SQL error while initializing database import: " + sqlEx.getMessage());
					continue;
				}

				// creating worker pools needed for data import
				// this pool is for registering xlinks
				tmpXlinkPool = new WorkerPool<DBXlink>(
						minThreads,
						maxThreads,
						new DBImportXlinkWorkerFactory(cacheManager, config, eventDispatcher),
						queueSize,
						false);

				// this pool basically works on the data import
				dbWorkerPool = new WorkerPool<CityGMLBase>(
						minThreads,
						maxThreads,
						new DBImportWorkerFactory(dbPool, 
								tmpXlinkPool, 
								lookupServerManager, 
								cityGMLFactory, 
								importFilter,
								config, 
								eventDispatcher),
								queueSize,
								false);

				// this worker pool parses the xml file and passes xml chunks to the dbworker pool
				featureWorkerPool = new WorkerPool<SAXBuffer>(
						minThreads,
						maxThreads,
						new FeatureReaderWorkerFactory(jaxbContext, dbWorkerPool, cityGMLFactory, eventDispatcher, config),
						queueSize,
						false);

				// create a new XML parser
				XMLReader reader = null;
				try {
					reader = factory.newSAXParser().getXMLReader();
				} catch (SAXException saxE) {
					LOG.error("I/O error: " + saxE.getMessage());
					shouldRun = false;
					continue;
				} catch (ParserConfigurationException pcE) {
					LOG.error("I/O error: " + pcE.getMessage());
					shouldRun = false;
					continue;
				}

				// prepare a xml splitter
				SAXSplitter splitter = new SAXSplitter(featureWorkerPool, config, eventDispatcher);

				// prepare an xml errorHandler
				SAXErrorHandler errorHandler = new SAXErrorHandler();

				// prepare namespaceFilter used for mapping xml namespaces
				SAXNamespaceMapper nsMapper = new SAXNamespaceMapper(reader);
				nsMapper.setNamespaceMapping("http://www.citygml.org/citygml/0/3/0", "http://www.citygml.org/citygml/1/0/0");
				nsMapper.setNamespaceMapping("http://www.citygml.org/citygml/0/4/0", "http://www.citygml.org/citygml/1/0/0");

				// connect both components
				nsMapper.setContentHandler(splitter);
				nsMapper.setErrorHandler(errorHandler);

				// open stream on input file
				try {
					if (shouldRun)
						fileIn = new FileInputStream(file);
				} catch (FileNotFoundException e1) {
					LOG.error("I/O error: " + e1.getMessage());
					continue;
				}

				// prestart threads
				tmpXlinkPool.prestartCoreWorkers();
				dbWorkerPool.prestartCoreWorkers();
				featureWorkerPool.prestartCoreWorkers();

				// ok, preparation done. inform user and  start parsing the input file
				try {
					if (shouldRun) {
						LOG.info("Importing file: " + file.toString());						
						nsMapper.parse(new InputSource(fileIn));
					}
				} catch (IOException ioE) {
					// we catch "Read error" and "Bad file descriptor" because we produce these ones when interrupting the import
					if (!(ioE.getMessage().equals("Read error") || ioE.getMessage().equals("Bad file descriptor"))) {
						LOG.error("I/O error: " + ioE.getMessage());
						xmlValidationErrorCounter++;
					}						
				} catch (SAXException saxE) {
					xmlValidationErrorCounter++;
				}

				// we are done with parsing. so shutdown the workers
				// xlink pool is not shutdown because we need it afterwards
				try {
					featureWorkerPool.shutdownAndWait();
					dbWorkerPool.shutdownAndWait();
					tmpXlinkPool.join();
				} catch (InterruptedException ie) {
					//
				}

				try {
					if (fileIn != null)
						fileIn.close();
				} catch (IOException e) {
					//
				}

				if (shouldRun) {
					runState = XLINK_RESOLVING;

					// get an xlink resolver pool
					LOG.info("Resolving XLink references.");
					xlinkResolverPool = new WorkerPool<DBXlink>(
							minThreads,
							maxThreads,
							new DBImportXlinkResolverWorkerFactory(dbPool, 
									tmpXlinkPool, 
									lookupServerManager, 
									cacheManager, 
									importFilter,
									config, 
									eventDispatcher),
									queueSize,
									false);

					// and prestart its workers
					xlinkResolverPool.prestartCoreWorkers();

					// we also need a splitter which extracts the data from the temp tables
					tmpSplitter = new DBXlinkSplitter(cacheManager, 
							xlinkResolverPool, 
							tmpXlinkPool,
							eventDispatcher);

					// resolve xlinks
					try {
						if (shouldRun)
							tmpSplitter.startQuery();
					} catch (SQLException sqlE) {
						LOG.error("SQL error: " + sqlE.getMessage());
					}

					// shutdown worker pools
					try {
						xlinkResolverPool.shutdownAndWait();
						tmpXlinkPool.shutdownAndWait();
					} catch (InterruptedException iE) {
						//
					}
				} else {
					// at least shutdown tmp xlink pool
					try {
						tmpXlinkPool.shutdownAndWait();
					} catch (InterruptedException iE) {
						//
					}
				}

				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("import.dialog.finish.msg")));
				eventDispatcher.triggerEvent(new StatusDialogProgressBar(true));

				// finally clean up and join eventDispatcher
				try {
					LOG.info("Cleaning temporary cache.");
					cacheManager.dropAll();
				} catch (SQLException sqlE) {
					LOG.error("SQL error: " + sqlE.getMessage());
				}

				try {
					lookupServerManager.shutdownAll();
				} catch (SQLException e) {
					LOG.error("SQL error: " + e.getMessage());
				}

				try {
					dbPool.refresh();
				} catch (SQLException e) {
					LOG.error("SQL error: " + e.getMessage());
				}

				try {
					eventDispatcher.join();
				} catch (InterruptedException e) {
					// 
				}

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

				if (tmpXlinkPool != null && !tmpXlinkPool.isTerminated())
					tmpXlinkPool.shutdownNow();

				if (xlinkResolverPool != null && !xlinkResolverPool.isTerminated())
					xlinkResolverPool.shutdownNow();

				// set to null
				cacheManager = null;
				lookupServerManager = null;
				tmpXlinkPool = null;
				dbWorkerPool = null;
				featureWorkerPool = null;
				xlinkResolverPool = null;
				tmpSplitter = null;
			}
		} 	

		// reactivate database indexes
		if (shouldRun) {
			if (index.isSpatialIndexModeDeactivateActivate() || index.isNormalIndexModeDeactivateActivate()) {
				try {
					if (index.isSpatialIndexModeDeactivateActivate()) {
						LOG.info("Activating spatial indexes. This can take long time...");
						String[] result = dbUtil.createSpatialIndexes();

						if (result != null) {
							for (String line : result) {
								String[] parts = line.split(":");

								if (!parts[4].equals("VALID")) {
									LOG.error("FAILED: " + parts[0] + " auf " + parts[1] + "(" + parts[2] + ")");
									String errMsg = dbUtil.errorMessage(parts[3]);
									LOG.error("Error cause: " + errMsg);
								}
							}
						}
					}

					if (index.isNormalIndexModeDeactivateActivate()) {
						LOG.info("Activating normal indexes. This can take long time...");
						String[] result = dbUtil.createNormalIndexes();

						if (result != null) {
							for (String line : result) {
								String[] parts = line.split(":");

								if (!parts[4].equals("VALID")) {
									LOG.error("FAILED: " + parts[0] + " auf " + parts[1] + "(" + parts[2] + ")");
									String errMsg = dbUtil.errorMessage(parts[3]);
									LOG.error("Error cause: " + errMsg);
								}
							}
						}
					}

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
		for (GMLClass type : geometryCounterMap.keySet())
			geometryObjects += geometryCounterMap.get(type);

		if (geometryObjects != 0)
			LOG.info("Processed geometry objects: " + geometryObjects);

		// cleaning temp cache
		if (cacheManager != null) {
			try {
				LOG.info("Cleaning temporary cache.");
				cacheManager.dropAll();
				cacheManager = null;
			} catch (SQLException sqlEx) {
				LOG.error("SQL error while finishing database import: " + sqlEx.getMessage());
			}
		}

		return shouldRun;
	}

	// react on events we are receiving via the eventDispatcher
	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.FeatureCounter) {
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

		else if (e.getEventType() == EventType.GeometryCounter) {
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

		else if (e.getEventType() == EventType.Interrupt) {
			if (isInterrupted.compareAndSet(false, true)) {
				switch (((InterruptEvent)e).getInterruptType()) {
				case READ_SCHEMA_ERROR:
					xmlValidationErrorCounter++;
				case USER_ABORT:
					shouldRun = false;
					break;
				}

				String log = ((InterruptEvent)e).getLogMessage();
				if (log != null)
					LOG.log(((InterruptEvent)e).getLogLevelType(), log);

				if (runState == PARSING && fileIn != null)
					try {
						fileIn.close();
					} catch (IOException ioE) {
						//
					}
					else if (runState == XLINK_RESOLVING && tmpSplitter != null)
						tmpSplitter.shutdown();
			}
		}

		else if (e.getEventType() == EventType.Counter && 
				((CounterEvent)e).getType() == CounterType.XML_VALIDATION_ERROR) {
			xmlValidationErrorCounter += ((CounterEvent)e).getCounter();
		}
	}

}
