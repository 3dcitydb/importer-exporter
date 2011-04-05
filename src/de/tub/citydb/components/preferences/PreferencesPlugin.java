package de.tub.citydb.components.preferences;

import java.util.Locale;

import de.tub.citydb.components.preferences.gui.preferences.GeneralPreferences;
import de.tub.citydb.components.preferences.gui.view.PreferencesView;
import de.tub.citydb.components.preferences.gui.view.components.PreferencesPanel;
import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.plugin.api.extension.preferences.Preferences;
import de.tub.citydb.plugin.api.extension.preferences.PreferencesExtension;
import de.tub.citydb.plugin.api.extension.view.View;
import de.tub.citydb.plugin.api.extension.view.ViewExtension;
import de.tub.citydb.plugin.internal.InternalPlugin;
import de.tub.citydb.plugin.service.PluginService;

public class PreferencesPlugin implements InternalPlugin, ViewExtension, PreferencesExtension {
	private PreferencesView view;
	private GeneralPreferences preferences;
	
	public PreferencesPlugin(PluginService pluginService, Config config, ImpExpGui mainView) {
		view = new PreferencesView(pluginService, config, mainView);
		preferences = new GeneralPreferences(mainView, config);
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
	
	public boolean requestChange() {
		return ((PreferencesPanel)view.getViewComponent()).requestChange();
	}
	
}
