package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlEnum
@XmlType(name="relationType")
public enum RelationType {

    @XmlEnumValue("association")
    ASSOCIATION("association"),
    @XmlEnumValue("aggregation")
    AGGREGATION("aggregation"),
    @XmlEnumValue("composition")
    COMPOSITION("composition");

    private final String value;

    RelationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static RelationType fromValue(String v) {
        for (RelationType c: RelationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
