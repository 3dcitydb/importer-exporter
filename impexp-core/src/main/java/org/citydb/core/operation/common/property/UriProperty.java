package org.citydb.core.operation.common.property;

public class UriProperty extends StringProperty {
    @Override
    public PropertyType getType() {
        return PropertyType.URI;
    }
}
