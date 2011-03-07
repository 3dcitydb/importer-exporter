package de.tub.citydb.config.project.general;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="PathModeType")
@XmlEnum
public enum PathMode {
	@XmlEnumValue("lastUsed")
    LASTUSED("lastUsed"),
    @XmlEnumValue("standard")
    STANDARD("standard");

    private final String value;

    PathMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static PathMode fromValue(String v) {
        for (PathMode c: PathMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return LASTUSED;
    }
}
