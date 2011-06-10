package de.tub.citydb.modules.kml;

import java.util.Locale;

import javax.xml.bind.JAXBContext;

import de.tub.citydb.api.plugin.extension.preferences.Preferences;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesExtension;
import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.api.plugin.extension.view.ViewExtension;
import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.modules.kml.gui.preferences.KMLExportPreferences;
import de.tub.citydb.modules.kml.gui.view.KMLExportView;
import de.tub.citydb.plugin.InternalPlugin;

public class KMLExportPlugin implements InternalPlugin, ViewExtension, PreferencesExtension {
	private KMLExportView view;
	private KMLExportPreferences preferences;
	
	public KMLExportPlugin(JAXBContext kmlContext, JAXBContext colladaContext, Config config, ImpExpGui mainView) {
		view = new KMLExportView(kmlContext, colladaContext, config, mainView);
		preferences = new KMLExportPreferences(mainView, config);
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
