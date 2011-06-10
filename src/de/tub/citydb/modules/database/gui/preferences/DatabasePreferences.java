package de.tub.citydb.modules.database.gui.preferences;

import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.preferences.AbstractPreferences;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;

public class DatabasePreferences extends AbstractPreferences {
	
	public DatabasePreferences(Config config, ImpExpGui mainView) {
		super(new DatabaseEntry());
		
		root.addChildEntry(new DefaultPreferencesEntry(new IndexPanel(config, mainView)));
		root.addChildEntry(new DefaultPreferencesEntry(new SrsPanel(config, mainView)));
	}

}
