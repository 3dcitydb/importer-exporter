package de.tub.citydb.plugins.matching_merging.gui.preferences;

import de.tub.citydb.plugins.matching_merging.PluginImpl;

public class MatchingPreferences extends AbstractPreferences {
	
	public MatchingPreferences(PluginImpl plugin) {
		super(new MatchingEntry());
		
		root.addChildEntry(new DefaultPreferencesEntry(new MasterPanel(plugin)));
		root.addChildEntry(new DefaultPreferencesEntry(new CandidatePanel(plugin)));
	}
	
}
