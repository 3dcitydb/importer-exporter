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
package org.citydb.gui.operation.database;

import org.citydb.config.Config;
import org.citydb.core.database.connection.ConnectionViewHandler;
import org.citydb.core.plugin.internal.InternalPlugin;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.operation.database.preferences.DatabasePreferences;
import org.citydb.gui.operation.database.view.DatabaseView;
import org.citydb.gui.plugin.preferences.Preferences;
import org.citydb.gui.plugin.preferences.PreferencesExtension;
import org.citydb.gui.plugin.view.View;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.gui.plugin.view.ViewExtension;

import java.util.Locale;

public class DatabasePlugin extends InternalPlugin implements ViewExtension, PreferencesExtension {
	private DatabaseView view;
	private DatabasePreferences preferences;

	@Override
	public void initGuiExtension(ViewController viewController, Locale locale) {
		Config config = ObjectRegistry.getInstance().getConfig();
		view = new DatabaseView(viewController, config);
		preferences = new DatabasePreferences(viewController, config);
		loadSettings();
	}

	@Override
	public void shutdownGui() {
		setSettings();
	}

	@Override
	public void switchLocale(Locale locale) {
		view.doTranslation();
		preferences.switchLocale(locale);
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
	
	public ConnectionViewHandler getConnectionViewHandler() {
		return (ConnectionViewHandler)view.getViewComponent();
	}
	
}
