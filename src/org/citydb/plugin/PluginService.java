/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.plugin;

import java.util.List;

import org.citydb.api.plugin.Plugin;
import org.citydb.api.plugin.extension.config.ConfigExtension;
import org.citydb.api.plugin.extension.config.PluginConfig;
import org.citydb.api.plugin.extension.menu.MenuExtension;
import org.citydb.api.plugin.extension.preferences.PreferencesExtension;
import org.citydb.api.plugin.extension.view.ViewExtension;

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
