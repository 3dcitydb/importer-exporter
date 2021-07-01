package org.citydb.gui.operation.preferences.plugin;

import org.citydb.config.Config;
import org.citydb.gui.operation.common.DefaultPreferences;

public class PluginsPreferences extends DefaultPreferences {

    protected PluginsPreferences(PluginsOverviewPlugin plugin, Config config) {
        super(new PluginsOverviewEntry(plugin, config));
    }
}
