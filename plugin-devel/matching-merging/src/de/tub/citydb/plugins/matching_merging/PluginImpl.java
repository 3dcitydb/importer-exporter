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
package de.tub.citydb.plugins.matching_merging;

import java.util.Locale;
import java.util.ResourceBundle;

import de.tub.citydb.api.controller.ApplicationStarter;
import de.tub.citydb.api.plugin.Plugin;
import de.tub.citydb.api.plugin.extension.config.ConfigExtension;
import de.tub.citydb.api.plugin.extension.config.PluginConfigEvent;
import de.tub.citydb.api.plugin.extension.preferences.Preferences;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesExtension;
import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.api.plugin.extension.view.ViewExtension;
import de.tub.citydb.plugins.matching_merging.config.ConfigImpl;
import de.tub.citydb.plugins.matching_merging.gui.preferences.MatchingPreferences;
import de.tub.citydb.plugins.matching_merging.gui.view.MatchingView;
import de.tub.citydb.plugins.matching_merging.util.Util;

public class PluginImpl implements Plugin, ViewExtension, PreferencesExtension, ConfigExtension<ConfigImpl> {
	private ConfigImpl config;
	private MatchingView view;
	private MatchingPreferences preferences;
	
	private Locale currentLocale;
	
	public static void main(String[] args) {
		// just for testing...
		ApplicationStarter starter = new ApplicationStarter();
		starter.run(args, new PluginImpl());
	}
	
	@Override
	public void init(Locale locale) {
		view = new MatchingView(this);
		preferences = new MatchingPreferences(this);
		
		loadSettings();	
		switchLocale(locale);
	}

	@Override
	public void shutdown() {
		setSettings();
	}

	@Override
	public void switchLocale(Locale locale) {
		if (locale.equals(currentLocale))
			return;
		
		Util.I18N = ResourceBundle.getBundle("de.tub.citydb.plugins.matching_merging.gui.locale", locale);
		currentLocale = locale;
		
		view.switchLocale();
		preferences.switchLocale();
	}
	
	@Override
	public ConfigImpl getConfig() {
		return config;
	}

	public void setConfig(ConfigImpl config) {
		this.config = config;
	}
	
	@Override
	public void handleEvent(PluginConfigEvent event) {
		switch (event) {
		case RESET_DEFAULT_CONFIG:
			this.config = new ConfigImpl();
			loadSettings();
			break;
		case PRE_SAVE_CONFIG:
			setSettings();
			break;
		}
	}

	@Override
	public void configLoaded(ConfigImpl config) {
		boolean reload = this.config != null;		
		setConfig(config);
		
		if (reload)
			loadSettings();
	}

	@Override
	public Preferences getPreferences() {
		return preferences;
	}

	@Override
	public View getView() {
		return view;
	}
	
	public void loadSettings() {
		view.loadSettings();
		preferences.loadSettings();
	}
	
	public void setSettings() {
		view.setSettings();
		preferences.setSettings();
	}

}
