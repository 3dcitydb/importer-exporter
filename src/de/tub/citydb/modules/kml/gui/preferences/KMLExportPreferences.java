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
package de.tub.citydb.modules.kml.gui.preferences;

import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.preferences.AbstractPreferences;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;

public class KMLExportPreferences extends AbstractPreferences {
	
	public KMLExportPreferences(ImpExpGui mainView, Config config) {
		super(new KMLExportEntry());
		
		DefaultPreferencesEntry renderingNode = new RenderingPanel();
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new BuildingRenderingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new VegetationRenderingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new CityFurnitureRenderingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new GenericCityObjectRenderingPanel(config)));
		renderingNode.addChildEntry(new DefaultPreferencesEntry(new CityObjectGroupRenderingPanel(config)));

		DefaultPreferencesEntry balloonNode = new BalloonPanel();
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new BuildingBalloonPanel(config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new VegetationBalloonPanel(config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new CityFurnitureBalloonPanel(config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new GenericCityObjectBalloonPanel(config)));
		balloonNode.addChildEntry(new DefaultPreferencesEntry(new CityObjectGroupBalloonPanel(config)));

		root.addChildEntry(new DefaultPreferencesEntry(new GeneralPanel(config)));
		root.addChildEntry(renderingNode);
		root.addChildEntry(balloonNode);
		root.addChildEntry(new DefaultPreferencesEntry(new AltitudePanel(config)));
	}

}
