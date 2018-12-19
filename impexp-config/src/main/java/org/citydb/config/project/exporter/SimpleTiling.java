package org.citydb.config.project.exporter;

import org.citydb.config.project.query.filter.tiling.AbstractTiling;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="SimpleTilingType", propOrder={
        "tilingOptions"
})
public class SimpleTiling extends AbstractTiling {
    @XmlAttribute(required = true)
    private SimpleTilingMode mode = SimpleTilingMode.BBOX;
    private SimpleTilingOptions tilingOptions;

    public SimpleTiling() {
        tilingOptions = new SimpleTilingOptions();
    }

    public SimpleTilingMode getMode() {
        return mode;
    }

    public void setMode(SimpleTilingMode mode) {
        this.mode = mode;
    }

    @Override
    public SimpleTilingOptions getTilingOptions() {
        return tilingOptions;
    }

    @Override
    public boolean isSetTilingOptions() {
        return tilingOptions != null;
    }

    public void setTilingOptions(SimpleTilingOptions tilingOptions) {
        this.tilingOptions = tilingOptions;
    }

}
