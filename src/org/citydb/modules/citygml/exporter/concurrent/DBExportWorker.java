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
package org.citydb.modules.citygml.exporter.concurrent;

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
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.modules.citygml.common.database.cache.CacheTableManager;
import org.citydb.modules.citygml.common.database.uid.UIDCacheManager;
import org.citydb.modules.citygml.common.database.xlink.DBXlink;
import org.citydb.modules.citygml.exporter.database.content.DBAppearance;
import org.citydb.modules.citygml.exporter.database.content.DBBridge;
import org.citydb.modules.citygml.exporter.database.content.DBBuilding;
import org.citydb.modules.citygml.exporter.database.content.DBCityFurniture;
import org.citydb.modules.citygml.exporter.database.content.DBCityObjectGroup;
import org.citydb.modules.citygml.exporter.database.content.DBExporterEnum;
import org.citydb.modules.citygml.exporter.database.content.DBExporterManager;
import org.citydb.modules.citygml.exporter.database.content.DBGenericCityObject;
import org.citydb.modules.citygml.exporter.database.content.DBLandUse;
import org.citydb.modules.citygml.exporter.database.content.DBPlantCover;
import org.citydb.modules.citygml.exporter.database.content.DBReliefFeature;
import org.citydb.modules.citygml.exporter.database.content.DBSolitaryVegetatObject;
import org.citydb.modules.citygml.exporter.database.content.DBSplittingResult;
import org.citydb.modules.citygml.exporter.database.content.DBTransportationComplex;
import org.citydb.modules.citygml.exporter.database.content.DBTunnel;
import org.citydb.modules.citygml.exporter.database.content.DBWaterBody;
import org.citydb.modules.citygml.exporter.util.FeatureProcessException;
import org.citydb.modules.citygml.exporter.util.FeatureProcessor;
import org.citydb.modules.common.event.CounterEvent;
import org.citydb.modules.common.event.CounterType;
import org.citydb.modules.common.event.EventType;
import org.citydb.modules.common.event.FeatureCounterEvent;
import org.citydb.modules.common.event.GeometryCounterEvent;
import org.citydb.modules.common.event.InterruptEvent;
import org.citydb.modules.common.event.InterruptReason;
import org.citydb.modules.common.filter.ExportFilter;
import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.xml.sax.SAXException;

public class DBExportWorker extends Worker<DBSplittingResult> implements EventHandler {
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;
	private volatile boolean shouldWork = true;

	private final DatabaseConnectionPool dbConnectionPool;
	private final JAXBBuilder jaxbBuilder;
	private final FeatureProcessor featureProcessor;
	private final WorkerPool<DBXlink> xlinkExporterPool;
	private final UIDCacheManager uidCacheManager;
	private final CacheTableManager cacheTableManager;
	private final ExportFilter exportFilter;
	private final Config config;
	private Connection connection;	
	private DBExporterManager dbExporterManager;
	private final EventDispatcher eventDispatcher;
	private int exportCounter = 0;

	public DBExportWorker(DatabaseConnectionPool dbConnectionPool,
			JAXBBuilder jaxbBuilder,
			FeatureProcessor featureProcessor,
			WorkerPool<DBXlink> xlinkExporterPool,
			UIDCacheManager uidCacheManager,
			CacheTableManager cacheTableManager,
			ExportFilter exportFilter,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException, SAXException {
		this.dbConnectionPool = dbConnectionPool;
		this.jaxbBuilder = jaxbBuilder;
		this.featureProcessor = featureProcessor;
		this.xlinkExporterPool = xlinkExporterPool;
		this.uidCacheManager = uidCacheManager;
		this.cacheTableManager = cacheTableManager;
		this.exportFilter = exportFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
		init();
	}

	private void init() throws SQLException, SAXException {
		connection = dbConnectionPool.getConnection();

		// try and change workspace the connections if needed
		if (dbConnectionPool.getActiveDatabaseAdapter().hasVersioningSupport()) {
			dbConnectionPool.getActiveDatabaseAdapter().getWorkspaceManager().gotoWorkspace(
					connection, 
					config.getProject().getDatabase().getWorkspaces().getExportWorkspace());
		}

		dbExporterManager = new DBExporterManager( 
				connection,
				dbConnectionPool.getActiveDatabaseAdapter(),
				jaxbBuilder,
				featureProcessor,
				xlinkExporterPool,
				uidCacheManager,
				cacheTableManager,
				exportFilter,
				config,
				eventDispatcher);

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
					DBSplittingResult work = workQueue.take();
					doWork(work);
				} catch (InterruptedException ie) {
					// re-check state
				}
			}

			try {
				dbExporterManager.close();
			} catch (SQLException e) {
				//
			}

			eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, exportCounter, this));
			eventDispatcher.triggerEvent(new FeatureCounterEvent(dbExporterManager.getFeatureCounter(), this));
			eventDispatcher.triggerEvent(new GeometryCounterEvent(dbExporterManager.getGeometryCounter(), this));
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					//
				}

				connection = null;
			}
		}
	}

	private void doWork(DBSplittingResult work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			if (!shouldWork)
				return;
			
			boolean success = false;

			if (work.isCheckIfAlreadyExported())
				if (dbExporterManager.lookupAndPutGmlId(work.getGmlId(), work.getPrimaryKey(), work.getCityObjectType()))
					return;						

			switch (work.getCityObjectType()) {
			case BUILDING:
				DBBuilding dbBuilding = (DBBuilding)dbExporterManager.getDBExporter(DBExporterEnum.BUILDING);
				if (dbBuilding != null)
					success = dbBuilding.read(work);
				break;
			case BRIDGE:
				DBBridge dbBridge = (DBBridge)dbExporterManager.getDBExporter(DBExporterEnum.BRIDGE);
				if (dbBridge != null)
					success = dbBridge.read(work);
				break;
			case TUNNEL:
				DBTunnel dbTunnel = (DBTunnel)dbExporterManager.getDBExporter(DBExporterEnum.TUNNEL);
				if (dbTunnel != null)
					success = dbTunnel.read(work);
				break;
			case CITY_FURNITURE:
				DBCityFurniture dbCityFurniture = (DBCityFurniture)dbExporterManager.getDBExporter(DBExporterEnum.CITY_FURNITURE);
				if (dbCityFurniture != null)
					success = dbCityFurniture.read(work);
				break;
			case LAND_USE:
				DBLandUse dbLandUse = (DBLandUse)dbExporterManager.getDBExporter(DBExporterEnum.LAND_USE);
				if (dbLandUse != null)
					success = dbLandUse.read(work);
				break;
			case WATER_BODY:
				DBWaterBody dbWaterBody = (DBWaterBody)dbExporterManager.getDBExporter(DBExporterEnum.WATERBODY);
				if (dbWaterBody != null)
					success = dbWaterBody.read(work);
				break;
			case PLANT_COVER:
				DBPlantCover dbPlantCover = (DBPlantCover)dbExporterManager.getDBExporter(DBExporterEnum.PLANT_COVER);
				if (dbPlantCover != null)
					success = dbPlantCover.read(work);
				break;
			case SOLITARY_VEGETATION_OBJECT:
				DBSolitaryVegetatObject dbSolVegObject = (DBSolitaryVegetatObject)dbExporterManager.getDBExporter(DBExporterEnum.SOLITARY_VEGETAT_OBJECT);
				if (dbSolVegObject != null)
					success = dbSolVegObject.read(work);
				break;
			case TRANSPORTATION_COMPLEX:
			case TRACK:
			case RAILWAY:
			case ROAD:
			case SQUARE:
				DBTransportationComplex dbTransComplex = (DBTransportationComplex)dbExporterManager.getDBExporter(DBExporterEnum.TRANSPORTATION_COMPLEX);
				if (dbTransComplex != null)
					success = dbTransComplex.read(work);
				break;
			case RELIEF_FEATURE:
				DBReliefFeature dbReliefFeature = (DBReliefFeature)dbExporterManager.getDBExporter(DBExporterEnum.RELIEF_FEATURE);
				if (dbReliefFeature != null)
					success = dbReliefFeature.read(work);
				break;
			case GENERIC_CITY_OBJECT:
				DBGenericCityObject dbGenericCityObject = (DBGenericCityObject)dbExporterManager.getDBExporter(DBExporterEnum.GENERIC_CITYOBJECT);
				if (dbGenericCityObject != null)
					success = dbGenericCityObject.read(work);
				break;
			case CITY_OBJECT_GROUP:
				DBCityObjectGroup dbCityObjectGroup = (DBCityObjectGroup)dbExporterManager.getDBExporter(DBExporterEnum.CITYOBJECTGROUP);
				if (dbCityObjectGroup != null)
					success = dbCityObjectGroup.read(work);
				break;
			case APPEARANCE:
				// we are working on global appearances here
				DBAppearance dbAppearance = (DBAppearance)dbExporterManager.getDBExporter(DBExporterEnum.GLOBAL_APPEARANCE);
				if (dbAppearance != null)
					success = dbAppearance.read(work);
				break;
			default:
				return;
			}

			if (success)
				++exportCounter;

			if (exportCounter == 20) {
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, exportCounter, this));
				exportCounter = 0;
			}

		} catch (SQLException e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent(InterruptReason.SQL_ERROR, "Aborting export due to SQL errors.", LogLevel.WARN, e, eventChannel, this));
		} catch (FeatureProcessException e) {
			eventDispatcher.triggerSyncEvent(new InterruptEvent(InterruptReason.FEATURE_PROCESS_ERROR, "Fatal error while processing CityGML features.", LogLevel.WARN, e, eventChannel, this));
		} catch (Throwable e) {
			// this is to catch general exceptions that may occur during the export
			eventDispatcher.triggerSyncEvent(new InterruptEvent(InterruptReason.UNKNOWN_ERROR, "Aborting due to an unexpected " + e.getClass().getName() + " error.", LogLevel.ERROR, e, eventChannel, this));
		} finally {
			runLock.unlock();
		}
	}

	@Override
	public void handleEvent(Event event) throws Exception {
		if (event.getChannel() == eventChannel)
			shouldWork = false;
	}

}
