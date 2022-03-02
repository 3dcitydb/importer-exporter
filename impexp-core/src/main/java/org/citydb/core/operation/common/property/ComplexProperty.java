package org.citydb.core.operation.common.property;

import java.util.HashSet;
import java.util.Set;

public class ComplexProperty extends AbstractProperty {
    Set<AbstractProperty> children = new HashSet<>();

    @Override
    public PropertyType getType() {
        return PropertyType.COMPLEX;
    }

    public Set<AbstractProperty> getChildren() {
        return children;
    }

    public boolean isEmpty() {
        return children.isEmpty();
    }

    public void addChild(AbstractProperty property) {
        children.add(property);
    }
}
