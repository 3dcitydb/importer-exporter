package org.citydb.config.project.importer;

import org.citydb.config.geometry.BoundingBox;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="SimpleBBOXFilterType", propOrder={
        "extent"
})
public class SimpleBBOXOperator {
    @XmlAttribute(required = true)
    private SimpleBBOXMode mode = SimpleBBOXMode.BBOX;
    @XmlElement(required=true)
    private BoundingBox extent;

    public SimpleBBOXOperator() {
        extent = new BoundingBox();
    }

    public SimpleBBOXMode getMode() {
        return mode;
    }

    public void setMode(SimpleBBOXMode mode) {
        this.mode = mode;
    }

    public boolean isSetExtent() {
        return extent != null;
    }

    public BoundingBox getExtent() {
        return extent;
    }

    public void setExtent(BoundingBox extent) {
        this.extent = extent;
    }
}
