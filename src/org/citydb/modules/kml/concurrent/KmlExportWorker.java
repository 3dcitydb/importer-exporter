/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package org.citydb.modules.kml.concurrent;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.bind.JAXBContext;

import net.opengis.kml._2.ObjectFactory;

import org.citydb.api.concurrent.Worker;
import org.citydb.api.concurrent.WorkerPool;
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
import org.citydb.modules.kml.database.BalloonTemplateHandlerImpl;
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
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.util.xml.SAXEventBuffer;

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
	private EnumMap<CityGMLClass, BalloonTemplateHandlerImpl>balloonTemplateHandler = new EnumMap<CityGMLClass, BalloonTemplateHandlerImpl>(CityGMLClass.class);

	private ElevationServiceHandler elevationServiceHandler;

	public KmlExportWorker(JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			DatabaseConnectionPool dbConnectionPool,
			WorkerPool<SAXEventBuffer> ioWriterPool,
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
				kmlFactory,
				textureExportAdapter,
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
			if (colladaOptions.isGenerateTextureAtlases()) {
				//				eventDispatcher.triggerEvent(new StatusDialogMessage(Internal.I18N.getString("kmlExport.dialog.creatingAtlases")));
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

	private BalloonTemplateHandlerImpl getBalloonTemplateHandler(CityGMLClass cityObjectType) {
		BalloonTemplateHandlerImpl currentBalloonTemplateHandler = balloonTemplateHandler.get(cityObjectType);

		if (currentBalloonTemplateHandler == null) {
			Balloon balloonSettings = getBalloonSettings(cityObjectType);
			if (balloonSettings != null &&	balloonSettings.isIncludeDescription() &&
					balloonSettings.getBalloonContentMode() != BalloonContentMode.GEN_ATTRIB) {
				String balloonTemplateFilename = balloonSettings.getBalloonContentTemplateFile();
				if (balloonTemplateFilename != null && balloonTemplateFilename.length() > 0) {
					currentBalloonTemplateHandler = new BalloonTemplateHandlerImpl(new File(balloonTemplateFilename), connection);
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
