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
