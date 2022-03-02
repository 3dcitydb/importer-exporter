package org.citydb.core.operation.common.property;

public class XmlContentProperty extends StringProperty {
    @Override
    public PropertyType getType() {
        return PropertyType.XML_CONTENT;
    }
}
