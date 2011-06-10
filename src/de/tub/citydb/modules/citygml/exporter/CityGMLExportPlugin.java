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
