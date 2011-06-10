package de.tub.citydb.modules.citygml.exporter.gui.preferences;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;
import de.tub.citydb.gui.preferences.NullComponent;

public class CityGMLExportEntry extends DefaultPreferencesEntry {

	public CityGMLExportEntry() {
		super(NullComponent.getInstance());
	}
	
	@Override
	public String getLocalizedTitle() {
		return Internal.I18N.getString("pref.tree.export");
	}

}
