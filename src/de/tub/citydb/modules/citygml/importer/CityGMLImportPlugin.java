package de.tub.citydb.modules.citygml.importer;

import java.util.Locale;

import org.citygml4j.builder.jaxb.JAXBBuilder;

import de.tub.citydb.api.plugin.extension.preferences.Preferences;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesExtension;
import de.tub.citydb.api.plugin.extension.view.View;
import de.tub.citydb.api.plugin.extension.view.ViewExtension;
import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.modules.citygml.importer.gui.preferences.CityGMLImportPreferences;
import de.tub.citydb.modules.citygml.importer.gui.view.CityGMLImportView;
import de.tub.citydb.plugin.InternalPlugin;

public class CityGMLImportPlugin implements InternalPlugin, ViewExtension, PreferencesExtension {
	private CityGMLImportView view;
	private CityGMLImportPreferences preferences;
	
	public CityGMLImportPlugin(JAXBBuilder jaxbBuilder, Config config, ImpExpGui mainView) {
		view = new CityGMLImportView(jaxbBuilder, config, mainView);
		preferences = new CityGMLImportPreferences(config);
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
