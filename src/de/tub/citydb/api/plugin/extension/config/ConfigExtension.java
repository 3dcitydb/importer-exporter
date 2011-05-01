package de.tub.citydb.api.plugin.extension.config;

public interface ConfigExtension<T extends PluginConfig> {
	public T getConfig();
	public void configLoaded(T config);
	public void handleEvent(PluginConfigEvent event);
}
