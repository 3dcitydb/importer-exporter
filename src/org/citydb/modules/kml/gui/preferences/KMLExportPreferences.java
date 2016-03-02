/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.modules.kml.gui.preferences;

import org.citydb.config.Config;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.preferences.AbstractPreferences;
import org.citydb.gui.preferences.DefaultPreferencesEntry;

public class KMLExportPreferences extends AbstractPreferences {
	
	public KMLExportPreferences(ImpExpGui mainView, Config config) {
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

		root.addChildEntry(new DefaultPreferencesEntry(new GeneralPanel(config)));
		root.addChildEntry(renderingNode);
		root.addChildEntry(balloonNode);
		root.addChildEntry(new DefaultPreferencesEntry(new AltitudePanel(config)));
	}

}
