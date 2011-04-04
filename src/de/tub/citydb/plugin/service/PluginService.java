package de.tub.citydb.plugin.service;

import java.util.Iterator;

import de.tub.citydb.plugin.api.Plugin;

public interface PluginService {
	public Iterator<Plugin> getPlugins();
	public void initPlugins();
	public void shutdownPlugins();
}
