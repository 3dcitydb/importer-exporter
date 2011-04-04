package de.tub.citydb.components.citygml.exporter.gui.preferences;

import de.tub.citydb.components.citygml.exporter.gui.preferences.components.AppearancePanel;
import de.tub.citydb.components.citygml.exporter.gui.preferences.components.BoundingBoxPanel;
import de.tub.citydb.components.citygml.exporter.gui.preferences.components.ResourcesPanel;
import de.tub.citydb.components.citygml.exporter.gui.preferences.components.VersionPanel;
import de.tub.citydb.components.citygml.exporter.gui.preferences.components.XLinkPanel;
import de.tub.citydb.components.citygml.exporter.gui.preferences.entries.CityGMLExportEntry;
import de.tub.citydb.config.Config;
import de.tub.citydb.gui.preferences.AbstractPreferences;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;

public class CityGMLExportPreferences extends AbstractPreferences {
	
	public CityGMLExportPreferences(Config config) {
		super(new CityGMLExportEntry());
		
		entry.addChildEntry(new DefaultPreferencesEntry(new VersionPanel(config)));
		entry.addChildEntry(new DefaultPreferencesEntry(new BoundingBoxPanel(config)));
		entry.addChildEntry(new DefaultPreferencesEntry(new AppearancePanel(config)));
		entry.addChildEntry(new DefaultPreferencesEntry(new XLinkPanel(config)));
		entry.addChildEntry(new DefaultPreferencesEntry(new ResourcesPanel(config)));
	}

}
