package org.citydb.config.project.kmlExporter;

import org.citydb.config.project.query.filter.tiling.AbstractTiling;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="KmlTilingType", propOrder={
        "tilingOptions"
})
public class KmlTiling extends AbstractTiling {
    @XmlAttribute(required = true)
    private KmlTilingMode mode = KmlTilingMode.NO_TILING;
    private KmlTilingOptions tilingOptions;

    public KmlTiling() {
        tilingOptions = new KmlTilingOptions();
    }

    public KmlTilingMode getMode() {
        return mode;
    }

    public void setMode(KmlTilingMode mode) {
        this.mode = mode;
    }

    @Override
    public KmlTilingOptions getTilingOptions() {
        return tilingOptions;
    }

    @Override
    public boolean isSetTilingOptions() {
        return tilingOptions != null;
    }

    public void setTilingOptions(KmlTilingOptions tilingOptions) {
        this.tilingOptions = tilingOptions;
    }

}
