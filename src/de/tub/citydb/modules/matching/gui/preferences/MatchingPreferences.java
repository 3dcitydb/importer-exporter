package de.tub.citydb.modules.matching.gui.preferences;

import de.tub.citydb.config.Config;
import de.tub.citydb.gui.preferences.AbstractPreferences;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;

public class MatchingPreferences extends AbstractPreferences {
	
	public MatchingPreferences(Config config) {
		super(new MatchingEntry());
		
		root.addChildEntry(new DefaultPreferencesEntry(new MasterPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new CandidatePanel(config)));
	}

}
