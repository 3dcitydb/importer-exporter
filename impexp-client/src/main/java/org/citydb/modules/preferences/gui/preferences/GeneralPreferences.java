/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2018
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
package org.citydb.modules.preferences.gui.preferences;

import org.citydb.config.Config;
import org.citydb.gui.ImpExpGui;
import org.citydb.gui.preferences.AbstractPreferences;
import org.citydb.gui.preferences.DefaultPreferencesEntry;

public class GeneralPreferences extends AbstractPreferences {
	private final LoggingPanel loggingPanel;
	
	public GeneralPreferences(ImpExpGui mainView, Config config) {
		super(new GeneralPreferencesEntry());
		
		loggingPanel = new LoggingPanel(mainView, config);
		root.addChildEntry(new DefaultPreferencesEntry(new CachePanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new PathPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new ProxyPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new APIKeysPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(loggingPanel));
		root.addChildEntry(new DefaultPreferencesEntry(new LanguagePanel(mainView, config)));
	}

	public void setLogginSettings() {
		loggingPanel.setSettings();
	}

}
