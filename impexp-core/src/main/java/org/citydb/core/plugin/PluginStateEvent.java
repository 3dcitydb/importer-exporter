package org.citydb.core.plugin;

import org.citydb.util.event.Event;
import org.citydb.util.event.global.EventType;

import java.util.List;

public class PluginStateEvent extends Event {
    private final List<Plugin> plugins;

    public PluginStateEvent(List<Plugin> plugins, String label) {
        super(EventType.PLUGIN_STATE, GLOBAL_CHANNEL, label);
        this.plugins = plugins;
    }

    public PluginStateEvent(List<Plugin> plugins) {
        this(plugins, null);
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }
}
