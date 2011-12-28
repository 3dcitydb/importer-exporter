package de.tub.citydb.config.project.plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.tub.citydb.api.plugin.extension.config.PluginConfig;

public class PluginConfigListAdapter extends XmlAdapter<PluginConfigList, HashMap<Class<? extends PluginConfig>, PluginConfig>> {

	@Override
	public HashMap<Class<? extends PluginConfig>, PluginConfig> unmarshal(PluginConfigList v) throws Exception {
		HashMap<Class<? extends PluginConfig>, PluginConfig> map = new HashMap<Class<? extends PluginConfig>, PluginConfig>();

		if (v != null) {
			for (PluginConfigItem item : v.getItems())
				if (item.getConfig() != null && !item.getConfig().getClass().equals(PluginConfig.class))
					map.put((Class<? extends PluginConfig>)item.getConfig().getClass(), item.getConfig());
		}

		return map;
	}

	@Override
	public PluginConfigList marshal(HashMap<Class<? extends PluginConfig>, PluginConfig> v) throws Exception {
		PluginConfigList list = new PluginConfigList();

		if (v != null) {
			Iterator<Entry<Class<? extends PluginConfig>, PluginConfig>> iter = v.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Class<? extends PluginConfig>, PluginConfig> entry = iter.next();
				PluginConfigItem item = new PluginConfigItem();
				item.setConfig(entry.getValue());
				list.addItem(item);
			}
		}

		return list;
	}

}
