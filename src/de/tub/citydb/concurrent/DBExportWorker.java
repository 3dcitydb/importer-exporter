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
package de.tub.citydb.concurrent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import oracle.jdbc.driver.OracleConnection;

import org.citygml4j.factory.CityGMLFactory;

import de.tub.citydb.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.db.DBConnectionPool;
import de.tub.citydb.db.exporter.DBAppearance;
import de.tub.citydb.db.exporter.DBBuilding;
import de.tub.citydb.db.exporter.DBCityFurniture;
import de.tub.citydb.db.exporter.DBCityObjectGroup;
import de.tub.citydb.db.exporter.DBExporterEnum;
import de.tub.citydb.db.exporter.DBExporterManager;
import de.tub.citydb.db.exporter.DBGenericCityObject;
import de.tub.citydb.db.exporter.DBLandUse;
import de.tub.citydb.db.exporter.DBPlantCover;
import de.tub.citydb.db.exporter.DBReliefFeature;
import de.tub.citydb.db.exporter.DBSolitaryVegetatObject;
import de.tub.citydb.db.exporter.DBSplittingResult;
import de.tub.citydb.db.exporter.DBTransportationComplex;
import de.tub.citydb.db.exporter.DBWaterBody;
import de.tub.citydb.db.gmlId.DBGmlIdLookupServerManager;
import de.tub.citydb.db.xlink.DBXlink;
import de.tub.citydb.event.EventDispatcher;
import de.tub.citydb.event.statistic.CounterEvent;
import de.tub.citydb.event.statistic.CounterType;
import de.tub.citydb.event.statistic.FeatureCounterEvent;
import de.tub.citydb.event.statistic.GeometryCounterEvent;
import de.tub.citydb.filter.ExportFilter;
import de.tub.citydb.log.Logger;
import de.tub.citydb.sax.SAXBuffer;

public class DBExportWorker implements Worker<DBSplittingResult> {
	private final Logger LOG = Logger.getInstance();

	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<DBSplittingResult> workQueue = null;
	private DBSplittingResult firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final JAXBContext jaxbContext;
	private final DBConnectionPool dbConnectionPool;
	private final WorkerPool<SAXBuffer> ioWriterPool;
	private final WorkerPool<DBXlink> xlinkExporterPool;
	private final DBGmlIdLookupServerManager lookupServerManager;
	private final CityGMLFactory cityGMLFactory;
	private final ExportFilter exportFilter;
	private final Config config;
	private Connection connection;	
	private DBExporterManager dbExporterManager;
	private final EventDispatcher eventDispatcher;
	private int exportCounter = 0;

	public DBExportWorker(JAXBContext jaxbContext,
			DBConnectionPool dbConnectionPool,
			WorkerPool<SAXBuffer> ioWriterPool,
			WorkerPool<DBXlink> xlinkExporterPool,
			DBGmlIdLookupServerManager lookupServerManager,
			CityGMLFactory cityGMLFactory,
			ExportFilter exportFilter,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.jaxbContext = jaxbContext;
		this.dbConnectionPool = dbConnectionPool;
		this.ioWriterPool = ioWriterPool;
		this.xlinkExporterPool = xlinkExporterPool;
		this.lookupServerManager = lookupServerManager;
		this.cityGMLFactory = cityGMLFactory;
		this.exportFilter = exportFilter;
		this.config = config;
		this.eventDispatcher = eventDispatcher;
		init();
	}

	private void init() throws SQLException {
		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(false);
		((OracleConnection)connection).setDefaultRowPrefetch(50);

		// try and change workspace for both connections if needed
		Database database = config.getProject().getDatabase();
		dbConnectionPool.changeWorkspace(
				connection, 
				database.getWorkspaces().getExportWorkspace());

		dbExporterManager = new DBExporterManager(
				jaxbContext,
				connection,
				ioWriterPool,
				xlinkExporterPool,
				lookupServerManager,
				cityGMLFactory,
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
			
			eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, exportCounter));
			eventDispatcher.triggerEvent(new FeatureCounterEvent(dbExporterManager.getFeatureCounter()));
			eventDispatcher.triggerEvent(new GeometryCounterEvent(dbExporterManager.getGeometryCounter()));
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
				case CITYFURNITURE:
					DBCityFurniture dbCityFurniture = (DBCityFurniture)dbExporterManager.getDBExporter(DBExporterEnum.CITY_FURNITURE);
					if (dbCityFurniture != null)
						success = dbCityFurniture.read(work);
					break;
				case LANDUSE:
					DBLandUse dbLandUse = (DBLandUse)dbExporterManager.getDBExporter(DBExporterEnum.LAND_USE);
					if (dbLandUse != null)
						success = dbLandUse.read(work);
					break;
				case WATERBODY:
					DBWaterBody dbWaterBody = (DBWaterBody)dbExporterManager.getDBExporter(DBExporterEnum.WATERBODY);
					if (dbWaterBody != null)
						success = dbWaterBody.read(work);
					break;
				case PLANTCOVER:
					DBPlantCover dbPlantCover = (DBPlantCover)dbExporterManager.getDBExporter(DBExporterEnum.PLANT_COVER);
					if (dbPlantCover != null)
						success = dbPlantCover.read(work);
					break;
				case SOLITARYVEGETATIONOBJECT:
					DBSolitaryVegetatObject dbSolVegObject = (DBSolitaryVegetatObject)dbExporterManager.getDBExporter(DBExporterEnum.SOLITARY_VEGETAT_OBJECT);
					if (dbSolVegObject != null)
						success = dbSolVegObject.read(work);
					break;
				case TRANSPORTATIONCOMPLEX:
				case TRACK:
				case RAILWAY:
				case ROAD:
				case SQUARE:
					DBTransportationComplex dbTransComplex = (DBTransportationComplex)dbExporterManager.getDBExporter(DBExporterEnum.TRANSPORTATION_COMPLEX);
					if (dbTransComplex != null)
						success = dbTransComplex.read(work);
					break;
				case RELIEFFEATURE:
					DBReliefFeature dbReliefFeature = (DBReliefFeature)dbExporterManager.getDBExporter(DBExporterEnum.RELIEF_FEATURE);
					if (dbReliefFeature != null)
						success = dbReliefFeature.read(work);
					break;
				case APPEARANCE:
					// we are working on global appearances here
					DBAppearance dbAppearance = (DBAppearance)dbExporterManager.getDBExporter(DBExporterEnum.APPEARANCE);
					if (dbAppearance != null)
						success = dbAppearance.read(work);
					break;
				case GENERICCITYOBJECT:
					DBGenericCityObject dbGenericCityObject = (DBGenericCityObject)dbExporterManager.getDBExporter(DBExporterEnum.GENERIC_CITYOBJECT);
					if (dbGenericCityObject != null)
						success = dbGenericCityObject.read(work);
					break;
				case CITYOBJECTGROUP:
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
			} catch (JAXBException jaxbEx) {
				return;
			}

			if (exportCounter == 20) {
				eventDispatcher.triggerEvent(new CounterEvent(CounterType.TOPLEVEL_FEATURE, exportCounter));
				exportCounter = 0;
			}

		} finally {
			runLock.unlock();
		}
	}

}
