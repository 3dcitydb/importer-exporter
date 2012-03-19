/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
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
