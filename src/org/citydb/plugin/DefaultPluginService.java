/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.citydb.api.plugin.Plugin;
import org.citydb.api.plugin.extension.config.ConfigExtension;
import org.citydb.api.plugin.extension.config.PluginConfig;
import org.citydb.api.plugin.extension.menu.MenuExtension;
import org.citydb.api.plugin.extension.preferences.PreferencesExtension;
import org.citydb.api.plugin.extension.view.ViewExtension;

public class DefaultPluginService implements PluginService {
	private static DefaultPluginService pluginService;
	private final ClassLoader loader;
	private final List<InternalPlugin> internalPlugins;
	private final List<Plugin> externalPlugins;

	private DefaultPluginService(ClassLoader loader) throws ServiceConfigurationError {
		this.loader = loader;
		internalPlugins = new ArrayList<InternalPlugin>();
		externalPlugins = new ArrayList<Plugin>();
	}

	public static synchronized DefaultPluginService getInstance(ClassLoader loader) throws ServiceConfigurationError {
		if (pluginService == null)
			pluginService = new DefaultPluginService(loader);

		return pluginService;
	}

	@Override
	public void loadPlugins() {
		ServiceLoader<Plugin> pluginLoader = ServiceLoader.load(Plugin.class, loader);
		for (Iterator<Plugin> iter = pluginLoader.iterator(); iter.hasNext(); )
			registerExternalPlugin(iter.next());
	}

	@Override
	public void registerInternalPlugin(InternalPlugin plugin) {
		internalPlugins.add(plugin);
	}

	@Override
	public void registerExternalPlugin(Plugin plugin) {
		externalPlugins.add(plugin);
	}

	@Override
	public List<InternalPlugin> getInternalPlugins() {
		return internalPlugins;
	}

	@Override
	public List<Plugin> getExternalPlugins() {
		return externalPlugins;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends InternalPlugin> T getInternalPlugin(Class<T> pluginClass) {
		for (InternalPlugin plugin : internalPlugins)
			if (pluginClass.isInstance(plugin))
				return (T)plugin;

		return null;
	}

	@Override
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

	@Override
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

	@Override
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
	@Override
	public List<ConfigExtension<? extends PluginConfig>> getExternalConfigExtensions() {
		List<ConfigExtension<? extends PluginConfig>> configExtensions = new ArrayList<ConfigExtension<? extends PluginConfig>>();
		for (Plugin plugin : externalPlugins)
			if (plugin instanceof ConfigExtension<?>)
				configExtensions.add((ConfigExtension<? extends PluginConfig>)plugin);
		
		return configExtensions;
	}

	@Override
	public List<Plugin> getPlugins() {
		List<Plugin> plugins = new ArrayList<Plugin>(externalPlugins);
		plugins.addAll(internalPlugins);

		return plugins;
	}

}
