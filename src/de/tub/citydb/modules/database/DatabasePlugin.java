package de.tub.citydb.modules.database;

import java.util.Locale;

import de.tub.citydb.api.plugin.extension.preferences.Preferences;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesExtension;
import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.api.plugin.extension.view.ViewExtension;
import de.tub.citydb.config.Config;
import de.tub.citydb.database.ConnectionViewHandler;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.modules.database.gui.preferences.DatabasePreferences;
import de.tub.citydb.modules.database.gui.view.DatabaseView;
import de.tub.citydb.plugin.InternalPlugin;

public class DatabasePlugin implements InternalPlugin, ViewExtension, PreferencesExtension {
	private DatabaseView view;
	private DatabasePreferences preferences;
	
	public DatabasePlugin(Config config, ImpExpGui mainView) {
		view = new DatabaseView(config, mainView);
		preferences = new DatabasePreferences(config, mainView);
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
	
	public ConnectionViewHandler getConnectionViewHandler() {
		return (ConnectionViewHandler)view.getViewComponent();
	}
	
}
