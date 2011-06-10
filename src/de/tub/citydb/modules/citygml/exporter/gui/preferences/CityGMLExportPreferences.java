package de.tub.citydb.modules.citygml.exporter.gui.preferences;

import de.tub.citydb.config.Config;
import de.tub.citydb.gui.preferences.AbstractPreferences;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;

public class CityGMLExportPreferences extends AbstractPreferences {
	
	public CityGMLExportPreferences(Config config) {
		super(new CityGMLExportEntry());
		
		root.addChildEntry(new DefaultPreferencesEntry(new VersionPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new BoundingBoxPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new AppearancePanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new XLinkPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new ResourcesPanel(config)));
	}

}
