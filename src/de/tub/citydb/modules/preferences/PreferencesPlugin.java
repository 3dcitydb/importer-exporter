package de.tub.citydb.modules.preferences;

import java.util.Locale;

import de.tub.citydb.api.plugin.extension.preferences.Preferences;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesExtension;
import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.api.plugin.extension.view.ViewExtension;
import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.modules.preferences.gui.preferences.GeneralPreferences;
import de.tub.citydb.modules.preferences.gui.view.PreferencesPanel;
import de.tub.citydb.modules.preferences.gui.view.PreferencesView;
import de.tub.citydb.plugin.InternalPlugin;
import de.tub.citydb.plugin.PluginService;

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
