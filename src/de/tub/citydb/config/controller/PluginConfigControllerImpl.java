package de.tub.citydb.config.controller;

import de.tub.citydb.api.controller.PluginConfigController;
import de.tub.citydb.api.log.Logger;
import de.tub.citydb.api.plugin.extension.config.ConfigExtension;
import de.tub.citydb.api.plugin.extension.config.PluginConfig;
import de.tub.citydb.config.Config;

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
