package de.tub.citydb.gui.preferences;

import de.tub.citydb.plugin.api.extension.preferences.Preferences;
import de.tub.citydb.plugin.api.extension.preferences.PreferencesEntry;

public class AbstractPreferences implements Preferences {
	protected DefaultPreferencesEntry entry;
	
	protected AbstractPreferences(DefaultPreferencesEntry entry) {
		this.entry = entry;
	}
	
	@Override
	public final DefaultPreferencesEntry getPreferencesEntry() {
		return entry;
	}
	
	public final void doTranslation() {
		entry.doTranslation();
		
		for (PreferencesEntry childEntry : entry.getChildEntries())
			((DefaultPreferencesEntry)childEntry).doTranslation();
	}
	
	public final void loadSettings() {
		entry.getViewComponent().loadSettings();
		
		for (PreferencesEntry childEntry : entry.getChildEntries())
			((DefaultPreferencesEntry)childEntry).getViewComponent().loadSettings();
	}
	
	public final void setSettings() {
		entry.getViewComponent().setSettings();
		
		for (PreferencesEntry childEntry : entry.getChildEntries())
			((DefaultPreferencesEntry)childEntry).getViewComponent().setSettings();
	}

}
