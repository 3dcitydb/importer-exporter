package org.citydb.core.operation.common.property;

public abstract class AbstractRefProperty extends AbstractProperty {
    public Long getValue() {
        return (Long) value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
