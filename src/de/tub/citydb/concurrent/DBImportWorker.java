package de.tub.citydb.concurrent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.core.CityGMLBase;
import org.citygml4j.model.citygml.core.CityObject;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.gml.AbstractFeature;
import org.citygml4j.model.gml.Code;

import de.tub.citydb.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.database.Workspace;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.gmlId.DBGmlIdLookupServerManager;
import de.tub.citydb.db.importer.DBAppearance;
import de.tub.citydb.db.importer.DBBuilding;
import de.tub.citydb.db.importer.DBCityFurniture;
import de.tub.citydb.db.importer.DBCityObjectGroup;
import de.tub.citydb.db.importer.DBGenericCityObject;
import de.tub.citydb.db.importer.DBImporterEnum;
import de.tub.citydb.db.importer.DBImporterManager;
import de.tub.citydb.db.importer.DBLandUse;
import de.tub.citydb.db.importer.DBPlantCover;
import de.tub.citydb.db.importer.DBReliefFeature;
import de.tub.citydb.db.importer.DBSolitaryVegetatObject;
import de.tub.citydb.db.importer.DBTransportationComplex;
import de.tub.citydb.db.importer.DBWaterBody;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.statistic.CounterEvent;
import de.tub.citydb.event.statistic.CounterType;
import de.tub.citydb.event.statistic.FeatureCounterEvent;
import de.tub.citydb.event.statistic.GeometryCounterEvent;
import de.tub.citydb.filter.ImportFilter;
import de.tub.citydb.filter.feature.BoundingBoxFilter;
import de.tub.citydb.filter.feature.FeatureClassFilter;
import de.tub.citydb.filter.feature.GmlIdFilter;
import de.tub.citydb.filter.feature.GmlNameFilter;
import de.tub.citydb.log.Logger;

public class DBImportWorker implements Worker<CityGMLBase> {
	private final Logger LOG = Logger.getInstance();

	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<CityGMLBase> workQueue = null;
	private CityGMLBase firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final DBConnectionPool dbConnectionPool;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private final CityGMLFactory cityGMLFactory;
	private final ImportFilter importFilter;
	private Connection batchConn;
	private Connection commitConn;
	private DBImporterManager dbImporterManager;
	private int updateCounter = 0;
	private int commitAfter = 20;

	// filter
	private FeatureClassFilter featureClassFilter;
	private BoundingBoxFilter featureBoundingBoxFilter;
	private GmlIdFilter featureGmlIdFilter;
	private GmlNameFilter featureGmlNameFilter;

	public DBImportWorker(DBConnectionPool dbConnectionPool,
			WorkerPool<DBXlink> tmpXlinkPool,
			DBGmlIdLookupServerManager lookupServerManager,
			CityGMLFactory cityGMLFactory,
			ImportFilter importFilter,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.dbConnectionPool = dbConnectionPool;
		this.tmpXlinkPool = tmpXlinkPool;
		this.lookupServerManager = lookupServerManager;
		this.cityGMLFactory = cityGMLFactory;
		this.importFilter = importFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		init();
	}

	private void init() throws SQLException {
		batchConn = dbConnectionPool.getConnection();
		batchConn.setAutoCommit(false);

		commitConn = dbConnectionPool.getConnection();
		commitConn.setAutoCommit(true);

		// try and change workspace for both connections if needed
		Database database = config.getProject().getDatabase();
		Workspace workspace = database.getWorkspaces().getImportWorkspace();
		dbConnectionPool.changeWorkspace(batchConn, workspace);
		dbConnectionPool.changeWorkspace(commitConn, workspace);

		// init filter 
		featureClassFilter = importFilter.getFeatureClassFilter();
		featureBoundingBoxFilter = importFilter.getBoundingBoxFilter();
		featureGmlIdFilter = importFilter.getGmlIdFilter();
		featureGmlNameFilter = importFilter.getGmlNameFilter();		

		dbImporterManager = new DBImporterManager(
				batchConn,
				commitConn,
				config,
				tmpXlinkPool,
				lookupServerManager,
				cityGMLFactory,
				eventDispatcher);

		Integer commitAfterProp = database.getUpdateBatching().getFeatureBatchValue();
		if (commitAfterProp != null && commitAfterProp > 0)
			commitAfter = commitAfterProp;
	}

	@Override
	public Thread getThread() {
		return workerThread;
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
	public void setFirstWork(CityGMLBase firstWork) {
		this.firstWork = firstWork;
	}

	@Override
	public void setThread(Thread workerThread) {
		this.workerThread = workerThread;
	}

	@Override
	public void setWorkQueue(WorkQueue<CityGMLBase> workQueue) {
		this.workQueue = workQueue;
	}

	@Override
	public void run() {
		try {
			if (firstWork != null && shouldRun) {
				doWork(firstWork);
				firstWork = null;
			}

			while (shouldRun) {
				try {
					CityGMLBase work = workQueue.take();
					doWork(work);
				} catch (InterruptedException ie) {
					// re-check state
				}
			}

			try {
				dbImporterManager.executeBatch();
				batchConn.commit();

				eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, updateCounter));
			} catch (SQLException sqlEx) {
				LOG.error("SQL error: " + sqlEx.getMessage());
			}
			
			try {
				dbImporterManager.close();
			} catch (SQLException sqlEx) {
				LOG.error("SQL error: " + sqlEx.getMessage());
			}
			
			eventDispatcher.triggerEvent(new FeatureCounterEvent(dbImporterManager.getFeatureCounter()));
			eventDispatcher.triggerEvent(new GeometryCounterEvent(dbImporterManager.getGeometryCounter()));
		} finally {
			if (batchConn != null) {
				try {
					batchConn.close();
				} catch (SQLException sqlEx) {
					//
				}

				batchConn = null;
			}

			if (commitConn != null) {
				try {
					commitConn.close();
				} catch (SQLException sqlEx) {
					//
				}

				commitConn = null;
			}
		}
	}

	private void doWork(CityGMLBase work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			try {
				long id = 0;

				if (work.getCityGMLClass() == CityGMLClass.APPEARANCE) {
					// global appearances
					if (!config.getProject().getImporter().getAppearances().isSetImportAppearance())
						return;

					DBAppearance dbAppearance = (DBAppearance)dbImporterManager.getDBImporter(DBImporterEnum.APPEARANCE);
					if (dbAppearance != null)
						id = dbAppearance.insert((Appearance)work, CityGMLClass.CITYMODEL, 0);

				} else if (work.getCityGMLClass().childOrSelf(CityGMLClass.CITYOBJECT)){
					CityObject cityObject = (CityObject)work;

					// gml:id filter
					if (featureGmlIdFilter.isActive()) {
						if (cityObject.isSetId()) {
							if (featureGmlIdFilter.filter(cityObject.getId()))
								return;
						} else
							return;
					}

					// gml:name filter
					if (featureGmlNameFilter.isActive()) {
						if (cityObject.isSetName()) {
							boolean success = false;

							for (Code code : cityObject.getName()) {
								if (code.isSetValue() && !featureGmlNameFilter.filter(code.getValue())) {
									success = true;
									break;
								}
							}

							if (!success)
								return;

						} else
							return;
					}

					// bounding box filter
					// first of all compute bounding box for cityobject since we need it anyways
					if (!cityObject.isSetBoundedBy())
						cityObject.calcBoundedBy();
					else
						// re-work on this
						cityObject.getBoundedBy().convertEnvelope();

					// filter
					if (cityObject.isSetBoundedBy() && 
							featureBoundingBoxFilter.filter(cityObject.getBoundedBy().getEnvelope()))
						return;

					// top-level filter
					if (featureClassFilter.filter(work.getCityGMLClass()))
						return;

					// if the cityobject did pass all filters, let us furhter work on it
					switch (work.getCityGMLClass()) {
					case BUILDING:
						DBBuilding dbBuilding = (DBBuilding)dbImporterManager.getDBImporter(DBImporterEnum.BUILDING);
						if (dbBuilding != null)
							id = dbBuilding.insert((Building)work);

						break;
					case CITYFURNITURE:
						DBCityFurniture dbCityFurniture = (DBCityFurniture)dbImporterManager.getDBImporter(DBImporterEnum.CITY_FURNITURE);
						if (dbCityFurniture != null)
							id = dbCityFurniture.insert((CityFurniture)work);

						break;
					case LANDUSE:
						DBLandUse dbLandUse = (DBLandUse)dbImporterManager.getDBImporter(DBImporterEnum.LAND_USE);
						if (dbLandUse != null)
							id = dbLandUse.insert( (LandUse)work);

						break;
					case WATERBODY:
						DBWaterBody dbWaterBody = (DBWaterBody)dbImporterManager.getDBImporter(DBImporterEnum.WATERBODY);
						if (dbWaterBody != null)
							id = dbWaterBody.insert((WaterBody)work);

						break;
					case PLANTCOVER:
						DBPlantCover dbPlantCover = (DBPlantCover)dbImporterManager.getDBImporter(DBImporterEnum.PLANT_COVER);
						if (dbPlantCover != null)
							id = dbPlantCover.insert((PlantCover)work);

						break;
					case SOLITARYVEGETATIONOBJECT:
						DBSolitaryVegetatObject dbSolVegObject = (DBSolitaryVegetatObject)dbImporterManager.getDBImporter(DBImporterEnum.SOLITARY_VEGETAT_OBJECT);
						if (dbSolVegObject != null)
							id = dbSolVegObject.insert((SolitaryVegetationObject)work);

						break;
					case TRANSPORTATIONCOMPLEX:
					case ROAD:
					case RAILWAY:
					case TRACK:
					case SQUARE:
						DBTransportationComplex dbTransComplex = (DBTransportationComplex)dbImporterManager.getDBImporter(DBImporterEnum.TRANSPORTATION_COMPLEX);
						if (dbTransComplex != null)
							id = dbTransComplex.insert((TransportationComplex)work);

						break;
					case RELIEFFEATURE:
						DBReliefFeature dbReliefFeature = (DBReliefFeature)dbImporterManager.getDBImporter(DBImporterEnum.RELIEF_FEATURE);
						if (dbReliefFeature != null)
							id = dbReliefFeature.insert((ReliefFeature)work);

						break;
					case GENERICCITYOBJECT:
						DBGenericCityObject dbGenericCityObject = (DBGenericCityObject)dbImporterManager.getDBImporter(DBImporterEnum.GENERIC_CITYOBJECT);
						if (dbGenericCityObject != null)
							id = dbGenericCityObject.insert((GenericCityObject)work);

						break;
					case CITYOBJECTGROUP:
						DBCityObjectGroup dbCityObjectGroup = (DBCityObjectGroup)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECTGROUP);
						if (dbCityObjectGroup != null)
							id = dbCityObjectGroup.insert((CityObjectGroup)work);

						break;
					}
				}

				if (id != 0)
					updateCounter++;

			} catch (SQLException sqlEx) {
				AbstractFeature feature = (AbstractFeature)work;

				if (feature.isSetId())
					LOG.error("SQL error for feature with gml:id '" + feature.getId() + "': " + sqlEx.getMessage());
				else
					LOG.error("SQL error: " + sqlEx.getMessage());

				return;
			}

			try {
				if (updateCounter == commitAfter) {
					dbImporterManager.executeBatch();
					batchConn.commit();

					eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, updateCounter));
					updateCounter = 0;
				}
			} catch (SQLException sqlEx) {
				// uh, batch update did not work. this is serious...
				LOG.error("SQL error: " + sqlEx.getMessage());
				return;
			}

		} finally {
			runLock.unlock();
		}
	}
}