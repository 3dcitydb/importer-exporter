package de.tub.citydb.config.project.plugin;

import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.api.plugin.extension.config.PluginConfig;

@XmlType(name="PluginConfigItemType", propOrder={
		"config"
})
public class PluginConfigItem {
	
	private PluginConfig config;

	public PluginConfigItem() {
	}

	public PluginConfig getConfig() {
		return config;
	}

	public void setConfig(PluginConfig config) {
		if (config != null)
			this.config = config;
	}

}
