package de.tub.citydb.components.database;

import java.util.Locale;

import javax.xml.bind.JAXBContext;

import de.tub.citydb.components.database.gui.preferences.DatabasePreferences;
import de.tub.citydb.components.database.gui.view.DatabaseView;
import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.plugin.api.extension.preferences.Preferences;
import de.tub.citydb.plugin.api.extension.preferences.PreferencesExtension;
import de.tub.citydb.plugin.api.extension.view.View;
import de.tub.citydb.plugin.api.extension.view.ViewExtension;
import de.tub.citydb.plugin.internal.InternalPlugin;

public class DatabasePlugin implements InternalPlugin, ViewExtension, PreferencesExtension {
	private DatabaseView view;
	private DatabasePreferences preferences;
	
	public DatabasePlugin(JAXBContext projectContext, Config config, ImpExpGui mainView) {
		view = new DatabaseView(config, mainView);
		preferences = new DatabasePreferences(projectContext, mainView, config);
	}
		
	@Override
	public void init() {
		view.loadSettings();
		preferences.loadSettings();
	}

	@Override
	public void shutdown() {
		view.setSettings();
		preferences.setSettings();
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
	
}
