package org.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "CityJSONImportOptionsType", propOrder = {
        "mapUnknownExtensions"
})
public class CityJSONOptions {
    @XmlElement(defaultValue = "false")
    private boolean mapUnknownExtensions;

    public boolean isMapUnknownExtensions() {
        return mapUnknownExtensions;
    }

    public void setMapUnknownExtensions(boolean mapUnknownExtensions) {
        this.mapUnknownExtensions = mapUnknownExtensions;
    }
}
