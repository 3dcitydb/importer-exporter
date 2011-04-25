package de.tub.citydb.api.plugin.extension.config;

public interface ConfigExtension<T extends PluginConfig> {
	public Class<T> getConfigClass();
	public void configLoaded(T config);
	public void resetConfigDefaults();
}
