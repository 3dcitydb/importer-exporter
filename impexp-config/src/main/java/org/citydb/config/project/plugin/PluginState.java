package org.citydb.config.project.plugin;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "PluginStateType")
public class PluginState {
    @XmlAttribute(required = true)
    private String pluginClass;
    @XmlAttribute(required = true)
    private boolean isEnabled;

    public String getPluginClass() {
        return pluginClass;
    }

    public void setPluginClass(String pluginClass) {
        if (pluginClass != null) {
            this.pluginClass = pluginClass;
        }
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
}
