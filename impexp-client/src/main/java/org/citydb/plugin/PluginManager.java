/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.citydb.config.project.plugin.PluginConfig;
import org.citydb.plugin.extension.config.ConfigExtension;
import org.citydb.plugin.extension.menu.MenuExtension;
import org.citydb.plugin.extension.preferences.PreferencesExtension;
import org.citydb.plugin.extension.view.ViewExtension;

public class PluginManager {
	private static PluginManager instance;
	private final List<InternalPlugin> internalPlugins;
	private final List<Plugin> externalPlugins;

	private PluginManager() {
		internalPlugins = new ArrayList<InternalPlugin>();
		externalPlugins = new ArrayList<Plugin>();
	}
	
	public static synchronized PluginManager getInstance() {
		if (instance == null)
			instance = new PluginManager();
		
		return instance;
	}

	public void loadPlugins(ClassLoader loader) {
		ServiceLoader<Plugin> pluginLoader = ServiceLoader.load(Plugin.class, loader);
		for (Iterator<Plugin> iter = pluginLoader.iterator(); iter.hasNext(); )
			registerExternalPlugin(iter.next());
	}

	public void registerInternalPlugin(InternalPlugin plugin) {
		internalPlugins.add(plugin);
	}

	public void registerExternalPlugin(Plugin plugin) {
		externalPlugins.add(plugin);
	}

	public List<InternalPlugin> getInternalPlugins() {
		return internalPlugins;
	}

	public List<Plugin> getExternalPlugins() {
		return externalPlugins;
	}

	@SuppressWarnings("unchecked")
	public <T extends InternalPlugin> T getInternalPlugin(Class<T> pluginClass) {
		for (InternalPlugin plugin : internalPlugins)
			if (pluginClass.isInstance(plugin))
				return (T)plugin;

		return null;
	}

	public List<ViewExtension> getExternalViewExtensions() {
		List<ViewExtension> viewExtensions = new ArrayList<ViewExtension>();
		for (Plugin plugin : externalPlugins) {
			if (plugin instanceof ViewExtension) {
				ViewExtension viewExtension = (ViewExtension)plugin;
				if (viewExtension.getView() != null && 
						viewExtension.getView().getViewComponent() != null)
					viewExtensions.add((ViewExtension)plugin);
			}
		}

		return viewExtensions;
	}

	public List<PreferencesExtension> getExternalPreferencesExtensions() {
		List<PreferencesExtension> preferencesExtensions = new ArrayList<PreferencesExtension>();
		for (Plugin plugin : externalPlugins) {
			if (plugin instanceof PreferencesExtension) {
				PreferencesExtension preferencesExtension = (PreferencesExtension)plugin;
				if (preferencesExtension.getPreferences() != null && 
						preferencesExtension.getPreferences().getPreferencesEntry() != null)
					preferencesExtensions.add((PreferencesExtension)plugin);
			}
		}

		return preferencesExtensions;
	}

	public List<MenuExtension> getExternalMenuExtensions() {
		List<MenuExtension> menuExtensions = new ArrayList<MenuExtension>();
		for (Plugin plugin : externalPlugins) {
			if (plugin instanceof MenuExtension) {
				MenuExtension menuExtension = (MenuExtension)plugin;
				if (menuExtension.getMenu() != null && 
						menuExtension.getMenu().getMenuComponent() != null)
					menuExtensions.add((MenuExtension)plugin);
			}
		}

		return menuExtensions;
	}
	
	@SuppressWarnings("unchecked")
	public List<ConfigExtension<? extends PluginConfig>> getExternalConfigExtensions() {
		List<ConfigExtension<? extends PluginConfig>> configExtensions = new ArrayList<ConfigExtension<? extends PluginConfig>>();
		for (Plugin plugin : externalPlugins)
			if (plugin instanceof ConfigExtension<?>)
				configExtensions.add((ConfigExtension<? extends PluginConfig>)plugin);
		
		return configExtensions;
	}

	public List<Plugin> getPlugins() {
		List<Plugin> plugins = new ArrayList<Plugin>(externalPlugins);
		plugins.addAll(internalPlugins);

		return plugins;
	}

}
