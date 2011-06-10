package de.tub.citydb.modules.preferences.gui.preferences;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;
import de.tub.citydb.gui.preferences.NullComponent;

public class GeneralPreferencesEntry extends DefaultPreferencesEntry {

	public GeneralPreferencesEntry() {
		super(NullComponent.getInstance());
	}
	
	@Override
	public String getLocalizedTitle() {
		return Internal.I18N.getString("pref.tree.general");
	}

}
