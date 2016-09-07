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
package org.citydb.modules.kml.concurrent;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;

import org.citydb.api.concurrent.Worker;
import org.citydb.api.concurrent.WorkerPool;
import org.citydb.api.database.BalloonTemplateHandler;
import org.citydb.api.event.EventDispatcher;
import org.citydb.config.Config;
import org.citydb.config.project.database.Database;
import org.citydb.config.project.exporter.ExportFilterConfig;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.BalloonContentMode;
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.database.DatabaseConnectionPool;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.database.adapter.BlobType;
import org.citydb.modules.common.event.FeatureCounterEvent;
import org.citydb.modules.kml.database.Bridge;
import org.citydb.modules.kml.database.Building;
import org.citydb.modules.kml.database.CityFurniture;
import org.citydb.modules.kml.database.CityObjectGroup;
import org.citydb.modules.kml.database.ColladaBundle;
import org.citydb.modules.kml.database.ElevationServiceHandler;
import org.citydb.modules.kml.database.GenericCityObject;
import org.citydb.modules.kml.database.KmlExporterManager;
import org.citydb.modules.kml.database.KmlGenericObject;
import org.citydb.modules.kml.database.KmlSplittingResult;
import org.citydb.modules.kml.database.LandUse;
import org.citydb.modules.kml.database.PlantCover;
import org.citydb.modules.kml.database.Relief;
import org.citydb.modules.kml.database.SolitaryVegetationObject;
import org.citydb.modules.kml.database.Transportation;
import org.citydb.modules.kml.database.Tunnel;
import org.citydb.modules.kml.database.WaterBody;
import org.citydb.modules.kml.util.ExportTracker;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.util.xml.SAXEventBuffer;

import net.opengis.kml._2.ObjectFactory;

public class KmlExportWorker extends Worker<KmlSplittingResult> {
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;

	private AbstractDatabaseAdapter databaseAdapter;
	private BlobExportAdapter textureExportAdapter;
	private final ObjectFactory kmlFactory; 
	private final Config config;
	private final EventDispatcher eventDispatcher;

	private Connection connection;
	private ExportFilterConfig filterConfig;
	private KmlExporterManager kmlExporterManager;
	private KmlGenericObject singleObject = null;

	private EnumMap<CityGMLClass, Integer>objectGroupCounter = new EnumMap<CityGMLClass, Integer>(CityGMLClass.class);
	private EnumMap<CityGMLClass, Integer>objectGroupSize = new EnumMap<CityGMLClass, Integer>(CityGMLClass.class);
	private EnumMap<CityGMLClass, KmlGenericObject>objectGroup = new EnumMap<CityGMLClass, KmlGenericObject>(CityGMLClass.class);
	private EnumMap<CityGMLClass, BalloonTemplateHandler>balloonTemplateHandler = new EnumMap<CityGMLClass, BalloonTemplateHandler>(CityGMLClass.class);

	private ElevationServiceHandler elevationServiceHandler;

	public KmlExportWorker(JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			DatabaseConnectionPool dbConnectionPool,
			WorkerPool<SAXEventBuffer> ioWriterPool,
			ExportTracker tracker,
			ObjectFactory kmlFactory,
			Config config,
			EventDispatcher eventDispatcher) throws SQLException {
		this.kmlFactory = kmlFactory;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		connection = dbConnectionPool.getConnection();
		connection.setAutoCommit(false);
		// try and change workspace if needed
		if (dbConnectionPool.getActiveDatabaseAdapter().hasVersioningSupport()) {
			Database database = config.getProject().getDatabase();
			dbConnectionPool.getActiveDatabaseAdapter().getWorkspaceManager().gotoWorkspace(connection, 
					database.getWorkspaces().getKmlExportWorkspace());
		}
		
		databaseAdapter = dbConnectionPool.getActiveDatabaseAdapter();
		textureExportAdapter = databaseAdapter.getSQLAdapter().getBlobExportAdapter(connection, BlobType.TEXTURE_IMAGE);

		kmlExporterManager = new KmlExporterManager(jaxbKmlContext,
				jaxbColladaContext,
				ioWriterPool,
				tracker,
				kmlFactory,
				textureExportAdapter,
				eventDispatcher,
				config);
		
		elevationServiceHandler = new ElevationServiceHandler();
		
		filterConfig = config.getProject().getKmlExporter().getFilter();
		ColladaOptions colladaOptions = null; 

		objectGroupCounter.put(CityGMLClass.BUILDING, 0);
		objectGroupSize.put(CityGMLClass.BUILDING, 1);
		objectGroup.put(CityGMLClass.BUILDING, null);
		if (filterConfig.getComplexFilter().getFeatureClass().isSetBuilding()) {
			colladaOptions = config.getProject().getKmlExporter().getBuildingColladaOptions();
			if (colladaOptions.isGroupObjects()) {
				objectGroupSize.put(CityGMLClass.BUILDING, colladaOptions.getGroupSize());
			}
		}

		objectGroupCounter.put(CityGMLClass.WATER_BODY, 0);
		objectGroupSize.put(CityGMLClass.WATER_BODY, 1);
		objectGroup.put(CityGMLClass.WATER_BODY, null);
		if (filterConfig.getComplexFilter().getFeatureClass().isSetWaterBody()) {
			colladaOptions = config.getProject().getKmlExporter().getWaterBodyColladaOptions();
			if (colladaOptions.isGroupObjects()) {
				objectGroupSize.put(CityGMLClass.WATER_BODY, colladaOptions.getGroupSize());
			}
		}

		objectGroupCounter.put(CityGMLClass.LAND_USE, 0);
		objectGroupSize.put(CityGMLClass.LAND_USE, 1);
		objectGroup.put(CityGMLClass.LAND_USE, null);
		if (filterConfig.getComplexFilter().getFeatureClass().isSetLandUse()) {
			colladaOptions = config.getProject().getKmlExporter().getLandUseColladaOptions();
			if (colladaOptions.isGroupObjects()) {
				objectGroupSize.put(CityGMLClass.LAND_USE, colladaOptions.getGroupSize());
			}
		}

		objectGroupCounter.put(CityGMLClass.SOLITARY_VEGETATION_OBJECT, 0);
		objectGroupSize.put(CityGMLClass.SOLITARY_VEGETATION_OBJECT, 1);
		objectGroup.put(CityGMLClass.SOLITARY_VEGETATION_OBJECT, null);
		if (filterConfig.getComplexFilter().getFeatureClass().isSetVegetation()) {
			colladaOptions = config.getProject().getKmlExporter().getVegetationColladaOptions();
			if (colladaOptions.isGroupObjects()) {
				objectGroupSize.put(CityGMLClass.SOLITARY_VEGETATION_OBJECT, colladaOptions.getGroupSize());
			}
		}

		objectGroupCounter.put(CityGMLClass.TRANSPORTATION_COMPLEX, 0);
		objectGroupSize.put(CityGMLClass.TRANSPORTATION_COMPLEX, 1);
		objectGroup.put(CityGMLClass.TRANSPORTATION_COMPLEX, null);
		if (filterConfig.getComplexFilter().getFeatureClass().isSetTransportation()) {
			colladaOptions = config.getProject().getKmlExporter().getTransportationColladaOptions();
			if (colladaOptions.isGroupObjects()) {
				objectGroupSize.put(CityGMLClass.TRANSPORTATION_COMPLEX, colladaOptions.getGroupSize());
			}
		}

		objectGroupCounter.put(CityGMLClass.RELIEF_FEATURE, 0);
		objectGroupSize.put(CityGMLClass.RELIEF_FEATURE, 1);
		objectGroup.put(CityGMLClass.RELIEF_FEATURE, null);
		if (filterConfig.getComplexFilter().getFeatureClass().isSetReliefFeature()) {
			colladaOptions = config.getProject().getKmlExporter().getReliefColladaOptions();
			if (colladaOptions.isGroupObjects()) {
				objectGroupSize.put(CityGMLClass.RELIEF_FEATURE, colladaOptions.getGroupSize());
			}
		}

		objectGroupCounter.put(CityGMLClass.GENERIC_CITY_OBJECT, 0);
		objectGroupSize.put(CityGMLClass.GENERIC_CITY_OBJECT, 1);
		objectGroup.put(CityGMLClass.GENERIC_CITY_OBJECT, null);
		if (filterConfig.getComplexFilter().getFeatureClass().isSetGenericCityObject()) {
			colladaOptions = config.getProject().getKmlExporter().getGenericCityObjectColladaOptions();
			if (colladaOptions.isGroupObjects()) {
				objectGroupSize.put(CityGMLClass.GENERIC_CITY_OBJECT, colladaOptions.getGroupSize());
			}
		}

		objectGroupCounter.put(CityGMLClass.CITY_FURNITURE, 0);
		objectGroupSize.put(CityGMLClass.CITY_FURNITURE, 1);
		objectGroup.put(CityGMLClass.CITY_FURNITURE, null);
		if (filterConfig.getComplexFilter().getFeatureClass().isSetCityFurniture()) {
			colladaOptions = config.getProject().getKmlExporter().getCityFurnitureColladaOptions();
			if (colladaOptions.isGroupObjects()) {
				objectGroupSize.put(CityGMLClass.CITY_FURNITURE, colladaOptions.getGroupSize());
			}
		}
		objectGroupCounter.put(CityGMLClass.TUNNEL, 0);
		objectGroupSize.put(CityGMLClass.TUNNEL, 1);
		objectGroup.put(CityGMLClass.TUNNEL, null);
		if (filterConfig.getComplexFilter().getFeatureClass().isSetTunnel()) {
			colladaOptions = config.getProject().getKmlExporter().getTunnelColladaOptions();
			if (colladaOptions.isGroupObjects()) {
				objectGroupSize.put(CityGMLClass.TUNNEL, colladaOptions.getGroupSize());
			}
		}
		objectGroupCounter.put(CityGMLClass.BRIDGE, 0);
		objectGroupSize.put(CityGMLClass.BRIDGE, 1);
		objectGroup.put(CityGMLClass.BRIDGE, null);
		if (filterConfig.getComplexFilter().getFeatureClass().isSetBridge()) {
			colladaOptions = config.getProject().getKmlExporter().getBridgeColladaOptions();
			if (colladaOptions.isGroupObjects()) {
				objectGroupSize.put(CityGMLClass.BRIDGE, colladaOptions.getGroupSize());
			}
		}
		// CityGMLClass.CITY_OBJECT_GROUP is left out, it does not make sense to group it without COLLADA DisplayForm 
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
			//
			// here we have a small problem. since more than one Workers have been created by the Worker Factory,
			// so that each worker may process few works (< group size). as a result the exported objects could be
			// distributed in several groups, one of which may contain less Feature objects.
			
			for (CityGMLClass cityObjectType: objectGroup.keySet()) {
				if (objectGroupCounter.get(cityObjectType) != 0) {  // group is not empty
					KmlGenericObject currentObjectGroup = objectGroup.get(cityObjectType);
					if (currentObjectGroup == null || currentObjectGroup.getGmlId() == null) continue;
					sendGroupToFile(currentObjectGroup);
					currentObjectGroup = null;
					objectGroup.put(cityObjectType, currentObjectGroup);
					objectGroupCounter.put(cityObjectType, 0);
				}
			}
			
			eventDispatcher.triggerEvent(new FeatureCounterEvent(kmlExporterManager.getFeatureCounter(), this));
		}
		finally {
			if (textureExportAdapter != null) {
				try {
					textureExportAdapter.close();
				} catch (SQLException e) {
					// 
				}
			}
			
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

		CityGMLClass featureClass = work.getCityObjectType();
		try {
			switch (featureClass) {
			case BUILDING:
				singleObject = new Building(connection,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(featureClass),
						eventDispatcher,
						config);
				break;

			case WATER_BODY:
			case WATER_CLOSURE_SURFACE:
			case WATER_GROUND_SURFACE:
			case WATER_SURFACE:
				singleObject = new WaterBody(connection,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(featureClass),
						eventDispatcher,
						config);
				break;

			case LAND_USE:
				singleObject = new LandUse(connection,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(featureClass),
						eventDispatcher,
						config);
				break;

			case SOLITARY_VEGETATION_OBJECT:
				singleObject = new SolitaryVegetationObject(connection,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(featureClass),
						eventDispatcher,
						config);
				break;

			case PLANT_COVER:
				singleObject = new PlantCover(connection,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(featureClass),
						eventDispatcher,
						config);
				break;

			case TRAFFIC_AREA:
			case AUXILIARY_TRAFFIC_AREA:
			case TRANSPORTATION_COMPLEX:
			case TRACK:
			case RAILWAY:
			case ROAD:
			case SQUARE:
				singleObject = new Transportation(connection,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(featureClass),
						eventDispatcher,
						config);
				break;
				
			case RELIEF_FEATURE:
				singleObject = new Relief(connection,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(featureClass),
						eventDispatcher,
						config);
				break;

			case GENERIC_CITY_OBJECT:
				singleObject = new GenericCityObject(connection,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(featureClass),
						eventDispatcher,
						config);
				break;

			case CITY_FURNITURE:
				singleObject = new CityFurniture(connection,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(featureClass),
						eventDispatcher,
						config);
				break;

			case CITY_OBJECT_GROUP:
				singleObject = new CityObjectGroup(connection,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(featureClass),
						eventDispatcher,
						config);
				break;
			case BRIDGE:
				singleObject = new Bridge(connection,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(featureClass),
						eventDispatcher,
						config);
				break;
			case TUNNEL:
				singleObject = new Tunnel(connection,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(featureClass),
						eventDispatcher,
						config);
				break;
			default:
				break;

			}

			singleObject.read(work);

			if (!work.isCityObjectGroup() && 
					work.getDisplayForm().getForm() == DisplayForm.COLLADA &&
					singleObject.getGmlId() != null) { // object is filled

				// correction for some CityGML Types exported together
				if (featureClass == CityGMLClass.PLANT_COVER) featureClass = CityGMLClass.SOLITARY_VEGETATION_OBJECT;

				if (featureClass == CityGMLClass.WATER_CLOSURE_SURFACE ||
						featureClass == CityGMLClass.WATER_GROUND_SURFACE ||
						featureClass == CityGMLClass.WATER_SURFACE) featureClass = CityGMLClass.WATER_BODY;

				if (featureClass == CityGMLClass.TRAFFIC_AREA ||
						featureClass == CityGMLClass.AUXILIARY_TRAFFIC_AREA ||
						featureClass == CityGMLClass.TRACK ||
						featureClass == CityGMLClass.RAILWAY ||
						featureClass == CityGMLClass.ROAD ||
						featureClass == CityGMLClass.SQUARE) featureClass = CityGMLClass.TRANSPORTATION_COMPLEX;

				KmlGenericObject currentObjectGroup = objectGroup.get(featureClass);
				if (currentObjectGroup == null) {
					currentObjectGroup = singleObject;
					objectGroup.put(featureClass, currentObjectGroup);
				}
				else {
					currentObjectGroup.appendObject(singleObject);
				}

				objectGroupCounter.put(featureClass, objectGroupCounter.get(featureClass).intValue() + 1);
				if (objectGroupCounter.get(featureClass).intValue() == objectGroupSize.get(featureClass).intValue()) {
					sendGroupToFile(currentObjectGroup);
					currentObjectGroup = null;
					objectGroup.put(featureClass, currentObjectGroup);
					objectGroupCounter.put(featureClass, 0);
				}
			}
		}
		finally {
			runLock.unlock();
		}
	}

	private void sendGroupToFile(KmlGenericObject objectGroup) {
		try {
			double imageScaleFactor = 1;
			ColladaOptions colladaOptions = objectGroup.getColladaOptions();
			
			if (colladaOptions.isCropImages()) {
				objectGroup.cropImages();
			}
			
			if (colladaOptions.isGenerateTextureAtlases()) {
				if (colladaOptions.isScaleImages()) {
					imageScaleFactor = colladaOptions.getImageScaleFactor();
				}
				objectGroup.createTextureAtlas(colladaOptions.getPackingAlgorithm(),
						imageScaleFactor,
						colladaOptions.isTextureAtlasPots());
			}
			else if (colladaOptions.isScaleImages()) {
				imageScaleFactor = colladaOptions.getImageScaleFactor();
				if (imageScaleFactor < 1) {
					objectGroup.resizeAllImagesByFactor(imageScaleFactor);
				}
			}

			ColladaBundle colladaBundle = new ColladaBundle();
			colladaBundle.setCollada(objectGroup.generateColladaTree());
			colladaBundle.setTexImages(objectGroup.getTexImages());
			colladaBundle.setUnsupportedTexImageIds(objectGroup.getUnsupportedTexImageIds());
			colladaBundle.setPlacemark(objectGroup.createPlacemarkForColladaModel());
			colladaBundle.setGmlId(objectGroup.getGmlId());
			colladaBundle.setId(objectGroup.getId());

			kmlExporterManager.print(colladaBundle,
					objectGroup.getId(),					
					objectGroup.getBalloonSettings().isBalloonContentInSeparateFile());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private BalloonTemplateHandler getBalloonTemplateHandler(CityGMLClass cityObjectType) {
		BalloonTemplateHandler currentBalloonTemplateHandler = balloonTemplateHandler.get(cityObjectType);

		if (currentBalloonTemplateHandler == null) {
			Balloon balloonSettings = getBalloonSettings(cityObjectType);
			if (balloonSettings != null &&	balloonSettings.isIncludeDescription() &&
					balloonSettings.getBalloonContentMode() != BalloonContentMode.GEN_ATTRIB) {
				String balloonTemplateFilename = balloonSettings.getBalloonContentTemplateFile();
				if (balloonTemplateFilename != null && balloonTemplateFilename.length() > 0) {
					currentBalloonTemplateHandler = databaseAdapter.getBalloonTemplateHandler(new File(balloonTemplateFilename));
					balloonTemplateHandler.put(cityObjectType, currentBalloonTemplateHandler);
				}
			}
		}

		return currentBalloonTemplateHandler;
	}

	private Balloon getBalloonSettings(CityGMLClass cityObjectType) {
		Balloon balloonSettings = null;
		switch (cityObjectType) {
		case BUILDING:
			balloonSettings = config.getProject().getKmlExporter().getBuildingBalloon();
			break;
		case LAND_USE:
			balloonSettings = config.getProject().getKmlExporter().getLandUseBalloon();
			break;
		case WATER_BODY:
		case WATER_CLOSURE_SURFACE:
		case WATER_GROUND_SURFACE:
		case WATER_SURFACE:
			balloonSettings = config.getProject().getKmlExporter().getWaterBodyBalloon();
			break;
		case SOLITARY_VEGETATION_OBJECT:
		case PLANT_COVER:
			balloonSettings = config.getProject().getKmlExporter().getVegetationBalloon();
			break;
		case TRAFFIC_AREA:
		case AUXILIARY_TRAFFIC_AREA:
		case TRANSPORTATION_COMPLEX:
		case TRACK:
		case RAILWAY:
		case ROAD:
		case SQUARE:
			balloonSettings = config.getProject().getKmlExporter().getTransportationBalloon();
			break;
		case RELIEF_FEATURE:
			balloonSettings = config.getProject().getKmlExporter().getReliefBalloon();
			break;
		case GENERIC_CITY_OBJECT:
			balloonSettings = config.getProject().getKmlExporter().getGenericCityObject3DBalloon();
			break;
		case CITY_FURNITURE:
			balloonSettings = config.getProject().getKmlExporter().getCityFurnitureBalloon();
			break;
		case CITY_OBJECT_GROUP:
			balloonSettings = config.getProject().getKmlExporter().getCityObjectGroupBalloon();
			break;
		case BRIDGE:
			balloonSettings = config.getProject().getKmlExporter().getBridgeBalloon();
			break;
		case TUNNEL:
			balloonSettings = config.getProject().getKmlExporter().getTunnelBalloon();
			break;
		default:
			return null;
		}
		return balloonSettings;
	}

}
