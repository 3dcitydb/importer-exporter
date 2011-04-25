package de.tub.citydb.api.plugin.extension.config;

public interface Config<T extends PluginConfig> {
	public void configLoaded(T config);
	public void handleEvent(ConfigEvent event);
}
