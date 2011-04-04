package de.tub.citydb.components.citygml.importer.gui.preferences.entries;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;
import de.tub.citydb.gui.preferences.NullComponent;

public class CityGMLImportEntry extends DefaultPreferencesEntry {

	public CityGMLImportEntry() {
		super(NullComponent.getInstance());
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.import");
	}

}
