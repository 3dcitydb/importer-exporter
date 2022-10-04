package org.citydb.core.plugin;

import org.citydb.util.event.Event;
import org.citydb.util.event.global.EventType;

import java.util.List;

public class PluginStateEvent extends Event {
    private final List<Plugin> plugins;

    public PluginStateEvent(List<Plugin> plugins) {
        super(EventType.PLUGIN_STATE, GLOBAL_CHANNEL);
        this.plugins = plugins;
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }
}
