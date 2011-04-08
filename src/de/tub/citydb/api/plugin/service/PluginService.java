package de.tub.citydb.api.plugin.service;

import java.util.List;

import de.tub.citydb.api.plugin.api.Plugin;
import de.tub.citydb.api.plugin.api.extension.view.ViewExtension;
import de.tub.citydb.api.plugin.internal.InternalPlugin;

public interface PluginService {
	public void registerInternalPlugin(InternalPlugin plugin);
	public List<InternalPlugin> getInternalPlugins();
	public <T extends InternalPlugin> T getInternalPlugin(Class<T> pluginClass);
	
	public List<Plugin> getExternalPlugins();
	public List<ViewExtension> getExternalViewExtensions(boolean sortByTitle);
	public List<Plugin> getPlugins();
	
	public void initPlugins();
	public void shutdownPlugins();
}
