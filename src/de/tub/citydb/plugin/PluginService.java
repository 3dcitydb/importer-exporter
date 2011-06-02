package de.tub.citydb.plugin;

import java.util.List;

import de.tub.citydb.api.plugin.Plugin;
import de.tub.citydb.api.plugin.extension.config.ConfigExtension;
import de.tub.citydb.api.plugin.extension.config.PluginConfig;
import de.tub.citydb.api.plugin.extension.menu.MenuExtension;
import de.tub.citydb.api.plugin.extension.preferences.PreferencesExtension;
import de.tub.citydb.api.plugin.extension.view.ViewExtension;

public interface PluginService {
	public void registerInternalPlugin(InternalPlugin plugin);
	public void registerExternalPlugin(Plugin plugin);
	public void loadPlugins();
	
	public List<InternalPlugin> getInternalPlugins();
	public <T extends InternalPlugin> T getInternalPlugin(Class<T> pluginClass);
	
	public List<Plugin> getExternalPlugins();
	public List<ViewExtension> getExternalViewExtensions();
	public List<PreferencesExtension> getExternalPreferencesExtensions();
	public List<MenuExtension> getExternalMenuExtensions();
	public List<ConfigExtension<? extends PluginConfig>> getExternalConfigExtensions();
	
	public List<Plugin> getPlugins();
}
