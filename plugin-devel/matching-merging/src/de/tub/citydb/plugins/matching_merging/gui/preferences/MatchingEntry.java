package de.tub.citydb.plugins.matching_merging.gui.preferences;

import de.tub.citydb.plugins.matching_merging.PluginImpl;
import de.tub.citydb.plugins.matching_merging.util.Util;

public class MatchingEntry extends DefaultPreferencesEntry {

	@SuppressWarnings("serial")
	public MatchingEntry(PluginImpl plugin) {
		super(new AbstractPreferencesComponent(plugin) {
			
			@Override
			public void switchLocale() {
			}
			
			@Override
			public void setSettings() {
			}
			
			@Override
			public void loadSettings() {
			}
			
			@Override
			public boolean isModified() {
				return false;
			}
			
			@Override
			public String getTitle() {
				return null;
			}
		});
	}
	
	@Override
	public String getLocalizedTitle() {
		return Util.I18N.getString("pref.tree.matching");
	}

}
