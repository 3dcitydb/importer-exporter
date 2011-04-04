package de.tub.citydb.components.citygml.importer.gui.preferences;

import de.tub.citydb.components.citygml.importer.gui.preferences.components.AppearancePanel;
import de.tub.citydb.components.citygml.importer.gui.preferences.components.BoundingBoxPanel;
import de.tub.citydb.components.citygml.importer.gui.preferences.components.ContinuationPanel;
import de.tub.citydb.components.citygml.importer.gui.preferences.components.GeometryPanel;
import de.tub.citydb.components.citygml.importer.gui.preferences.components.IdHandlingPanel;
import de.tub.citydb.components.citygml.importer.gui.preferences.components.ResourcesPanel;
import de.tub.citydb.components.citygml.importer.gui.preferences.components.XMLValidationPanel;
import de.tub.citydb.components.citygml.importer.gui.preferences.entries.CityGMLImportEntry;
import de.tub.citydb.config.Config;
import de.tub.citydb.filter.FilterMode;
import de.tub.citydb.gui.preferences.AbstractPreferences;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;

public class CityGMLImportPreferences extends AbstractPreferences {
	
	public CityGMLImportPreferences(Config config) {
		super(new CityGMLImportEntry());
		
		entry.addChildEntry(new DefaultPreferencesEntry(new ContinuationPanel(config)));
		entry.addChildEntry(new DefaultPreferencesEntry(new IdHandlingPanel(config)));
		entry.addChildEntry(new DefaultPreferencesEntry(new BoundingBoxPanel(config)));
		entry.addChildEntry(new DefaultPreferencesEntry(new AppearancePanel(config)));
		entry.addChildEntry(new DefaultPreferencesEntry(new GeometryPanel(FilterMode.IMPORT, config)));
		entry.addChildEntry(new DefaultPreferencesEntry(new XMLValidationPanel(config)));
		entry.addChildEntry(new DefaultPreferencesEntry(new ResourcesPanel(config)));
	}

}
