package de.tub.citydb.plugins.matching_merging.gui.preferences;

import java.awt.Component;

import de.tub.citydb.api.plugin.extension.preferences.PreferencesEntry;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesEvent;
import de.tub.citydb.plugins.matching_merging.util.Util;

public class MatchingEntry extends PreferencesEntry {

	@Override
	public String getLocalizedTitle() {
		return Util.I18N.getString("pref.tree.matching");
	}

	@Override
	public boolean isModified() {
		// we do not have content which could be modified by the user
		return false;
	}

	@Override
	public boolean handleEvent(PreferencesEvent event) {
		// we do not handle events
		return true;
	}

	@Override
	public Component getViewComponent() {
		// we do not have content thus return null
		return null;
	}

}
