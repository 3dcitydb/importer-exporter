/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.config.controller;

import de.tub.citydb.api.controller.PluginConfigController;
import de.tub.citydb.api.plugin.extension.config.ConfigExtension;
import de.tub.citydb.api.plugin.extension.config.PluginConfig;
import de.tub.citydb.config.Config;
import de.tub.citydb.log.Logger;

public class PluginConfigControllerImpl implements PluginConfigController {
	private final Logger LOG = Logger.getInstance();
	private final Config config;

	public PluginConfigControllerImpl(Config config) {
		this.config = config;
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
			LOG.error("Failed to instantiate config for plugin '" + plugin.getClass().getCanonicalName() + "'.");
			LOG.error("Please check the following error message: " + e.getMessage());
		} catch (InstantiationException e) {
			LOG.error("Failed to instantiate class '" + pluginConfigClass.getCanonicalName() + "'.");
			LOG.error("Please provide a no-arg constructor.");
		} catch (IllegalAccessException e) {
			LOG.error("Failed to access no-arg constructor of class '" + pluginConfigClass.getCanonicalName() + "'.");
			LOG.error("Please check the following error message: " + e.getMessage());
		} catch (SecurityException e) {
			LOG.error("Failed to instantiate config for plugin '" + plugin.getClass().getCanonicalName() + "'.");
			LOG.error("Please check the following error message: " + e.getMessage());
		}			
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PluginConfig> T getPluginConfig(Class<T> pluginConfigClass) {
		if (pluginConfigClass == null)
			throw new IllegalArgumentException("Plugin config class may not be null.");

		return (T)config.getProject().getExtension(pluginConfigClass);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends PluginConfig> T updatePluginConfig(T pluginConfig) {
		if (pluginConfig == null)
			throw new IllegalArgumentException("Plugin config may not be null.");

		return (T)config.getProject().registerExtension(pluginConfig);
	}

}
