package org.citydb.core.operation.common.property;

public class GeometryProperty extends AbstractProperty {
    @Override
    public PropertyType getType() {
        return PropertyType.GEOMETRY;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
