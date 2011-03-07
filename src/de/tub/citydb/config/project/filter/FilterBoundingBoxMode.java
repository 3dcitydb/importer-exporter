package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="BoundingBoxModeType")
@XmlEnum
public enum FilterBoundingBoxMode {
	@XmlEnumValue("contain")
    CONTAIN("contain"),
    @XmlEnumValue("overlap")
    OVERLAP("overlap");

    private final String value;

    FilterBoundingBoxMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FilterBoundingBoxMode fromValue(String v) {
        for (FilterBoundingBoxMode c: FilterBoundingBoxMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return CONTAIN;
    }
}
