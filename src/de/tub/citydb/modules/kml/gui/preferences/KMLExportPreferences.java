package de.tub.citydb.modules.kml.gui.preferences;

import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.preferences.AbstractPreferences;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;

public class KMLExportPreferences extends AbstractPreferences {
	
	public KMLExportPreferences(ImpExpGui mainView, Config config) {
		super(new KMLExportEntry());
		
		root.addChildEntry(new DefaultPreferencesEntry(new GeneralPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new BalloonPanel(config)));
		root.addChildEntry(new DefaultPreferencesEntry(new AltitudePanel(config)));
	}

}
