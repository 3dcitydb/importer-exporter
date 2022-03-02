package org.citydb.core.operation.common.property;

public class FeatureProperty extends AbstractRefProperty {
    @Override
    public PropertyType getType() {
        return PropertyType.FEATURE;
    }
}
