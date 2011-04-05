package de.tub.citydb.plugin.service;

import java.util.List;

import de.tub.citydb.plugin.api.Plugin;
import de.tub.citydb.plugin.internal.InternalPlugin;

public interface PluginService {
	public void registerInternalPlugin(InternalPlugin plugin);
	public List<InternalPlugin> getInternalPlugins();
	public <T extends InternalPlugin> T getInternalPlugin(Class<T> pluginClass);
	public List<Plugin> getExternalPlugins();
	public List<Plugin> getPlugins();
	
	public void initPlugins();
	public void shutdownPlugins();
}
