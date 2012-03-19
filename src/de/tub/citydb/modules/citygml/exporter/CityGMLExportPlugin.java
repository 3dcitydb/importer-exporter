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
package de.tub.citydb.modules.citygml.exporter;

import java.util.Locale;

import org.citygml4j.builder.jaxb.JAXBBuilder;

import de.tub.citydb.api.plugin.extension.preferences.Preferences;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesExtension;
import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.api.plugin.extension.view.ViewExtension;
import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.modules.citygml.exporter.gui.preferences.CityGMLExportPreferences;
import de.tub.citydb.modules.citygml.exporter.gui.view.CityGMLExportView;
import de.tub.citydb.plugin.InternalPlugin;

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
