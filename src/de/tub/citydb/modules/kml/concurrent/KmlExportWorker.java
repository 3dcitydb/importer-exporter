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
package de.tub.citydb.modules.kml.concurrent;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;

import net.opengis.kml._2.ObjectFactory;

import org.citygml4j.factory.CityGMLFactory;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.util.xml.SAXEventBuffer;

import de.tub.citydb.api.concurrent.Worker;
import de.tub.citydb.api.concurrent.WorkerPool;
import de.tub.citydb.api.concurrent.WorkerPool.WorkQueue;
import de.tub.citydb.api.event.EventDispatcher;
import de.tub.citydb.config.Config;
import de.tub.citydb.config.project.database.Database;
import de.tub.citydb.config.project.exporter.ExportFilterConfig;
import de.tub.citydb.config.project.kmlExporter.ColladaOptions;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.modules.kml.database.Building;
import de.tub.citydb.modules.kml.database.CityObjectGroup;
import de.tub.citydb.modules.kml.database.ColladaBundle;
import de.tub.citydb.modules.kml.database.KmlExporterManager;
import de.tub.citydb.modules.kml.database.KmlGenericObject;
import de.tub.citydb.modules.kml.database.KmlSplittingResult;
import de.tub.citydb.modules.kml.database.SolitaryVegetationObject;

public class KmlExportWorker implements Worker<KmlSplittingResult> {

	// instance members needed for WorkPool
	private volatile boolean shouldRun = true;
	private ReentrantLock runLock = new ReentrantLock();
	private WorkQueue<KmlSplittingResult> workQueue = null;
	private KmlSplittingResult firstWork;
	private Thread workerThread = null;

	// instance members needed to do work
	private final JAXBContext jaxbKmlContext;
	private final JAXBContext jaxbColladaContext;
	private final DatabaseConnectionPool dbConnectionPool;
	private final WorkerPool<SAXEventBuffer> ioWriterPool;
	private final ObjectFactory kmlFactory; 
	private final CityGMLFactory cityGMLFactory; 
	private final ConcurrentLinkedQueue<ColladaBundle> buildingQueue;
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private Connection connection;
	private ExportFilterConfig filterConfig;
	private KmlExporterManager kmlExporterManager;

	private KmlGenericObject singleObject = null;

	private EnumMap<CityGMLClass, Integer>objectGroupCounter = new EnumMap<CityGMLClass, Integer>(CityGMLClass.class);
	private EnumMap<CityGMLClass, Integer>objectGroupSize = new EnumMap<CityGMLClass, Integer>(CityGMLClass.class);
	private EnumMap<CityGMLClass, KmlGenericObject>objectGroups = new EnumMap<CityGMLClass, KmlGenericObject>(CityGMLClass.class);

	public KmlExportWorker(JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			DatabaseConnectionPool dbConnectionPool,
			WorkerPool<SAXEventBuffer> ioWriterPool,
			ObjectFactory kmlFactory,
			CityGMLFactory cityGMLFactory,
			ConcurrentLinkedQueue<ColladaBundle> buildingQueue,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.jaxbKmlContext = jaxbKmlContext;
		this.jaxbColladaContext = jaxbColladaContext;
		this.dbConnectionPool = dbConnectionPool;
		this.ioWriterPool = ioWriterPool;
		this.kmlFactory = kmlFactory;
		this.cityGMLFactory = cityGMLFactory;
		this.buildingQueue = buildingQueue;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(false);
		// try and change workspace for both connections if needed
		Database database = config.getProject().getDatabase();
		dbConnectionPool.gotoWorkspace(connection, 
				database.getWorkspaces().getKmlExportWorkspace());

		
		filterConfig = config.getProject().getKmlExporter().getFilter();
		ColladaOptions colladaOptions = null; 
		if (filterConfig.getComplexFilter().getFeatureClass().isSetBuilding()) {
			objectGroupCounter.put(CityGMLClass.BUILDING, 0);
			colladaOptions = config.getProject().getKmlExporter().getBuildingColladaOptions();
			objectGroupSize.put(CityGMLClass.BUILDING, colladaOptions.isGroupObjects() ? 
													   colladaOptions.getGroupSize(): 1);
			objectGroups.put(CityGMLClass.BUILDING, null);
		}
		if (filterConfig.getComplexFilter().getFeatureClass().isSetVegetation()) {
			objectGroupCounter.put(CityGMLClass.SOLITARY_VEGETATION_OBJECT, 0);
			colladaOptions = config.getProject().getKmlExporter().getVegetationColladaOptions();
			objectGroupSize.put(CityGMLClass.SOLITARY_VEGETATION_OBJECT, colladaOptions.isGroupObjects() ? 
													   					 colladaOptions.getGroupSize(): 1);
			objectGroups.put(CityGMLClass.SOLITARY_VEGETATION_OBJECT, null);
		}
		// CityGMLClass.CITY_OBJECT_GROUP is left out, it does not make sense to group it without COLLADA DisplayForm 

		kmlExporterManager = new KmlExporterManager(jaxbKmlContext,
				jaxbColladaContext,
				ioWriterPool,
				kmlFactory,
				buildingQueue,
				config);
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
	public void setFirstWork(KmlSplittingResult firstWork) {
		this.firstWork = firstWork;
	}

	@Override
	public void setThread(Thread workerThread) {
		this.workerThread = workerThread;
	}

	@Override
	public void setWorkQueue(WorkQueue<KmlSplittingResult> workQueue) {
		this.workQueue = workQueue;
	}

	@Override
	public void run() {
		try {
			if (firstWork != null && shouldRun) {
				doWork(firstWork);
				firstWork = null;
			}

			KmlSplittingResult work = null; 
			while (shouldRun) {
				try {
					work = workQueue.take();
					doWork(work);
				}
				catch (InterruptedException ie) {
					// re-check state
				}
			}

			// last objectGroups may be not empty but not big enough
/*
			if (work != null && !work.isCityObjectGroup() &&
				work.getDisplayForm().getForm() == DisplayForm.COLLADA &&
				singleObject.getId() != null && // object is filled
				objectGroupCounter.get(work.getCityObjectType()) != 0) {  // group is not empty
*/
			for (CityGMLClass cityObjectType: objectGroups.keySet()) {
				if (objectGroupCounter.get(cityObjectType) != 0) {  // group is not empty
					KmlGenericObject objectGroup = objectGroups.get(cityObjectType);
					if (objectGroup == null || objectGroup.getId() == null) continue;
					try {
						ColladaBundle colladaBundle = new ColladaBundle();
						colladaBundle.setCollada(objectGroup.generateColladaTree());
						colladaBundle.setTexImages(objectGroup.getTexImages());
						colladaBundle.setTexOrdImages(objectGroup.getTexOrdImages());
						colladaBundle.setPlacemark(singleObject.createPlacemarkFromGenericObject(objectGroup, work));
						colladaBundle.setBuildingId(objectGroup.getId());
						kmlExporterManager.print(colladaBundle, config.getProject().getKmlExporter().getBuildingBalloon().isBalloonContentInSeparateFile());
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
/*
			objectGroup = null;
			objectGroupCounter = null;
*/
		}
		finally {
			if (connection != null) {
				try {
					connection.commit(); // for all possible GE_LoDn_zOffset values
					connection.close();
				}
				catch (SQLException e) {}

				connection = null;
			}
		}
	}

	private void doWork(KmlSplittingResult work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		try {
			
			switch (work.getCityObjectType()) {
			case BUILDING:
				singleObject = new Building(connection,
											kmlExporterManager,
											cityGMLFactory,
											kmlFactory,
											eventDispatcher,
											dbConnectionPool.getActiveConnectionMetaData().getReferenceSystem(),
											config);
				break;

			case CITY_OBJECT_GROUP:
				singleObject = new CityObjectGroup(connection,
												   kmlExporterManager,
												   cityGMLFactory,
												   kmlFactory,
												   eventDispatcher,
												   dbConnectionPool.getActiveConnectionMetaData().getReferenceSystem(),
												   config);
				break;

			case SOLITARY_VEGETATION_OBJECT:
				singleObject = new SolitaryVegetationObject(connection,
												   			kmlExporterManager,
												   			cityGMLFactory,
												   			kmlFactory,
												   			eventDispatcher,
												   			dbConnectionPool.getActiveConnectionMetaData().getReferenceSystem(),
												   			config);
				break;
			}

			singleObject.read(work);
			
			if (!work.isCityObjectGroup() && 
				work.getDisplayForm().getForm() == DisplayForm.COLLADA &&
				singleObject.getId() != null) { // object is filled

				KmlGenericObject objectGroup = objectGroups.get(work.getCityObjectType());
				if (objectGroup == null) {
					objectGroup = singleObject; 
				}
				else {
					objectGroup.appendObject(singleObject);
				}

				objectGroupCounter.put(work.getCityObjectType(), objectGroupCounter.get(work.getCityObjectType()).intValue() + 1);
				if (objectGroupCounter.get(work.getCityObjectType()).intValue() == objectGroupSize.get(work.getCityObjectType()).intValue()) {
					try {
						ColladaBundle colladaBundle = new ColladaBundle();
						colladaBundle.setCollada(objectGroup.generateColladaTree());
						colladaBundle.setTexImages(objectGroup.getTexImages());
						colladaBundle.setTexOrdImages(objectGroup.getTexOrdImages());
						colladaBundle.setPlacemark(singleObject.createPlacemarkFromGenericObject(objectGroup, work));
						colladaBundle.setBuildingId(objectGroup.getId());
						kmlExporterManager.print(colladaBundle, config.getProject().getKmlExporter().getBuildingBalloon().isBalloonContentInSeparateFile());
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					objectGroup = null;
					objectGroupCounter.put(work.getCityObjectType(), 0);
				}
			}
		}
		finally {
			runLock.unlock();
		}
	}

}
