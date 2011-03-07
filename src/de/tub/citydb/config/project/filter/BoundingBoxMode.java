package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="BoundingBoxModeType")
@XmlEnum
public enum BoundingBoxMode {
	@XmlEnumValue("contain")
    CONTAIN("contain"),
    @XmlEnumValue("overlap")
    OVERLAP("overlap");

    private final String value;

    BoundingBoxMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BoundingBoxMode fromValue(String v) {
        for (BoundingBoxMode c: BoundingBoxMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return CONTAIN;
    }
}
