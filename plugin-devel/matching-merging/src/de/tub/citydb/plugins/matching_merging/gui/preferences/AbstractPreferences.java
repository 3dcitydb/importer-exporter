package de.tub.citydb.plugins.matching_merging.gui.preferences;

import de.tub.citydb.api.plugin.extension.preferences.Preferences;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesEntry;

public class AbstractPreferences implements Preferences {
	protected DefaultPreferencesEntry root;
	
	protected AbstractPreferences(DefaultPreferencesEntry entry) {
		this.root = entry;
	}
	
	@Override
	public final DefaultPreferencesEntry getPreferencesEntry() {
		return root;
	}
	
	public final void switchLocale() {
		root.switchLocale();
		
		for (PreferencesEntry childEntry : root.getChildEntries())
			((DefaultPreferencesEntry)childEntry).switchLocale();
	}
	
	public final void loadSettings() {
		root.getViewComponent().loadSettings();
		
		for (PreferencesEntry childEntry : root.getChildEntries())
			((DefaultPreferencesEntry)childEntry).getViewComponent().loadSettings();
	}
	
	public final void setSettings() {
		root.getViewComponent().setSettings();
		
		for (PreferencesEntry childEntry : root.getChildEntries())
			((DefaultPreferencesEntry)childEntry).getViewComponent().setSettings();
	}

}
