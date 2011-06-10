package de.tub.citydb.api.plugin.extension.preferences;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

public abstract class PreferencesEntry {
	private List<PreferencesEntry> childEntries;
	
	public abstract boolean isModified();
	public abstract boolean handleEvent(PreferencesEvent event);
	public abstract String getLocalizedTitle();
	public abstract Component getViewComponent();

	public void addChildEntry(PreferencesEntry child) {
		if (childEntries == null)
			childEntries = new ArrayList<PreferencesEntry>();
		
		childEntries.add(child);
	}
	
	public List<PreferencesEntry> getChildEntries() {
		if (childEntries == null)
			childEntries = new ArrayList<PreferencesEntry>();
		
		return childEntries;
	}
}
