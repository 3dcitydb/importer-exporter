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
package org.citydb.modules.citygml.exporter;

import java.util.Locale;

import org.citydb.api.plugin.extension.preferences.Preferences;
import org.citydb.api.plugin.extension.preferences.PreferencesExtension;
import org.citydb.api.plugin.extension.view.View;
import org.citydb.api.plugin.extension.view.ViewExtension;
import org.citydb.config.Config;
import org.citydb.gui.ImpExpGui;
import org.citydb.modules.citygml.exporter.gui.preferences.CityGMLExportPreferences;
import org.citydb.modules.citygml.exporter.gui.view.CityGMLExportView;
import org.citydb.plugin.InternalPlugin;
import org.citygml4j.builder.jaxb.JAXBBuilder;

public class CityGMLExportPlugin implements InternalPlugin, ViewExtension, PreferencesExtension {
	private CityGMLExportView view;
	private CityGMLExportPreferences preferences;
	
	public CityGMLExportPlugin(JAXBBuilder jaxbBuilder, Config config, ImpExpGui mainView) {
		view = new CityGMLExportView(jaxbBuilder, config, mainView);
		preferences = new CityGMLExportPreferences(config);
	}
		
	@Override
	public void init(Locale locale) {
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
