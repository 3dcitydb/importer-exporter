package de.tub.citydb.api.controller;

import de.tub.citydb.api.plugin.extension.config.PluginConfig;

public interface PluginConfigController {
	public <T extends PluginConfig> T getPluginConfig(Class<T> pluginConfigClass);
	public <T extends PluginConfig> T updatePluginConfig(T pluginConfig);
}
