package de.tub.citydb.components.citygml.exporter.gui.preferences.entries;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;
import de.tub.citydb.gui.preferences.NullComponent;

public class CityGMLExportEntry extends DefaultPreferencesEntry {

	public CityGMLExportEntry() {
		super(NullComponent.getInstance());
	}
	
	@Override
	public String getTitle() {
		return Internal.I18N.getString("pref.tree.export");
	}

}
