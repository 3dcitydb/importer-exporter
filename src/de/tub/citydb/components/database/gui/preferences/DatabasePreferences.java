package de.tub.citydb.components.database.gui.preferences;

import javax.xml.bind.JAXBContext;

import de.tub.citydb.components.database.gui.preferences.components.IndexPanel;
import de.tub.citydb.components.database.gui.preferences.components.SrsPanel;
import de.tub.citydb.components.database.gui.preferences.entries.DatabaseEntry;
import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.preferences.AbstractPreferences;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;

public class DatabasePreferences extends AbstractPreferences {
	
	public DatabasePreferences(JAXBContext projectContext, ImpExpGui mainView, Config config) {
		super(new DatabaseEntry());
		
		entry.addChildEntry(new DefaultPreferencesEntry(new IndexPanel(config, mainView)));
		entry.addChildEntry(new DefaultPreferencesEntry(new SrsPanel(projectContext, config, mainView)));
	}

}
