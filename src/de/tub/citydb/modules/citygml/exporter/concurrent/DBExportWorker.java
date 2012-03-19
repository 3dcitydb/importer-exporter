/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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
package de.tub.citydb.modules.citygml.exporter.concurrent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import org.citygml4j.builder.jaxb.JAXBBuilder;
import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.modules.citygml.common.database.cache.CacheManager;
import de.tub.citydb.modules.citygml.common.database.gmlid.DBGmlIdLookupServerManager;
import de.tub.citydb.modules.citygml.common.database.xlink.DBXlink;
import de.tub.citydb.modules.citygml.exporter.database.content.DBBuilding;
import de.tub.citydb.modules.citygml.exporter.database.content.DBCityFurniture;
import de.tub.citydb.modules.citygml.exporter.database.content.DBCityObjectGroup;
import de.tub.citydb.modules.citygml.exporter.database.content.DBExporterEnum;
import de.tub.citydb.modules.citygml.exporter.database.content.DBExporterManager;
import de.tub.citydb.modules.citygml.exporter.database.content.DBGenericCityObject;
import de.tub.citydb.modules.citygml.exporter.database.content.DBGlobalAppearance;
import de.tub.citydb.modules.citygml.exporter.database.content.DBLandUse;
import de.tub.citydb.modules.citygml.exporter.database.content.DBPlantCover;
import de.tub.citydb.modules.citygml.exporter.database.content.DBReliefFeature;
import de.tub.citydb.modules.citygml.exporter.database.content.DBSolitaryVegetatObject;
import de.tub.citydb.modules.citygml.exporter.database.content.DBSplittingResult;
import de.tub.citydb.modules.citygml.exporter.database.content.DBTransportationComplex;
import de.tub.citydb.modules.citygml.exporter.database.content.DBWaterBody;
import de.tub.citydb.modules.common.event.CounterEvent;
import de.tub.citydb.modules.common.event.CounterType;
import de.tub.citydb.modules.common.event.FeatureCounterEvent;
import de.tub.citydb.modules.common.event.GeometryCounterEvent;
import de.tub.citydb.modules.common.filter.ExportFilter;

public class DBExportWorker implements Worker<DBSplittingResult> {
	private final Logger LOG = Logger.getInstance();

	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<DBSplittingResult> workQueue = null;
	private DBSplittingResult firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final DatabaseConnectionPool dbConnectionPool;
	private final JAXBBuilder jaxbBuilder;
	private final WorkerPool<SAXEventBuffer> ioWriterPool;
	private final WorkerPool<DBXlink> xlinkExporterPool;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final CacheManager cacheManager;
	private final ExportFilter exportFilter;
	private final Config config;
	private Connection connection;	
	private DBExporterManager dbExporterManager;
	private final EventDispatcher eventDispatcher;
	private int exportCounter = 0;

	public DBExportWorker(DatabaseConnectionPool dbConnectionPool,
			JAXBBuilder jaxbBuilder,
			WorkerPool<SAXEventBuffer> ioWriterPool,
			WorkerPool<DBXlink> xlinkExporterPool,
			DBGmlIdLookupServerManager lookupServerManager,
			CacheManager cacheManager,
			ExportFilter exportFilter,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.dbConnectionPool = dbConnectionPool;
		this.jaxbBuilder = jaxbBuilder;
		this.ioWriterPool = ioWriterPool;
		this.xlinkExporterPool = xlinkExporterPool;
		this.lookupServerManager = lookupServerManager;
		this.cacheManager = cacheManager;
		this.exportFilter = exportFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
		init();
	}

	private void init() throws SQLException {
		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(false);

		// try and change workspace for both connections if needed
		Database database = config.getProject().getDatabase();
		dbConnectionPool.gotoWorkspace(
				connection, 
				database.getWorkspaces().getExportWorkspace());

		dbExporterManager = new DBExporterManager( 
				connection,
				jaxbBuilder,
				ioWriterPool,
				xlinkExporterPool,
				lookupServerManager,
				cacheManager,
				exportFilter,
				config,
				eventDispatcher);
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
	public void setFirstWork(DBSplittingResult firstWork) {
		this.firstWork = firstWork;
	}

	@Override
	public void setThread(Thread workerThread) {
		this.workerThread = workerThread;
	}

	@Override
	public void setWorkQueue(WorkQueue<DBSplittingResult> workQueue) {
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
			try {
				boolean success = false;

				if (work.isCheckIfAlreadyExported())
					if (dbExporterManager.getGmlId(work.getPrimaryKey(), work.getCityObjectType()) != null)
						return;						

				switch (work.getCityObjectType()) {
				case BUILDING:
					DBBuilding dbBuilding = (DBBuilding)dbExporterManager.getDBExporter(DBExporterEnum.BUILDING);
					if (dbBuilding != null)
						success = dbBuilding.read(work);
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
				case APPEARANCE:
					// we are working on global appearances here
					DBGlobalAppearance dbAppearance = (DBGlobalAppearance)dbExporterManager.getDBExporter(DBExporterEnum.GLOBAL_APPEARANCE);
					if (dbAppearance != null)
						success = dbAppearance.read(work);
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
				}

				if (success)
					++exportCounter;

			} catch (SQLException sqlEx) {
				LOG.error("SQL error while querying city object: " + sqlEx.getMessage());
				return;
			} catch (CityGMLWriteException e) {
				LOG.error("Fatal error while writing CityGML document: " + e.getCause().getMessage());
				return;
			}

			if (exportCounter == 20) {
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, exportCounter, this));
				exportCounter = 0;
			}

		} finally {
			runLock.unlock();
		}
	}

}
