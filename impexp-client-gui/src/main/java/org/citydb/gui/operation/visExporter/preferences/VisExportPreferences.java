/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.gui.operation.visExporter.preferences;

import org.citydb.core.ade.ADEExtension;
import org.citydb.core.ade.ADEExtensionManager;
import org.citydb.core.ade.visExporter.ADEVisExportExtension;
import org.citydb.core.ade.visExporter.ADEVisExportExtensionManager;
import org.citydb.config.Config;
import org.citydb.config.i18n.Language;
import org.citydb.core.database.schema.mapping.AppSchema;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.gui.operation.common.DefaultPreferences;
import org.citydb.gui.operation.common.DefaultPreferencesEntry;
import org.citydb.core.plugin.extension.view.ViewController;

import java.util.List;
import java.util.stream.Collectors;

public class VisExportPreferences extends DefaultPreferences {
	
	public VisExportPreferences(ViewController viewController, Config config) {
		super(new VisExportEntry());

		DefaultPreferencesEntry renderingNode = new StylingPanel();
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new SurfaceStylingPanel(
				"pref.tree.visExport.bridge.styling",
				() -> config.getVisExportConfig().getBridgeStyles(),
				true, true, true, true,
				config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new BuildingStylingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new SurfaceStylingPanel(
				"pref.tree.visExport.cityFurniture.styling",
				() -> config.getVisExportConfig().getCityFurnitureStyles(),
				config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new SurfaceStylingPanel(
				"pref.tree.visExport.cityObjectGroup.styling",
				() -> config.getVisExportConfig().getReliefStyles(),
				true, false, false, false, config)));
		DefaultPreferencesEntry genericCityObjectRenderingNode = new EmptyPanel(
				() -> Language.I18N.getString("pref.tree.visExport.genericCityObject.styling"));
		genericCityObjectRenderingNode.addChildEntry(new DefaultPreferencesEntry(new SurfaceStylingPanel(
				"pref.tree.visExport.surfaceAndSolid.styling",
				() -> config.getVisExportConfig().getGenericCityObjectStyles(),
				config)));
		genericCityObjectRenderingNode.addChildEntry(new DefaultPreferencesEntry(new PointAndCurveStylingPanel(
				() -> config.getVisExportConfig().getGenericCityObjectPointAndCurve(),
				config)));
		renderingNode.addChildEntry(genericCityObjectRenderingNode);
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new SurfaceStylingPanel(
				"pref.tree.visExport.landUse.styling",
				() -> config.getVisExportConfig().getLandUseStyles(),
				config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new SurfaceStylingPanel(
				"pref.tree.visExport.relief.styling",
				() -> config.getVisExportConfig().getReliefStyles(),
				config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new SurfaceStylingPanel(
				"pref.tree.visExport.transportation.styling",
				() -> config.getVisExportConfig().getTransportationStyles(),
				config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new SurfaceStylingPanel(
				"pref.tree.visExport.tunnel.styling",
				() -> config.getVisExportConfig().getTunnelStyles(),
				true, true, true, true,
				config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new SurfaceStylingPanel(
				"pref.tree.visExport.vegetation.styling",
				() -> config.getVisExportConfig().getVegetationStyles(),
				config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new SurfaceStylingPanel(
				"pref.tree.visExport.waterBody.styling",
				() -> config.getVisExportConfig().getWaterBodyStyles(),
				config)));

		DefaultPreferencesEntry balloonNode = new BalloonPanel();
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BalloonContentPanel(
				() -> Language.I18N.getString("pref.tree.visExport.bridge.balloon"),
				() -> config.getVisExportConfig().getBridgeBalloon(),
				config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BalloonContentPanel(
				() -> Language.I18N.getString("pref.tree.visExport.building.balloon"),
				() -> config.getVisExportConfig().getBuildingBalloon(),
				config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BalloonContentPanel(
				() -> Language.I18N.getString("pref.tree.visExport.cityFurniture.balloon"),
				() -> config.getVisExportConfig().getCityFurnitureBalloon(),
				config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BalloonContentPanel(
				() -> Language.I18N.getString("pref.tree.visExport.cityObjectGroup.balloon"),
				() -> config.getVisExportConfig().getCityObjectGroupBalloon(),
				config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BalloonContentPanel(
				() -> Language.I18N.getString("pref.tree.visExport.genericCityObject.balloon"),
				() -> config.getVisExportConfig().getGenericCityObject3DBalloon(),
				() -> config.getVisExportConfig().getGenericCityObjectPointAndCurve().getPointBalloon(),
				() -> config.getVisExportConfig().getGenericCityObjectPointAndCurve().getCurveBalloon(),
				config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BalloonContentPanel(
				() -> Language.I18N.getString("pref.tree.visExport.landUse.balloon"),
				() -> config.getVisExportConfig().getLandUseBalloon(),
				config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BalloonContentPanel(
				() -> Language.I18N.getString("pref.tree.visExport.relief.balloon"),
				() -> config.getVisExportConfig().getReliefBalloon(),
				config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BalloonContentPanel(
				() -> Language.I18N.getString("pref.tree.visExport.transportation.balloon"),
				() -> config.getVisExportConfig().getTransportationBalloon(),
				config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BalloonContentPanel(
				() -> Language.I18N.getString("pref.tree.visExport.tunnel.balloon"),
				() -> config.getVisExportConfig().getTunnelBalloon(),
				config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BalloonContentPanel(
				() -> Language.I18N.getString("pref.tree.visExport.vegetation.balloon"),
				() -> config.getVisExportConfig().getVegetationBalloon(),
				config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BalloonContentPanel(
				() -> Language.I18N.getString("pref.tree.visExport.waterBody.balloon"),
				() -> config.getVisExportConfig().getWaterBodyBalloon(),
				config)));

		// ADEs
		List<ADEExtension> adeExtensions = ADEExtensionManager.getInstance().getExtensions().stream()
				.filter(e -> e instanceof ADEVisExportExtension)
				.collect(Collectors.toList());
		
		if (!adeExtensions.isEmpty()) {
			ADEVisExportExtensionManager adeManager = ADEVisExportExtensionManager.getInstance();

			for (ADEExtension adeExtension : adeExtensions) {
				DefaultPreferencesEntry adeRenderingNode = new EmptyPanel(adeExtension.getMetadata()::getName);
				DefaultPreferencesEntry adeBalloonNode = new EmptyPanel(adeExtension.getMetadata()::getName);

				for (AppSchema schema : adeExtension.getSchemas()) {
					for (FeatureType adeTopLevelFeatureType : schema.listTopLevelFeatureTypes(true)) {
						DefaultPreferencesEntry adeFeatureRenderingNode = new EmptyPanel(adeTopLevelFeatureType::toString);

						adeFeatureRenderingNode.addChildEntry(new DefaultPreferencesEntry(new SurfaceStylingPanel(
								"pref.tree.visExport.surfaceAndSolid.styling",
								() -> adeManager.getPreference(config, adeTopLevelFeatureType).getStyles(),
								config)));

						adeFeatureRenderingNode.addChildEntry(new DefaultPreferencesEntry(new PointAndCurveStylingPanel(
								() -> adeManager.getPreference(config, adeTopLevelFeatureType).getPointAndCurve(),
								config)));

						adeRenderingNode.addChildEntry(adeFeatureRenderingNode);
						adeBalloonNode.addChildEntry(new DefaultPreferencesEntry(new BalloonContentPanel(
								() -> adeManager.getPreference(config, adeTopLevelFeatureType).getTarget(),
								() -> adeManager.getPreference(config, adeTopLevelFeatureType).getBalloon(),
								config)));
					}
				}

				renderingNode.addChildEntry(adeRenderingNode);
				balloonNode.addChildEntry(adeBalloonNode);
			}
		}

		root.addChildEntry(new DefaultPreferencesEntry(new GeneralPanel(viewController, config)));
		root.addChildEntry(renderingNode);
		root.addChildEntry(balloonNode);
		root.addChildEntry(new DefaultPreferencesEntry(new AltitudePanel(config)));
	}
}
