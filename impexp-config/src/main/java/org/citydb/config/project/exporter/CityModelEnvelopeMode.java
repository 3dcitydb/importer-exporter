package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "CityModelEnvelopeModeType")
public class CityModelEnvelopeMode {
    @XmlAttribute
    private Boolean useTileExtent;
    @XmlValue
    private boolean enabled;

    public boolean isUseTileExtent() {
        return useTileExtent != null ? useTileExtent : false;
    }

    public void setUseTileExtent(Boolean useTileExtent) {
        this.useTileExtent = useTileExtent;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
