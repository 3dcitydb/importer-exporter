package de.tub.citydb.components.citygml.importer.gui.preferences;

import de.tub.citydb.components.common.filter.FilterMode;
import de.tub.citydb.config.Config;
import de.tub.citydb.gui.preferences.AbstractPreferences;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;

public class CityGMLImportPreferences extends AbstractPreferences {
	
	public CityGMLImportPreferences(Config config) {
		super(new CityGMLImportEntry());
		
		root.addChildEntry(new DefaultPreferencesEntry(new ContinuationPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new IdHandlingPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new BoundingBoxPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new AppearancePanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new GeometryPanel(FilterMode.IMPORT, config)));
		root.addChildEntry(new DefaultPreferencesEntry(new XMLValidationPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new ResourcesPanel(config)));
	}

}
