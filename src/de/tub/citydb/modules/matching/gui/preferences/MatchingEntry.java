package de.tub.citydb.modules.matching.gui.preferences;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;
import de.tub.citydb.gui.preferences.NullComponent;

public class MatchingEntry extends DefaultPreferencesEntry {

	public MatchingEntry() {
		super(NullComponent.getInstance());
	}
	
	@Override
	public String getLocalizedTitle() {
		return Internal.I18N.getString("pref.tree.matching");
	}

}
