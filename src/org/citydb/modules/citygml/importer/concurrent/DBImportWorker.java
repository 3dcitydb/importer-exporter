/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.modules.citygml.importer.concurrent;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.citydb.api.concurrent.Worker;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.event.Event;
import org.citydb.api.event.EventDispatcher;
import org.citydb.api.event.EventHandler;
import org.citydb.api.log.LogLevel;
import org.citydb.config.Config;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.database.Workspace;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.importer.database.content.DBAppearance;
import org.citydb.modules.citygml.importer.database.content.DBBridge;
import org.citydb.modules.citygml.importer.database.content.DBBuilding;
import org.citydb.modules.citygml.importer.database.content.DBCityFurniture;
import org.citydb.modules.citygml.importer.database.content.DBCityObjectGroup;
import org.citydb.modules.citygml.importer.database.content.DBGenericCityObject;
import org.citydb.modules.citygml.importer.database.content.DBImporterEnum;
import org.citydb.modules.citygml.importer.database.content.DBImporterManager;
import org.citydb.modules.citygml.importer.database.content.DBLandUse;
import org.citydb.modules.citygml.importer.database.content.DBPlantCover;
import org.citydb.modules.citygml.importer.database.content.DBReliefFeature;
import org.citydb.modules.citygml.importer.database.content.DBSolitaryVegetatObject;
import org.citydb.modules.citygml.importer.database.content.DBTransportationComplex;
import org.citydb.modules.citygml.importer.database.content.DBTunnel;
import org.citydb.modules.citygml.importer.database.content.DBWaterBody;
import org.citydb.modules.citygml.importer.util.ImportLogger;
import org.citydb.modules.citygml.importer.util.ImportLogger.ImportLogEntry;
import org.citydb.modules.common.event.CounterEvent;
import org.citydb.modules.common.event.CounterType;
import org.citydb.modules.common.event.EventType;
import org.citydb.modules.common.event.FeatureCounterEvent;
import org.citydb.modules.common.event.GeometryCounterEvent;
import org.citydb.modules.common.event.InterruptEvent;
import org.citydb.modules.common.event.InterruptReason;
import org.citydb.modules.common.filter.ImportFilter;
import org.citydb.modules.common.filter.feature.BoundingBoxFilter;
import org.citydb.modules.common.filter.feature.GmlIdFilter;
import org.citydb.modules.common.filter.feature.GmlNameFilter;
import org.citydb.util.Util;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.model.citygml.CityGML;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.citygml.appearance.Appearance;
import org.citygml4j.model.citygml.bridge.Bridge;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.core.AbstractCityObject;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.citygml.tunnel.Tunnel;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.common.base.ModelType;
import org.citygml4j.model.gml.basicTypes.Code;
import org.citygml4j.model.gml.geometry.primitives.Envelope;

public class DBImportWorker extends Worker<CityGML> implements EventHandler {
	private final Logger LOG = Logger.getInstance();
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;

	private final DatabaseConnectionPool dbConnectionPool;
	private final JAXBBuilder jaxbBuilder;
	private final WorkerPool<DBXlink> tmpXlinkPool;
	private final UIDCacheManager uidCacheManager;
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private final ImportFilter importFilter;
	private final ImportLogger importLogger;

	private Connection batchConn;
	private DBImporterManager dbImporterManager;
	private int updateCounter = 0;
	private int commitAfter = 20;

	// filter
	private BoundingBoxFilter featureBoundingBoxFilter;
	private GmlIdFilter featureGmlIdFilter;
	private GmlNameFilter featureGmlNameFilter;

	public DBImportWorker(DatabaseConnectionPool dbConnectionPool,
			JAXBBuilder jaxbBuilder,
			WorkerPool<DBXlink> tmpXlinkPool,
			UIDCacheManager uidCacheManager,
			ImportFilter importFilter,
			ImportLogger importLogger,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.dbConnectionPool = dbConnectionPool;
		this.jaxbBuilder = jaxbBuilder;
		this.tmpXlinkPool = tmpXlinkPool;
		this.uidCacheManager = uidCacheManager;
		this.importFilter = importFilter;
		this.importLogger = importLogger;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		init();
	}

	private void init() throws SQLException {
		batchConn = dbConnectionPool.getConnection();
		batchConn.setAutoCommit(false);

		Database database = config.getProject().getDatabase();

		// try and change workspace for both connections if needed
		if (dbConnectionPool.getActiveDatabaseAdapter().hasVersioningSupport()) {
			Workspace workspace = database.getWorkspaces().getImportWorkspace();
			dbConnectionPool.getActiveDatabaseAdapter().getWorkspaceManager().gotoWorkspace(batchConn, workspace);
		}

		// init filter 
		featureBoundingBoxFilter = importFilter.getBoundingBoxFilter();
		featureGmlIdFilter = importFilter.getGmlIdFilter();
		featureGmlNameFilter = importFilter.getGmlNameFilter();		

		dbImporterManager = new DBImporterManager(
				batchConn,
				dbConnectionPool.getActiveDatabaseAdapter(),
				jaxbBuilder,
				config,
				tmpXlinkPool,
				uidCacheManager,
				eventDispatcher);

		Integer commitAfterProp = database.getUpdateBatching().getFeatureBatchValue();
		if (commitAfterProp != null && commitAfterProp > 0)
			commitAfter = commitAfterProp;

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
					dbImporterManager.executeBatch();
					batchConn.commit();
					updateImportContext();
				}
			} catch (SQLException e) {
				try {
					batchConn.rollback();
				} catch (SQLException sql) {
					//
				}

				eventDispatcher.triggerEvent(new InterruptEvent(InterruptReason.SQL_ERROR, "Aborting import due to SQL errors.", LogLevel.WARN, e, eventChannel, this));
			} catch (IOException e) {
				eventDispatcher.triggerEvent(new InterruptEvent(InterruptReason.IMPORT_LOG_ERROR, "Aborting import due I/O errors.", LogLevel.WARN, e, eventChannel, this));
			}

		} finally {
			try {
				dbImporterManager.close();
			} catch (SQLException e) {
				// 
			}

			try {
				batchConn.close();
			} catch (SQLException e) {
				//
			}

			batchConn = null;
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

			if (work.getCityGMLClass() == CityGMLClass.APPEARANCE) {
				// global appearances
				DBAppearance dbAppearance = (DBAppearance)dbImporterManager.getDBImporter(DBImporterEnum.APPEARANCE);
				if (dbAppearance != null)
					id = dbAppearance.insert((Appearance)work, CityGMLClass.CITY_MODEL, 0);

			} else if (work.getModelType() == ModelType.CITYGML) {
				AbstractCityObject cityObject = (AbstractCityObject)work;

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
				if (!cityObject.isSetBoundedBy() || !cityObject.getBoundedBy().isSetEnvelope())
					cityObject.calcBoundedBy(true);
				else if (!cityObject.getBoundedBy().getEnvelope().isSetLowerCorner() ||
						!cityObject.getBoundedBy().getEnvelope().isSetUpperCorner()){
					Envelope envelope = cityObject.getBoundedBy().getEnvelope().convert3d();
					if (envelope != null)
						cityObject.getBoundedBy().setEnvelope(envelope);
					else
						cityObject.calcBoundedBy(true);
				}

				// filter
				if (cityObject.isSetBoundedBy() && 
						featureBoundingBoxFilter.filter(cityObject.getBoundedBy().getEnvelope()))
					return;

				// if the cityobject did pass all filters, let us further work on it
				switch (work.getCityGMLClass()) {
				case BUILDING:
					DBBuilding dbBuilding = (DBBuilding)dbImporterManager.getDBImporter(DBImporterEnum.BUILDING);
					if (dbBuilding != null)
						id = dbBuilding.insert((Building)work);

					break;
				case BRIDGE:
					DBBridge dbBridge = (DBBridge)dbImporterManager.getDBImporter(DBImporterEnum.BRIDGE);
					if (dbBridge != null)
						id = dbBridge.insert((Bridge)work);

					break;
				case TUNNEL:
					DBTunnel dbTunnel = (DBTunnel)dbImporterManager.getDBImporter(DBImporterEnum.TUNNEL);
					if (dbTunnel != null)
						id = dbTunnel.insert((Tunnel)work);

					break;
				case CITY_FURNITURE:
					DBCityFurniture dbCityFurniture = (DBCityFurniture)dbImporterManager.getDBImporter(DBImporterEnum.CITY_FURNITURE);
					if (dbCityFurniture != null)
						id = dbCityFurniture.insert((CityFurniture)work);

					break;
				case LAND_USE:
					DBLandUse dbLandUse = (DBLandUse)dbImporterManager.getDBImporter(DBImporterEnum.LAND_USE);
					if (dbLandUse != null)
						id = dbLandUse.insert((LandUse)work);

					break;
				case WATER_BODY:
					DBWaterBody dbWaterBody = (DBWaterBody)dbImporterManager.getDBImporter(DBImporterEnum.WATERBODY);
					if (dbWaterBody != null)
						id = dbWaterBody.insert((WaterBody)work);

					break;
				case PLANT_COVER:
					DBPlantCover dbPlantCover = (DBPlantCover)dbImporterManager.getDBImporter(DBImporterEnum.PLANT_COVER);
					if (dbPlantCover != null)
						id = dbPlantCover.insert((PlantCover)work);

					break;
				case SOLITARY_VEGETATION_OBJECT:
					DBSolitaryVegetatObject dbSolVegObject = (DBSolitaryVegetatObject)dbImporterManager.getDBImporter(DBImporterEnum.SOLITARY_VEGETAT_OBJECT);
					if (dbSolVegObject != null)
						id = dbSolVegObject.insert((SolitaryVegetationObject)work);

					break;
				case TRANSPORTATION_COMPLEX:
				case ROAD:
				case RAILWAY:
				case TRACK:
				case SQUARE:
					DBTransportationComplex dbTransComplex = (DBTransportationComplex)dbImporterManager.getDBImporter(DBImporterEnum.TRANSPORTATION_COMPLEX);
					if (dbTransComplex != null)
						id = dbTransComplex.insert((TransportationComplex)work);

					break;
				case RELIEF_FEATURE:
					DBReliefFeature dbReliefFeature = (DBReliefFeature)dbImporterManager.getDBImporter(DBImporterEnum.RELIEF_FEATURE);
					if (dbReliefFeature != null)
						id = dbReliefFeature.insert((ReliefFeature)work);

					break;
				case GENERIC_CITY_OBJECT:
					DBGenericCityObject dbGenericCityObject = (DBGenericCityObject)dbImporterManager.getDBImporter(DBImporterEnum.GENERIC_CITYOBJECT);
					if (dbGenericCityObject != null)
						id = dbGenericCityObject.insert((GenericCityObject)work);

					break;
				case CITY_OBJECT_GROUP:
					DBCityObjectGroup dbCityObjectGroup = (DBCityObjectGroup)dbImporterManager.getDBImporter(DBImporterEnum.CITYOBJECTGROUP);
					if (dbCityObjectGroup != null)
						id = dbCityObjectGroup.insert((CityObjectGroup)work);

					break;
				default:
					StringBuilder msg = new StringBuilder(Util.getFeatureSignature(
							cityObject.getCityGMLClass(), 
							cityObject.getId()));
					LOG.error(msg.append(": Skipping import since this is not a top-level feature type.").toString());
					return;
				}
			}

			if (id != 0)
				updateCounter++;

			if (updateCounter == commitAfter) {
				dbImporterManager.executeBatch();
				batchConn.commit();
				updateImportContext();
			}

		} catch (SQLException e) {
			try {
				batchConn.rollback();
			} catch (SQLException sql) {
				//
			}

			eventDispatcher.triggerSyncEvent(new InterruptEvent(InterruptReason.SQL_ERROR, "Aborting import due to SQL errors.", LogLevel.WARN, e, eventChannel, this));
		} catch (IOException e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent(InterruptReason.IMPORT_LOG_ERROR, "Aborting import due I/O errors.", LogLevel.WARN, e, eventChannel, this));
		} catch (Throwable e) {
			// this is to catch general exceptions that may occur during the import
			eventDispatcher.triggerSyncEvent(new InterruptEvent(InterruptReason.UNKNOWN_ERROR, "Aborting due to an unexpected " + e.getClass().getName() + " error.", LogLevel.ERROR, e, eventChannel, this));
		} finally {
			runLock.unlock();
		}
	}

	private void updateImportContext() throws IOException {
		eventDispatcher.triggerEvent(new FeatureCounterEvent(dbImporterManager.getAndResetFeatureCounter(), this));
		eventDispatcher.triggerEvent(new GeometryCounterEvent(dbImporterManager.getAndResetGeometryCounter(), this));
		eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, updateCounter, this));
		updateCounter = 0;

		// log imported top-level features
		if (importLogger != null) {
			for (ImportLogEntry entry : dbImporterManager.getAndResetImportedFeatures())
				importLogger.write(entry);
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel)
			shouldWork = false;
	}

}
