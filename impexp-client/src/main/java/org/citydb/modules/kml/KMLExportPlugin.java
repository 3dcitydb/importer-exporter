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
package org.citydb.modules.kml;

import java.util.Locale;

import javax.xml.bind.JAXBContext;

import org.citydb.config.Config;
import org.citydb.modules.kml.gui.preferences.KMLExportPreferences;
import org.citydb.modules.kml.gui.view.KMLExportView;
import org.citydb.plugin.InternalPlugin;
import org.citydb.plugin.extension.preferences.Preferences;
import org.citydb.plugin.extension.preferences.PreferencesExtension;
import org.citydb.plugin.extension.view.View;
import org.citydb.plugin.extension.view.ViewController;
import org.citydb.plugin.extension.view.ViewExtension;

public class KMLExportPlugin implements InternalPlugin, ViewExtension, PreferencesExtension {
	private KMLExportView view;
	private KMLExportPreferences preferences;
	
	public KMLExportPlugin(ViewController viewController, JAXBContext kmlContext, JAXBContext colladaContext, Config config) {
		view = new KMLExportView(viewController, kmlContext, colladaContext, config);
		preferences = new KMLExportPreferences(viewController, config);
	}
		
	@Override
	public void init(ViewController viewController, Locale locale) {
		loadSettings();
	}

	@Override
	public void shutdown() {
		setSettings();
	}

	@Override
	public void switchLocale(Locale newLocale) {
		view.doTranslation();
		preferences.doTranslation();
	}

	@Override
	public Preferences getPreferences() {
		return preferences;
	}

	@Override
	public View getView() {
		return view;
	}
	
	@Override
	public void loadSettings() {
		view.loadSettings();
		preferences.loadSettings();
	}

	@Override
	public void setSettings() {
		view.setSettings();
		preferences.setSettings();
	}
	
}
