package org.citydb.config.project.plugin;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginListAdapter extends XmlAdapter<PluginListAdapter.PluginList, Map<String, Boolean>> {

    public static class PluginList {
        private List<PluginState> plugin;
    }

    @Override
    public Map<String, Boolean> unmarshal(PluginList plugins) throws Exception {
        Map<String, Boolean> map = new HashMap<>();

        if (plugins != null && plugins.plugin != null && !plugins.plugin.isEmpty()) {
            for (PluginState pluginState : plugins.plugin) {
                if (pluginState != null) {
                    map.put(pluginState.getPluginClass(), pluginState.isEnabled());
                }
            }
        }

        return map;
    }

    @Override
    public PluginList marshal(Map<String, Boolean> plugins) throws Exception {
        PluginList list = null;

        if (plugins != null && !plugins.isEmpty()) {
            list = new PluginList();
            list.plugin = new ArrayList<>();
            for (Map.Entry<String, Boolean> entry : plugins.entrySet()) {
                if (entry.getValue() != null) {
                    PluginState pluginState = new PluginState();
                    pluginState.setPluginClass(entry.getKey());
                    pluginState.setEnabled(entry.getValue());
                    list.plugin.add(pluginState);
                }
            }
        }

        return list;
    }
}
