package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name="CityModelEnvelopeModeType")
public class CityModelEnvelopeMode {
    @XmlAttribute
    private Boolean useTileExtent;
    @XmlValue
    private Boolean useEnvelope;

    public boolean isUseTileExtent() {
        return useTileExtent != null ? useTileExtent : false;
    }

    public void setUseTileExtent(boolean useTileExtent) {
        this.useTileExtent = useTileExtent;
    }

    public boolean isUseEnvelope() {
        return useEnvelope != null ? useEnvelope : false;
    }

    public void setUseEnvelope(boolean useEnvelope) {
        this.useEnvelope = useEnvelope;
    }
}
