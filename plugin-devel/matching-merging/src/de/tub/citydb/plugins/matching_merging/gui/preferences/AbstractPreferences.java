package de.tub.citydb.plugins.matching_merging.gui.preferences;

import de.tub.citydb.api.plugin.extension.preferences.Preferences;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesEntry;

public abstract class AbstractPreferences implements Preferences {
	protected PreferencesEntry root;
	
	protected AbstractPreferences(PreferencesEntry entry) {
		this.root = entry;
	}
	
	@Override
	public final PreferencesEntry getPreferencesEntry() {
		return root;
	}
	
	public final void switchLocale() {
		for (PreferencesEntry childEntry : root.getChildEntries())
			((DefaultPreferencesEntry)childEntry).switchLocale();
	}
	
	public final void loadSettings() {
		for (PreferencesEntry childEntry : root.getChildEntries())
			((DefaultPreferencesEntry)childEntry).getViewComponent().loadSettings();
	}
	
	public final void setSettings() {
		for (PreferencesEntry childEntry : root.getChildEntries())
			((DefaultPreferencesEntry)childEntry).getViewComponent().setSettings();
	}

}
