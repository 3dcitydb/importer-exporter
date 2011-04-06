package de.tub.citydb.components.matching;

import java.util.Locale;

import de.tub.citydb.components.matching.gui.preferences.MatchingPreferences;
import de.tub.citydb.components.matching.gui.view.MatchingView;
import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.plugin.api.extensions.preferences.Preferences;
import de.tub.citydb.plugin.api.extensions.preferences.PreferencesExtension;
import de.tub.citydb.plugin.api.extensions.view.View;
import de.tub.citydb.plugin.api.extensions.view.ViewExtension;
import de.tub.citydb.plugin.internal.InternalPlugin;

public class MatchingPlugin implements InternalPlugin, ViewExtension, PreferencesExtension {
	private MatchingView view;
	private MatchingPreferences preferences;
	
	public MatchingPlugin(Config config, ImpExpGui mainView) {
		view = new MatchingView(config, mainView);
		preferences = new MatchingPreferences(config);
	}
		
	@Override
	public void init() {
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
