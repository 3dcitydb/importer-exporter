package org.citydb.core.plugin.metadata;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "plugin")
@XmlType(name = "PluginType", propOrder = {})
public class PluginMetadata {
    @XmlElement(required = true)
    private String name;
    private String version;
    private String description;
    private String url;
    private PluginVendor vendor;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PluginVendor getVendor() {
        return vendor;
    }

    public void setVendor(PluginVendor vendor) {
        this.vendor = vendor;
    }
}
