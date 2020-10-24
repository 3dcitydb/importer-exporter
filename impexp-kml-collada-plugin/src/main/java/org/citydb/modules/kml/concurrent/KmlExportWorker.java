/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import net.opengis.kml._2.ObjectFactory;
import org.citydb.ade.kmlExporter.ADEKmlExportExtensionManager;
import org.citydb.concurrent.Worker;
import org.citydb.concurrent.WorkerPool;
import org.citydb.config.Config;
import org.citydb.config.project.kmlExporter.Balloon;
import org.citydb.config.project.kmlExporter.BalloonContentMode;
import org.citydb.config.project.kmlExporter.ColladaOptions;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.database.adapter.BlobType;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.event.EventDispatcher;
import org.citydb.event.global.ObjectCounterEvent;
import org.citydb.log.Logger;
import org.citydb.modules.kml.database.ColladaBundle;
import org.citydb.modules.kml.database.KmlExporterManager;
import org.citydb.modules.kml.database.KmlGenericObject;
import org.citydb.modules.kml.database.KmlSplittingResult;
import org.citydb.modules.kml.util.BalloonTemplateHandler;
import org.citydb.modules.kml.util.ElevationServiceHandler;
import org.citydb.modules.kml.util.ExportTracker;
import org.citydb.query.Query;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.bridge.Bridge;
import org.citygml4j.model.citygml.building.Building;
import org.citygml4j.model.citygml.cityfurniture.CityFurniture;
import org.citygml4j.model.citygml.cityobjectgroup.CityObjectGroup;
import org.citygml4j.model.citygml.generics.GenericCityObject;
import org.citygml4j.model.citygml.landuse.LandUse;
import org.citygml4j.model.citygml.relief.ReliefFeature;
import org.citygml4j.model.citygml.transportation.Railway;
import org.citygml4j.model.citygml.transportation.Road;
import org.citygml4j.model.citygml.transportation.Square;
import org.citygml4j.model.citygml.transportation.Track;
import org.citygml4j.model.citygml.transportation.TransportationComplex;
import org.citygml4j.model.citygml.tunnel.Tunnel;
import org.citygml4j.model.citygml.vegetation.AbstractVegetationObject;
import org.citygml4j.model.citygml.vegetation.PlantCover;
import org.citygml4j.model.citygml.vegetation.SolitaryVegetationObject;
import org.citygml4j.model.citygml.waterbody.WaterBody;
import org.citygml4j.model.gml.base.AbstractGML;
import org.citygml4j.util.xml.SAXEventBuffer;

import javax.xml.bind.JAXBContext;
import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class KmlExportWorker extends Worker<KmlSplittingResult> {
	private final ReentrantLock runLock = new ReentrantLock();
	private volatile boolean shouldRun = true;

	private final Connection connection;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final BlobExportAdapter textureExportAdapter;
	private final Query query;
	private final ObjectFactory kmlFactory; 
	private final Config config;
	private final EventDispatcher eventDispatcher;
	private final KmlExporterManager kmlExporterManager;

	private final Map<Class<? extends AbstractGML>, Integer> objectGroupCounter = new HashMap<>();
	private final Map<Class<? extends AbstractGML>, Integer> objectGroupSize = new HashMap<>();
	private final Map<Class<? extends AbstractGML>, KmlGenericObject> objectGroup = new HashMap<>();
	private final Map<Class<? extends AbstractGML>, BalloonTemplateHandler> balloonTemplateHandler = new HashMap<>();

	private final ElevationServiceHandler elevationServiceHandler;
	private final Logger log = Logger.getInstance();

	public KmlExportWorker(Path outputFile,
			Connection connection,
			AbstractDatabaseAdapter databaseAdapter,
			JAXBContext jaxbKmlContext,
			JAXBContext jaxbColladaContext,
			WorkerPool<SAXEventBuffer> writerPool,
			ExportTracker tracker,
			Query query,
			ObjectFactory kmlFactory,
			Config config,
			EventDispatcher eventDispatcher) {
		this.connection = connection;
		this.databaseAdapter = databaseAdapter;
		this.query = query;
		this.kmlFactory = kmlFactory;
		this.config = config;
		this.eventDispatcher = eventDispatcher;

		textureExportAdapter = databaseAdapter.getSQLAdapter().getBlobExportAdapter(connection, BlobType.TEXTURE_IMAGE);

		kmlExporterManager = new KmlExporterManager(outputFile,
				jaxbKmlContext,
				jaxbColladaContext,
				databaseAdapter,
				writerPool,
				tracker,
				query,
				kmlFactory,
				textureExportAdapter,
				eventDispatcher,
				config);

		elevationServiceHandler = new ElevationServiceHandler(config);

		FeatureTypeFilter typeFilter = query.getFeatureTypeFilter();

		for (FeatureType featureType : typeFilter.getFeatureTypes()) {
			AbstractGML object = Util.createObject(featureType.getObjectClassId(), query.getTargetVersion());
			if (object == null) {
				log.error("Failed to instantiate citygml4j object for " + featureType.getSchema().getXMLPrefix() + ":" + featureType.getPath() + ".");
				continue;
			}
			Class<? extends AbstractGML> objectClass = object.getClass();
			objectGroupCounter.put(objectClass, 0);
			objectGroupSize.put(objectClass, 1);
			objectGroup.put(objectClass, null);

			if (Building.class.equals(objectClass)) {
				ColladaOptions colladaOptions = config.getProject().getKmlExportConfig().getBuildingColladaOptions();
				if (colladaOptions.isGroupObjects())
					objectGroupSize.put(Building.class, colladaOptions.getGroupSize());
			} else if (WaterBody.class.equals(objectClass)) {
				ColladaOptions colladaOptions = config.getProject().getKmlExportConfig().getWaterBodyColladaOptions();
				if (colladaOptions.isGroupObjects())
					objectGroupSize.put(WaterBody.class, colladaOptions.getGroupSize());
			} else if (LandUse.class.equals(objectClass)) {
				ColladaOptions colladaOptions = config.getProject().getKmlExportConfig().getLandUseColladaOptions();
				if (colladaOptions.isGroupObjects())
					objectGroupSize.put(LandUse.class, colladaOptions.getGroupSize());
			} else if (SolitaryVegetationObject.class.equals(objectClass) || PlantCover.class.equals(objectClass)) {
				objectGroupCounter.put(AbstractVegetationObject.class, 0);
				objectGroupSize.put(AbstractVegetationObject.class, 1);
				objectGroup.put(AbstractVegetationObject.class, null);
				ColladaOptions colladaOptions = config.getProject().getKmlExportConfig().getVegetationColladaOptions();
				if (colladaOptions.isGroupObjects())
					objectGroupSize.put(AbstractVegetationObject.class, colladaOptions.getGroupSize());
			} else if (TransportationComplex.class.equals(objectClass)
					|| Track.class.equals(objectClass)
					|| Railway.class.equals(objectClass)
					|| Road.class.equals(objectClass)
					|| Square.class.equals(objectClass)) {
				objectGroupCounter.put(TransportationComplex.class, 0);
				objectGroupSize.put(TransportationComplex.class, 1);
				objectGroup.put(TransportationComplex.class, null);
				ColladaOptions colladaOptions = config.getProject().getKmlExportConfig().getTransportationColladaOptions();
				if (colladaOptions.isGroupObjects())
					objectGroupSize.put(TransportationComplex.class, colladaOptions.getGroupSize());
			} else if (ReliefFeature.class.equals(objectClass)) {
				ColladaOptions colladaOptions = config.getProject().getKmlExportConfig().getReliefColladaOptions();
				if (colladaOptions.isGroupObjects())
					objectGroupSize.put(ReliefFeature.class, colladaOptions.getGroupSize());
			} else if (GenericCityObject.class.equals(objectClass)) {
				ColladaOptions colladaOptions = config.getProject().getKmlExportConfig().getGenericCityObjectColladaOptions();
				if (colladaOptions.isGroupObjects())
					objectGroupSize.put(GenericCityObject.class, colladaOptions.getGroupSize());
			} else if (CityFurniture.class.equals(objectClass)) {
				ColladaOptions colladaOptions = config.getProject().getKmlExportConfig().getCityFurnitureColladaOptions();
				if (colladaOptions.isGroupObjects())
					objectGroupSize.put(CityFurniture.class, colladaOptions.getGroupSize());
			} else if (Tunnel.class.equals(objectClass)) {
				ColladaOptions colladaOptions = config.getProject().getKmlExportConfig().getTunnelColladaOptions();
				if (colladaOptions.isGroupObjects())
					objectGroupSize.put(Tunnel.class, colladaOptions.getGroupSize());
			} else if (Bridge.class.equals(objectClass)) {
				ColladaOptions colladaOptions = config.getProject().getKmlExportConfig().getBridgeColladaOptions();
				if (colladaOptions.isGroupObjects())
					objectGroupSize.put(Bridge.class, colladaOptions.getGroupSize());
			} else {
				ColladaOptions colladaOptions = ADEKmlExportExtensionManager.getInstance().getPreference(config, featureType).getColladaOptions();
				if (colladaOptions.isGroupObjects())
					objectGroupSize.put(objectClass, colladaOptions.getGroupSize());
			}
			// CityGMLClass.CITY_OBJECT_GROUP is left out, it does not make sense to group it without COLLADA DisplayForm
		}		
	}

	@Override
	public void interrupt() {
		shouldRun = false;
	}

	@Override
	public void run() {
		try {
			if (firstWork != null) {
				doWork(firstWork);
				firstWork = null;
			}

			KmlSplittingResult work;
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

			for (Class<? extends AbstractGML> cityObjectType: objectGroup.keySet()) {
				if (objectGroupCounter.get(cityObjectType) != 0) {  // group is not empty
					KmlGenericObject currentObjectGroup = objectGroup.get(cityObjectType);
					if (currentObjectGroup == null || currentObjectGroup.getGmlId() == null) continue;
					sendGroupToFile(currentObjectGroup);
					objectGroup.put(cityObjectType, null);
					objectGroupCounter.put(cityObjectType, 0);
				}
			}
			
			eventDispatcher.triggerEvent(new ObjectCounterEvent(kmlExporterManager.getObjectCounter(), this));
		}
		finally {
			if (textureExportAdapter != null) {
				try {
					textureExportAdapter.close();
				} catch (SQLException e) {
					// 
				}
			}

			try {
				connection.commit(); // for all possible GE_LoDn_zOffset values
				connection.close();
			} catch (SQLException e) {
				//
			}
		}
	}

	private void doWork(KmlSplittingResult work) {
		final ReentrantLock runLock = this.runLock;
		runLock.lock();

		int objectClassId = work.getObjectClassId();
		AbstractGML object = Util.createObject(objectClassId, query.getTargetVersion());
		if (object == null) {
			log.error("Failed to instantiate citygml4j object for (objectClassId: " + objectClassId + ", id: " + work.getId() + "). Skipping export.");
			return;
		}
		try {
			Class<? extends AbstractGML> objectClass = object.getClass();
			KmlGenericObject singleObject;
			if (Building.class.equals(objectClass)) {
				singleObject = new org.citydb.modules.kml.database.Building(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config);
			} else if (WaterBody.class.equals(objectClass)) {
				singleObject = new org.citydb.modules.kml.database.WaterBody(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config);
			} else if (LandUse.class.equals(objectClass)) {
				singleObject = new org.citydb.modules.kml.database.LandUse(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config);
			} else if (SolitaryVegetationObject.class.equals(objectClass)) {
				singleObject = new org.citydb.modules.kml.database.SolitaryVegetationObject(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config);
			} else if (PlantCover.class.equals(objectClass)) {
				singleObject = new org.citydb.modules.kml.database.PlantCover(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config);
			} else if (TransportationComplex.class.equals(objectClass)
					|| Track.class.equals(objectClass)
					|| Railway.class.equals(objectClass)
					|| Road.class.equals(objectClass)
					|| Square.class.equals(objectClass)) {
				singleObject = new org.citydb.modules.kml.database.Transportation(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config);
			} else if (ReliefFeature.class.equals(objectClass)) {
				singleObject = new org.citydb.modules.kml.database.Relief(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config);
			} else if (GenericCityObject.class.equals(objectClass)) {
				singleObject = new org.citydb.modules.kml.database.GenericCityObject(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config);
			} else if (CityFurniture.class.equals(objectClass)) {
				singleObject = new org.citydb.modules.kml.database.CityFurniture(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config);
			} else if (CityObjectGroup.class.equals(objectClass)) {
				singleObject = new org.citydb.modules.kml.database.CityObjectGroup(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config);
			} else if (Bridge.class.equals(objectClass)) {
				singleObject = new org.citydb.modules.kml.database.Bridge(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config);
			} else if (Tunnel.class.equals(objectClass)) {
				singleObject = new org.citydb.modules.kml.database.Tunnel(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config);
			} else {
				singleObject = new org.citydb.modules.kml.database.ADEObject(connection,
						query,
						kmlExporterManager,
						kmlFactory,
						databaseAdapter,
						textureExportAdapter,
						elevationServiceHandler,
						getBalloonTemplateHandler(objectClass),
						eventDispatcher,
						config,
						work.getObjectClassId());
			}

			singleObject.read(work);

			if (!CityObjectGroup.class.equals(objectClass) &&
					work.getDisplayForm().getForm() == DisplayForm.COLLADA &&
					singleObject.getGmlId() != null) { // object is filled

				// correction for some CityGML Types exported together
				if (PlantCover.class.equals(objectClass) || SolitaryVegetationObject.class.equals(objectClass))
					objectClass = AbstractVegetationObject.class;

				if (Track.class.equals(objectClass) || Railway.class.equals(objectClass) || Road.class.equals(objectClass) || Square.class.equals(objectClass))
					objectClass = TransportationComplex.class;

				KmlGenericObject currentObjectGroup = objectGroup.get(objectClass);
				if (currentObjectGroup == null) {
					currentObjectGroup = singleObject;
					objectGroup.put(objectClass, currentObjectGroup);
				}
				else {
					currentObjectGroup.appendObject(singleObject);
				}

				objectGroupCounter.put(objectClass, objectGroupCounter.get(objectClass) + 1);
				if (objectGroupCounter.get(objectClass).intValue() == objectGroupSize.get(objectClass).intValue()) {
					sendGroupToFile(currentObjectGroup);
					objectGroup.put(objectClass, null);
					objectGroupCounter.put(objectClass, 0);
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
			Logger.getInstance().logStackTrace(e);
		}
	}

	private BalloonTemplateHandler getBalloonTemplateHandler(Class<? extends AbstractGML> objectClass) {
		BalloonTemplateHandler currentBalloonTemplateHandler = balloonTemplateHandler.get(objectClass);

		if (currentBalloonTemplateHandler == null) {
			Balloon balloonSettings = getBalloonSettings(objectClass);
			if (balloonSettings != null &&	balloonSettings.isIncludeDescription() &&
					balloonSettings.getBalloonContentMode() != BalloonContentMode.GEN_ATTRIB) {
				String balloonTemplateFilename = balloonSettings.getBalloonContentTemplateFile();
				if (balloonTemplateFilename != null && balloonTemplateFilename.length() > 0) {
					currentBalloonTemplateHandler = new BalloonTemplateHandler(new File(balloonTemplateFilename), databaseAdapter);
					balloonTemplateHandler.put(objectClass, currentBalloonTemplateHandler);
				}
			}
		}

		return currentBalloonTemplateHandler;
	}

	private Balloon getBalloonSettings(Class<? extends AbstractGML> objectClass) {
		Balloon balloonSettings;
		if (Building.class.equals(objectClass)) {
			balloonSettings = config.getProject().getKmlExportConfig().getBuildingBalloon();
		} else if (LandUse.class.equals(objectClass)) {
			balloonSettings = config.getProject().getKmlExportConfig().getLandUseBalloon();
		} else if (WaterBody.class.equals(objectClass)) {
			balloonSettings = config.getProject().getKmlExportConfig().getWaterBodyBalloon();
		} else if (SolitaryVegetationObject.class.equals(objectClass) || PlantCover.class.equals(objectClass)) {
			balloonSettings = config.getProject().getKmlExportConfig().getVegetationBalloon();
		} else if (TransportationComplex.class.equals(objectClass)
				|| Track.class.equals(objectClass)
				|| Railway.class.equals(objectClass)
				|| Road.class.equals(objectClass)
				|| Square.class.equals(objectClass)) {
			balloonSettings = config.getProject().getKmlExportConfig().getTransportationBalloon();
		} else if (ReliefFeature.class.equals(objectClass)) {
			balloonSettings = config.getProject().getKmlExportConfig().getReliefBalloon();
		} else if (GenericCityObject.class.equals(objectClass)) {
			balloonSettings = config.getProject().getKmlExportConfig().getGenericCityObject3DBalloon();
		} else if (CityFurniture.class.equals(objectClass)) {
			balloonSettings = config.getProject().getKmlExportConfig().getCityFurnitureBalloon();
		} else if (CityObjectGroup.class.equals(objectClass)) {
			balloonSettings = config.getProject().getKmlExportConfig().getCityObjectGroupBalloon();
		} else if (Bridge.class.equals(objectClass)) {
			balloonSettings = config.getProject().getKmlExportConfig().getBridgeBalloon();
		} else if (Tunnel.class.equals(objectClass)) {
			balloonSettings = config.getProject().getKmlExportConfig().getTunnelBalloon();
		} else {
			balloonSettings = ADEKmlExportExtensionManager.getInstance().getPreference(config, Util.getObjectClassId(objectClass)).getBalloon();
		}

		return balloonSettings;
	}

}
