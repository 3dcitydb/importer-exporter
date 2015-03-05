/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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
package org.citydb.modules.preferences;

import java.util.Locale;

import org.citydb.api.plugin.extension.preferences.Preferences;
import org.citydb.api.plugin.extension.preferences.PreferencesExtension;
import org.citydb.api.plugin.extension.view.View;
import org.citydb.api.plugin.extension.view.ViewExtension;
import org.citydb.config.Config;
import org.citydb.gui.ImpExpGui;
import org.citydb.modules.preferences.gui.preferences.GeneralPreferences;
import org.citydb.modules.preferences.gui.view.PreferencesPanel;
import org.citydb.modules.preferences.gui.view.PreferencesView;
import org.citydb.plugin.InternalPlugin;
import org.citydb.plugin.PluginService;

public class PreferencesPlugin implements InternalPlugin, ViewExtension, PreferencesExtension {
	private PreferencesView view;
	private GeneralPreferences preferences;
	
	public PreferencesPlugin(PluginService pluginService, Config config, ImpExpGui mainView) {
		view = new PreferencesView(pluginService, config, mainView);
		preferences = ((PreferencesPanel)view.getViewComponent()).getGeneralPreferences();
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
		preferences.loadSettings();
	}

	@Override
	public void setSettings() {
		preferences.setSettings();
	}
	
	public void setLoggingSettings() {
		preferences.setLogginSettings();
	}
	
	public boolean requestChange() {
		return view.requestChange();
	}
	
}
