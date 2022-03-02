package org.citydb.core.operation.common.property;

public class MeasureProperty extends DoubleProperty {
    private String uom;

    @Override
    public PropertyType getType() {
        return PropertyType.MEASURE;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public boolean isSetUom() {
        return uom != null;
    }
}
