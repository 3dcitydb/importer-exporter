package org.citydb.core.operation.common.property;

public class DoubleProperty extends AbstractProperty {
    @Override
    public PropertyType getType() {
        return PropertyType.DOUBLE;
    }

    public Double getValue() {
        return (Double) value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
