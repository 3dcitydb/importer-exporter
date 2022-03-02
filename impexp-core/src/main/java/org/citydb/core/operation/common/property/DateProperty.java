package org.citydb.core.operation.common.property;

import java.time.OffsetDateTime;

public class DateProperty extends AbstractProperty {
    @Override
    public PropertyType getType() {
        return PropertyType.DATE;
    }

    public OffsetDateTime getValue() {
        return (OffsetDateTime) value;
    }

    public void setValue(OffsetDateTime value) {
        this.value = value;
    }
}
