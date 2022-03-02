package org.citydb.core.operation.common.property;

public class IntegerProperty extends AbstractProperty {
    @Override
    public PropertyType getType() {
        return PropertyType.INTEGER;
    }

    public Integer getValue() {
        return (Integer) value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}
