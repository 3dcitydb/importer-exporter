package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="NamespaceType", propOrder={
        "prefix",
        "schemaLocation"
})
public class Namespace {
    @XmlAttribute(required = true)
    private String uri;
    @XmlAttribute
    private NamespaceMode mode;
    private String prefix;
    private String schemaLocation;

    public boolean isSetURI() {
        return uri != null;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public NamespaceMode getMode() {
        return mode != null ? mode : NamespaceMode.AUTOMATIC;
    }

    public void setMode(NamespaceMode mode) {
        this.mode = mode;
    }

    public boolean isSetPrefix() {
        return prefix != null;
    }

    public String getPrefix() {
        return prefix != null ? prefix.trim() : null;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isSetSchemaLocation() {
        return schemaLocation != null;
    }

    public String getSchemaLocation() {
        return schemaLocation != null ? schemaLocation.trim() : null;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }
}
