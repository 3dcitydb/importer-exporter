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
package org.citydb.modules.kml.gui.preferences;

import org.citydb.ade.ADEExtension;
import org.citydb.ade.ADEExtensionManager;
import org.citydb.config.Config;
import org.citydb.database.schema.mapping.AppSchema;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.gui.preferences.AbstractPreferences;
import org.citydb.gui.preferences.DefaultPreferencesEntry;
import org.citydb.ade.kmlExporter.ADEKmlExportExtension;
import org.citydb.plugin.extension.view.ViewController;

import java.util.List;
import java.util.stream.Collectors;

public class KMLExportPreferences extends AbstractPreferences {
	
	public KMLExportPreferences(ViewController viewController, Config config) {
		super(new KMLExportEntry());
		
		DefaultPreferencesEntry renderingNode = new RenderingPanel();
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new BuildingRenderingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new WaterBodyRenderingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new LandUseRenderingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new VegetationRenderingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new TransportationRenderingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new ReliefRenderingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new CityFurnitureRenderingPanel(config)));		
		DefaultPreferencesEntry genericCityObjectRenderingNode = new GenericCityObjectBalloonPanel();
		genericCityObjectRenderingNode.addChildEntry(new DefaultPreferencesEntry(new ThreeDRenderingPanel(config)));
		genericCityObjectRenderingNode.addChildEntry(new DefaultPreferencesEntry(new PointAndCurveRenderingPanel(config)));
		renderingNode.addChildEntry(genericCityObjectRenderingNode);		
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new CityObjectGroupRenderingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new BridgeRenderingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new TunnelRenderingPanel(config)));

		DefaultPreferencesEntry balloonNode = new BalloonPanel();
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BuildingBalloonPanel(config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new WaterBodyBalloonPanel(config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new LandUseBalloonPanel(config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new VegetationBalloonPanel(config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new TransportationBalloonPanel(config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new ReliefBalloonPanel(config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new CityFurnitureBalloonPanel(config)));
		DefaultPreferencesEntry genericCityObjectBalloonNode = new GenericCityObjectBalloonPanel();
		genericCityObjectBalloonNode.addChildEntry(new DefaultPreferencesEntry(new ThreeDBalloonPanel(config)));
		genericCityObjectBalloonNode.addChildEntry(new DefaultPreferencesEntry(new PointAndCurveBalloonPanel(config)));
		balloonNode.addChildEntry(genericCityObjectBalloonNode);				
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new CityObjectGroupBalloonPanel(config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BridgeBalloonPanel(config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new TunnelBalloonPanel(config)));

		// ADEs
		List<ADEExtension> adeExtensions = ADEExtensionManager.getInstance().getExtensions().stream()
				.filter(e -> e instanceof ADEKmlExportExtension)
				.collect(Collectors.toList());
		
		if (!adeExtensions.isEmpty()) {
			DefaultPreferencesEntry adeRenderingRootNode = new ADEPanel("ADEs");
			DefaultPreferencesEntry adeBalloonRootNode = new ADEPanel("ADEs");

			for (ADEExtension adeExtension : adeExtensions) {
				String name = adeExtension.getMetadata().getName();
				DefaultPreferencesEntry adeRenderingNode = new ADEPanel(name);
				DefaultPreferencesEntry adeBalloonNode = new ADEPanel(name);

				for (AppSchema schema : adeExtension.getSchemas()) {
					for (FeatureType adeTopLevelFeatureType : schema.listTopLevelFeatureTypes(true)) {
						DefaultPreferencesEntry adeFeatureRenderingNode = new ADEPanel(adeTopLevelFeatureType.toString());
						adeFeatureRenderingNode.addChildEntry(new DefaultPreferencesEntry(new ADEThreeDRenderingPanel(config, adeTopLevelFeatureType)));
						adeFeatureRenderingNode.addChildEntry(new DefaultPreferencesEntry(new ADEPointAndCurveRenderingPanel(config, adeTopLevelFeatureType)));
						adeRenderingNode.addChildEntry(adeFeatureRenderingNode);
						adeBalloonNode.addChildEntry(new DefaultPreferencesEntry(new ADEDBalloonPanel(config, adeTopLevelFeatureType)));
					}
				}

				adeRenderingRootNode.addChildEntry(adeRenderingNode);
				adeBalloonRootNode.addChildEntry(adeBalloonNode);
			}

			renderingNode.addChildEntry(adeRenderingRootNode);
			balloonNode.addChildEntry(adeBalloonRootNode);
		}

		root.addChildEntry(new DefaultPreferencesEntry(new GeneralPanel(viewController, config)));
		root.addChildEntry(renderingNode);
		root.addChildEntry(balloonNode);
		root.addChildEntry(new DefaultPreferencesEntry(new AltitudePanel(config)));
	}

}
