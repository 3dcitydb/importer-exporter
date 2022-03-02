package org.citydb.core.operation.common.property;

public class StringProperty extends AbstractProperty {
    @Override
    public PropertyType getType() {
        return PropertyType.STRING;
    }

    public String getValue() {
        return (String) value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
