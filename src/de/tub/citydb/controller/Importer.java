package de.tub.citydb.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

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
import de.tub.citydb.config.project.importer.ImpGmlId;
import de.tub.citydb.config.project.importer.ImpIndex;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.cache.DBGmlIdLookupServerEnum;
import de.tub.citydb.db.cache.DBGmlIdLookupServerManager;
import de.tub.citydb.db.cache.DBImportCache;
import de.tub.citydb.db.temp.DBTempTableManager;
import de.tub.citydb.db.temp.model.DBTempTableModelEnum;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.db.xlink.resolver.DBXlinkSplitter;
import de.tub.citydb.event.Event;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.EventListener;
import de.tub.citydb.event.EventType;
import de.tub.citydb.event.info.LogMessageEvent;
import de.tub.citydb.event.statistic.FeatureCounterEvent;
import de.tub.citydb.event.statistic.GeometryCounterEvent;
import de.tub.citydb.filter.ImportFilter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.sax.SAXErrorHandler;
import de.tub.citydb.sax.SAXNamespaceMapper;
import de.tub.citydb.sax.SAXSplitter;
import de.tub.citydb.sax.SAXBuffer.SAXEvent;
import de.tub.citydb.util.DBUtil;
import de.tub.citygml4j.CityGMLFactory;
import de.tub.citygml4j.model.citygml.CityGMLClass;
import de.tub.citygml4j.model.citygml.core.CityGMLBase;
import de.tub.citygml4j.model.gml.GMLClass;

public class Importer implements EventListener {
	private Logger LOG = Logger.getInstance();

	private final JAXBContext jaxbContext;
	private final DBConnectionPool dbPool;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private ReentrantLock mainLock = new ReentrantLock();

	private WorkerPool<CityGMLBase> dbWorkerPool;
	private WorkerPool<Vector<SAXEvent>> featureWorkerPool;
	private WorkerPool<DBXlink> tmpXlinkPool;
	private WorkerPool<DBXlink> xlinkResolverPool;
	private DBTempTableManager dbTempTableManager;
	private DBXlinkSplitter tmpSplitter;
	private SAXParserFactory factory;

	private FileInputStream fileIn;
	private volatile boolean shouldRun = true;
	boolean success = false;
	private HashMap<CityGMLClass, Long> featureCounterMap;
	private HashMap<GMLClass, Long> geometryCounterMap;
	private DBGmlIdLookupServerManager lookupServerManager;
	private CityGMLFactory cityGMLFactory;

	private int runState;
	private final int PARSING = 1;
	private final int XLINK_RESOLVING = 2;

	public Importer(JAXBContext jaxbContext, DBConnectionPool dbPool, Config config, EventDispatcher eventDispatcher) {
		this.jaxbContext = jaxbContext;
		this.dbPool = dbPool;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);

		featureCounterMap = new HashMap<CityGMLClass, Long>();
		geometryCounterMap = new HashMap<GMLClass, Long>();
	}

	public boolean doProcess() {
		// determine pool sizes
		int coreSize = 2;
		int maxSize = 2;
		int commitAfter = 20;

		de.tub.citydb.config.project.system.System system = config.getProject().getImporter().getSystem();
		Database database = config.getProject().getDatabase();
		ImpIndex index = config.getProject().getImporter().getIndexes();
		Internal intConfig = config.getInternal();
		ImpGmlId gmlId = config.getProject().getImporter().getGmlId();

		Integer coreSizeProp = system.getThreadPool().getDefaultPool().getMinThreads();
		if (coreSizeProp != null && coreSizeProp > 0)
			coreSize = coreSizeProp;

		Integer maxSizeProp = system.getThreadPool().getDefaultPool().getMaxThreads();
		if (maxSizeProp != null && maxSizeProp > 0)
			maxSize = maxSizeProp;

		Integer commitAfterProp = database.getUpdateBatching().getFeatureBatchValue();
		if (commitAfterProp != null && commitAfterProp > 0)
			commitAfter = commitAfterProp;

		// check thread input
		if (coreSize > maxSize)
			coreSize = maxSize;

		// refill values
		system.getThreadPool().getDefaultPool().setMinThreads(coreSize);
		system.getThreadPool().getDefaultPool().setMaxThreads(maxSize);
		database.getUpdateBatching().setFeatureBatchValue(commitAfter);

		// check lookup cache
		Integer geometryCache = system.getGmlIdLookupServer().getGeometry().getCacheSize();
		Integer featureCache = system.getGmlIdLookupServer().getFeature().getCacheSize();
		Float geometryCacheDrainFactor = system.getGmlIdLookupServer().getGeometry().getPageFactor();
		Float featureCacheDrainFactor = system.getGmlIdLookupServer().getFeature().getPageFactor();
		Integer geometryPartitions = system.getGmlIdLookupServer().getGeometry().getPartitions();
		Integer featurePartitions = system.getGmlIdLookupServer().getFeature().getPartitions();
		Integer batchSize = database.getUpdateBatching().getGmlIdLookupServerBatchValue();

		if (geometryCache <= 0)
			geometryCache = 200000;

		if (featureCache <= 0)
			featureCache = 200000;

		if (geometryCacheDrainFactor <= 0 || geometryCacheDrainFactor > 1)
			geometryCacheDrainFactor = .85f;

		if (featureCacheDrainFactor <= 0 || featureCacheDrainFactor > 1)
			featureCacheDrainFactor = .85f;

		if (geometryPartitions <= 0 || geometryPartitions >= 100)
			geometryPartitions = 10;

		if (featurePartitions <= 0 || featurePartitions >= 100)
			featurePartitions = 10;

		if (batchSize == null || batchSize <= 0 || batchSize > Internal.ORACLE_MAX_BATCH_SIZE)
			batchSize = Internal.ORACLE_MAX_BATCH_SIZE;

		// refill values
		system.getGmlIdLookupServer().getGeometry().setCacheSize(geometryCache);
		system.getGmlIdLookupServer().getFeature().setCacheSize(featureCache);
		system.getGmlIdLookupServer().getGeometry().setPageFactor(geometryCacheDrainFactor);
		system.getGmlIdLookupServer().getFeature().setPageFactor(featureCacheDrainFactor);
		system.getGmlIdLookupServer().getGeometry().setPartitions(geometryPartitions);
		system.getGmlIdLookupServer().getFeature().setPartitions(featurePartitions);
		database.getUpdateBatching().setGmlIdLookupServerBatchValue(batchSize);

		// calc queueSize
		int queueSize = maxSize * 2;

		// checking workspace... this should be improved in future...
		String workspace = database.getWorkspace().getImportWorkspace();
		if (workspace != null) {
			workspace = workspace.trim();

			if (!workspace.toUpperCase().equals("LIVE")) {
				boolean workspaceExists = dbPool.checkWorkspace(workspace);

				if (!workspaceExists) {
					LOG.error("Der Datenbank-Workspace '" + workspace + "' ist nicht verfügbar.");
					return false;
				} else {
					LOG.info("Der Datenbank-Workspace '" + workspace + "' wird gewählt.");
				}
			}
		}

		// deactivate database indexes
		if (index.isSpatialIndexModeDeactivate() || index.isSpatialIndexModeDeactivateActivate() ||
				index.isNormalIndexModeDeactivate() || index.isNormalIndexModeDeactivateActivate()) {
			try {
				DBUtil dbUtil = new DBUtil(dbPool);

				if (index.isSpatialIndexModeDeactivate() || index.isSpatialIndexModeDeactivateActivate()) {
					LOG.info("Räumliche Indizes werden deaktiviert...");
					String[] result = dbUtil.dropSpatialIndexes();

					if (result != null) {
						for (String line : result) {
							String[] parts = line.split(":");

							if (!parts[4].equals("DROPPED")) {
								LOG.error("FAILED: " + parts[0] + " auf " + parts[1] + "(" + parts[2] + ")");
								String errMsg = dbUtil.errorMessage(parts[3]);
								LOG.error("Fehlerursache: " + errMsg);
							}
						}
					}
				}

				if (index.isNormalIndexModeDeactivate() || index.isNormalIndexModeDeactivateActivate()) {
					LOG.info("Normale Indizes werden deaktiviert...");
					String[] result = dbUtil.dropNormalIndexes();

					if (result != null) {
						for (String line : result) {
							String[] parts = line.split(":");

							if (!parts[4].equals("DROPPED")) {
								LOG.error("FAILED: " + parts[0] + " auf " + parts[1] + "(" + parts[2] + ")");
								String errMsg = dbUtil.errorMessage(parts[3]);
								LOG.error("Fehlerursache: " + errMsg);
							}
						}
					}
				}

			} catch (SQLException e) {
				LOG.error("Datenbankfehler beim Deaktivieren der Indizes: " + e.getMessage());
				return false;
			}			
		}

		String[] fileNames = intConfig.getImportFileName().trim().split("\n");
		int fileCounter = 0;

		// CityGML object factory
		cityGMLFactory = new CityGMLFactory();

		// import filter
		ImportFilter importFilter = new ImportFilter(config);

		// adding listeners
		eventDispatcher.addListener(EventType.LogMessage, this);
		eventDispatcher.addListener(EventType.FeatureCounter, this);
		eventDispatcher.addListener(EventType.GeometryCounter, this);

		while (shouldRun && fileCounter < fileNames.length) {
			try {
				runState = PARSING;

				String fileName = fileNames[fileCounter].trim();
				if (fileName.equals("")) {
					fileCounter++;
					continue;
				}

				File file = new File(fileName);
				if (!file.exists() || !file.canRead() || file.isDirectory()) {
					LOG.error("Datei '" + fileName + "' kann nicht gelesen werden.");
					fileCounter++;
					continue;
				}

				// set path variables
				File path = new File(file.getAbsolutePath());
				intConfig.setImportPath(path.getParent());

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
				dbTempTableManager = new DBTempTableManager(dbPool, maxSize);

				// create instance of gml:id lookup server manager...
				lookupServerManager = new DBGmlIdLookupServerManager(eventDispatcher);

				// ...and start servers
				try {
					lookupServerManager.initServer(
							DBGmlIdLookupServerEnum.GEOMETRY,
							new DBImportCache(dbTempTableManager, DBTempTableModelEnum.GMLID_GEOMETRY, geometryPartitions, batchSize),
							geometryCache,
							geometryCacheDrainFactor,
							maxSize);

					lookupServerManager.initServer(
							DBGmlIdLookupServerEnum.FEATURE,
							new DBImportCache(dbTempTableManager, DBTempTableModelEnum.GMLID_FEATURE, featurePartitions, batchSize),
							featureCache,
							featureCacheDrainFactor,
							maxSize);
				} catch (SQLException sqlEx) {
					LOG.error("SQL-Fehler bei der Initialisierung des Imports: " + sqlEx.getMessage());
					fileCounter++;
					continue;
				}

				// creating worker pools needed for data import
				// this pool is for registering xlinks
				tmpXlinkPool = new WorkerPool<DBXlink>(
						coreSize,
						maxSize,
						new DBImportXlinkWorkerFactory(dbTempTableManager, config, eventDispatcher),
						queueSize,
						false);

				// this pool basically works on the data import
				dbWorkerPool = new WorkerPool<CityGMLBase>(
						coreSize,
						maxSize,
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
				featureWorkerPool = new WorkerPool<Vector<SAXEvent>>(
						coreSize,
						maxSize,
						new FeatureReaderWorkerFactory(jaxbContext, dbWorkerPool, cityGMLFactory),
						queueSize,
						false);

				// prestart threads
				tmpXlinkPool.prestartCoreWorkers();
				dbWorkerPool.prestartCoreWorkers();
				featureWorkerPool.prestartCoreWorkers();

				// create a new XML parser
				XMLReader reader = null;
				try {
					reader = factory.newSAXParser().getXMLReader();
				} catch (SAXException saxE) {
					LOG.error("I/O-Fehler: " + saxE.getMessage());
				} catch (ParserConfigurationException pcE) {
					LOG.error("I/O-Fehler: " + pcE.getMessage());
				}

				// prepare a xml splitter
				SAXSplitter splitter = new SAXSplitter(featureWorkerPool, config);

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
					LOG.error("I/O-Fehler: " + e1.getMessage());
					fileCounter++;
					continue;
				}

				// ok, preparation done. inform user...
				LOG.info("Importiere Datei: " + fileName);
				InputSource input = new InputSource(fileIn);

				// this is were we are starting parsing the input file
				try {
					if (shouldRun)
						nsMapper.parse(input);
				} catch (IOException ioE) {
					// we catch "Read error" because we produce this one when interrupting the import
					if (!ioE.getMessage().equals("Read error")) {
						LOG.error("I/O-Fehler: " + ioE.getMessage());
					}
					shouldRun = false;
				} catch (SAXException saxE) {
					LOG.error("I/O-Fehler: " + saxE.getMessage());
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
					LOG.info("Xlink-Verweise werden verarbeitet.");
					xlinkResolverPool = new WorkerPool<DBXlink>(
							coreSize,
							maxSize,
							new DBImportXlinkResolverWorkerFactory(dbPool, 
									tmpXlinkPool, 
									lookupServerManager, 
									dbTempTableManager, 
									importFilter,
									config, 
									eventDispatcher),
									queueSize,
									false);

					// and prestart its workers
					xlinkResolverPool.prestartCoreWorkers();

					// we also need a splitter which extracts the data from the temp tables
					tmpSplitter = new DBXlinkSplitter(dbTempTableManager, 
							xlinkResolverPool, 
							tmpXlinkPool, 
							eventDispatcher);

					// resolve xlinks
					try {
						if (shouldRun)
							tmpSplitter.startQuery();
					} catch (SQLException sqlE) {
						LOG.error("SQL-Fehler: " + sqlE.getMessage());
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

				// finally clean up and join eventDispatcher
				try {
					LOG.info("Temporärer Cache wird bereinigt.");
					dbTempTableManager.dropAll();
					eventDispatcher.join();
				} catch (SQLException sqlE) {
					LOG.error("SQL-Fehler: " + sqlE.getMessage());
				} catch (InterruptedException iE) {
					//
				}
				
				success = true;
				fileCounter++;
				
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
				dbTempTableManager = null;
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
					DBUtil dbUtil = new DBUtil(dbPool);

					if (index.isSpatialIndexModeDeactivateActivate()) {
						LOG.info("Räumliche Indizes werden aktiviert. Dieser Vorgang kann sehr viel Zeit in Anspruch nehmen...");
						String[] result = dbUtil.createSpatialIndexes();

						if (result != null) {
							for (String line : result) {
								String[] parts = line.split(":");

								if (!parts[4].equals("VALID")) {
									LOG.error("FAILED: " + parts[0] + " auf " + parts[1] + "(" + parts[2] + ")");
									String errMsg = dbUtil.errorMessage(parts[3]);
									LOG.error("Fehlerursache: " + errMsg);
								}
							}
						}
					}

					if (index.isNormalIndexModeDeactivateActivate()) {
						LOG.info("Normale Indizes werden aktiviert. Dieser Vorgang kann sehr viel Zeit in Anspruch nehmen...");
						String[] result = dbUtil.createNormalIndexes();

						if (result != null) {
							for (String line : result) {
								String[] parts = line.split(":");

								if (!parts[4].equals("VALID")) {
									LOG.error("FAILED: " + parts[0] + " auf " + parts[1] + "(" + parts[2] + ")");
									String errMsg = dbUtil.errorMessage(parts[3]);
									LOG.error("Fehlerursache: " + errMsg);
								}
							}
						}
					}

				} catch (SQLException e) {
					LOG.error("Datenbankfehler beim Aktivieren der Indizes: " + e.getMessage());
					return false;
				}
			}
		}

		// show imported features
		if (success) {
			if (!featureCounterMap.isEmpty()) {
				LOG.info("Importierte CityGML Objekte:");
				for (CityGMLClass type : featureCounterMap.keySet())
					LOG.info(type + ": " + featureCounterMap.get(type));
			}

			long geometryObjects = 0;
			for (GMLClass type : geometryCounterMap.keySet())
				geometryObjects += geometryCounterMap.get(type);

			if (geometryObjects != 0)
				LOG.info("Verarbeitete Geometrie-Objekte: " + geometryObjects);
		}

		if (dbTempTableManager != null) {
			try {
				LOG.info("Temporärer Cache wird bereinigt.");
				dbTempTableManager.dropAll();
				dbTempTableManager = null;
			} catch (SQLException sqlEx) {
				LOG.error("SQL-Fehler bei der Beendigung des Imports: " + sqlEx.getMessage());
			}
		}

		if (!shouldRun)
			return false;

		return success;
	}

	public void shutdown(Thread importerThread) throws IOException {
		if (!shouldRun)
			return;

		final ReentrantLock lock = this.mainLock;
		lock.lock();
		try {
			shouldRun = false;
			success = false;

			if (runState == PARSING && fileIn != null)
				fileIn.close();
			else if (runState == XLINK_RESOLVING && tmpSplitter != null)
				tmpSplitter.shutdown();

			importerThread.interrupt();

		} finally {
			lock.unlock();
		}
	}

	// react on events we are receiving via the eventDispatcher
	@Override
	public void handleEvent(Event e) throws Exception {
		if (e.getEventType() == EventType.LogMessage) {
			String msg = ((LogMessageEvent)e).getMessage();
			msg = msg.replaceAll("\n*$", "");

			switch (((LogMessageEvent)e).getMessageType()) {
			case DEBUG:
				LOG.debug(msg);
				break;
			case INFO:
				LOG.info(msg);
				break;
			case WARN:
				LOG.warn(msg);
				break;
			case ERROR:
				LOG.error(msg);
				break;
			}
		}

		else if (e.getEventType() == EventType.FeatureCounter) {
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

	}
}
