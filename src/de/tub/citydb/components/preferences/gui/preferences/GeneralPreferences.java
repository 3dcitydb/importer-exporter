package de.tub.citydb.components.preferences.gui.preferences;

import de.tub.citydb.components.preferences.gui.preferences.components.LanguagePanel;
import de.tub.citydb.components.preferences.gui.preferences.components.LoggingPanel;
import de.tub.citydb.components.preferences.gui.preferences.components.PathPanel;
import de.tub.citydb.components.preferences.gui.preferences.entries.GeneralPreferencesEntry;
import de.tub.citydb.config.Config;
import de.tub.citydb.gui.ImpExpGui;
import de.tub.citydb.gui.preferences.AbstractPreferences;
import de.tub.citydb.gui.preferences.DefaultPreferencesEntry;

public class GeneralPreferences extends AbstractPreferences {
	private LoggingPanel loggingPanel;
	
	public GeneralPreferences(ImpExpGui mainView, Config config) {
		super(new GeneralPreferencesEntry());
		
		loggingPanel = new LoggingPanel(config, mainView);
		entry.addChildEntry(new DefaultPreferencesEntry(loggingPanel));
		entry.addChildEntry(new DefaultPreferencesEntry(new PathPanel(config)));
		entry.addChildEntry(new DefaultPreferencesEntry(new LanguagePanel(config, mainView)));
	}
	
	public LoggingPanel getLoggingPanel() {
		return loggingPanel;
	}

}
