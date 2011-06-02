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
