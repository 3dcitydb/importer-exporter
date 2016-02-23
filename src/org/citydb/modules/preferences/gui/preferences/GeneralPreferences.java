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
package org.citydb.modules.preferences.gui.preferences;

import org.citydb.config.Config;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.preferences.AbstractPreferences;
import org.citydb.gui.preferences.DefaultPreferencesEntry;

public class GeneralPreferences extends AbstractPreferences {
	private final LoggingPanel loggingPanel;
	
	public GeneralPreferences(ImpExpGui mainView, Config config) {
		super(new GeneralPreferencesEntry());
		
		loggingPanel = new LoggingPanel(config, mainView);
		root.addChildEntry(new DefaultPreferencesEntry(new CachePanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new PathPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new ProxyPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(loggingPanel));
		root.addChildEntry(new DefaultPreferencesEntry(new LanguagePanel(config, mainView)));
	}

	public void setLogginSettings() {
		loggingPanel.setSettings();
	}

}
