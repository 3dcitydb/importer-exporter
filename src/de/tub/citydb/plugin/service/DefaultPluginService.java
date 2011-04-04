package de.tub.citydb.plugin.service;

import java.util.Iterator;
import java.util.ServiceLoader;

import de.tub.citydb.plugin.api.Plugin;

public class DefaultPluginService implements PluginService {
	private static DefaultPluginService pluginService;
	private ServiceLoader<Plugin> pluginLoader;
	
	private DefaultPluginService(ClassLoader loader) {
		pluginLoader = ServiceLoader.load(Plugin.class, loader);
	}

	public static synchronized DefaultPluginService getInstance(ClassLoader loader) {
		if (pluginService == null)
			pluginService = new DefaultPluginService(loader);
		
		return pluginService;
	}

	@Override
	public Iterator<Plugin> getPlugins() {
		return pluginLoader.iterator();
	}

	@Override
	public void initPlugins() {
		Iterator<Plugin> iter = getPlugins();
		while (iter.hasNext())
			iter.next().init();
	}

	@Override
	public void shutdownPlugins() {
		Iterator<Plugin> iter = getPlugins();
		while (iter.hasNext())
			iter.next().shutdown();
	} 

}
