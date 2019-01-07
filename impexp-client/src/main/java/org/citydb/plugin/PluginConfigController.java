/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import org.citydb.config.Config;
import org.citydb.config.project.plugin.PluginConfig;
import org.citydb.log.Logger;
import org.citydb.plugin.extension.config.ConfigExtension;

public class PluginConfigController {
	public static PluginConfigController instance;
	private final Logger log = Logger.getInstance();
	private final Config config;

	private PluginConfigController(Config config) {
		this.config = config;
	}
	
	public static synchronized PluginConfigController getInstance(Config config) {
		if (instance == null)
			instance = new PluginConfigController(config);
		
		return instance;
	}

	@SuppressWarnings("unchecked")
	public <T extends PluginConfig> void setOrCreatePluginConfig(ConfigExtension<T> plugin) {
		Class<T> pluginConfigClass = null;
		T pluginConfig = null;

		try {
			pluginConfigClass = (Class<T>)plugin.getClass().getMethod("getConfig", new Class<?>[]{}).getReturnType();
			pluginConfig = getPluginConfig(pluginConfigClass);
			
			if (pluginConfig == null) {
				pluginConfig = pluginConfigClass.newInstance();
				updatePluginConfig(pluginConfig);
			}
			
			// propagate new config to plugin
			plugin.configLoaded(pluginConfig);
			
		} catch (NoSuchMethodException e) {
			log.error("Failed to instantiate config for plugin '" + plugin.getClass().getCanonicalName() + "'.");
			log.error("Please check the following error message: " + e.getMessage());
		} catch (InstantiationException e) {
			log.error("Failed to instantiate class '" + pluginConfigClass.getCanonicalName() + "'.");
			log.error("Please provide a no-arg constructor.");
		} catch (IllegalAccessException e) {
			log.error("Failed to access no-arg constructor of class '" + pluginConfigClass.getCanonicalName() + "'.");
			log.error("Please check the following error message: " + e.getMessage());
		} catch (SecurityException e) {
			log.error("Failed to instantiate config for plugin '" + plugin.getClass().getCanonicalName() + "'.");
			log.error("Please check the following error message: " + e.getMessage());
		}			
	}

	@SuppressWarnings("unchecked")
	public <T extends PluginConfig> T getPluginConfig(Class<T> pluginConfigClass) {
		if (pluginConfigClass == null)
			throw new IllegalArgumentException("Plugin config class may not be null.");

		return (T)config.getProject().getExtension(pluginConfigClass);
	}

	@SuppressWarnings("unchecked")
	public <T extends PluginConfig> T updatePluginConfig(T pluginConfig) {
		if (pluginConfig == null)
			throw new IllegalArgumentException("Plugin config may not be null.");

		return (T)config.getProject().registerExtension(pluginConfig);
	}

}
