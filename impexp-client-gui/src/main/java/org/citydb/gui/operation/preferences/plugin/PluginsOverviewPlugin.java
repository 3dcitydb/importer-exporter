package org.citydb.gui.operation.preferences.plugin;

import org.citydb.config.Config;
import org.citydb.gui.plugin.preferences.Preferences;
import org.citydb.gui.plugin.preferences.PreferencesExtension;
import org.citydb.gui.plugin.view.ViewController;
import org.citydb.core.plugin.internal.InternalPlugin;

import java.util.Locale;

public class PluginsOverviewPlugin extends InternalPlugin implements PreferencesExtension {
    private final PluginsPreferences preferences;
    private volatile boolean isShuttingDown;

    public PluginsOverviewPlugin(Config config) {
        preferences = new PluginsPreferences(this, config);
    }

    @Override
    public void initGuiExtension(ViewController viewController, Locale locale) {
        loadSettings();
    }

    @Override
    public void shutdown() {
        isShuttingDown = true;
        setSettings();
    }

    boolean isShuttingDown() {
        return isShuttingDown;
    }

    @Override
    public void switchLocale(Locale locale) {
        preferences.doTranslation();
    }

    @Override
    public Preferences getPreferences() {
        return preferences;
    }

    @Override
    public void loadSettings() {
        preferences.loadSettings();
    }

    @Override
    public void setSettings() {
        preferences.setSettings();
    }
}
