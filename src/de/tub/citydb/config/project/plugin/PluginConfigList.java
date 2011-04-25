package de.tub.citydb.config.project.plugin;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="PluginConfigListType", propOrder={
		"plugin"
})
public class PluginConfigList {
	private List<PluginConfigItem> plugin;

	public PluginConfigList() {
		plugin = new ArrayList<PluginConfigItem>();
	}

	public List<PluginConfigItem> getItems() {
		return plugin;
	}

	public void addItem(PluginConfigItem entry) {
		if (entry != null)
			plugin.add(entry);
	}
}
