package org.citydb.gui.operation.preferences.plugin;

import org.citydb.config.Config;
import org.citydb.core.plugin.internal.InternalPlugin;
import org.citydb.core.registry.ObjectRegistry;
import org.citydb.gui.plugin.preferences.Preferences;
import org.citydb.gui.plugin.preferences.PreferencesExtension;
import org.citydb.gui.plugin.view.ViewController;

import java.util.Locale;

public class PluginsOverviewPlugin extends InternalPlugin implements PreferencesExtension {
    private PluginsPreferences preferences;
    private volatile boolean isShuttingDown;

    @Override
    public void initGuiExtension(ViewController viewController, Locale locale) {
        Config config = ObjectRegistry.getInstance().getConfig();
        preferences = new PluginsPreferences(this, config);
        loadSettings();
    }

    @Override
    public void shutdownGui() {
        isShuttingDown = true;
        setSettings();
    }

    boolean isShuttingDown() {
        return isShuttingDown;
    }

    @Override
    public void switchLocale(Locale locale) {
        preferences.switchLocale(locale);
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
