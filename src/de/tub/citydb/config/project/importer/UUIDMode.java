package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="UUIDModeType")
@XmlEnum
public enum UUIDMode {
	@XmlEnumValue("complement")
    COMPLEMENT("complement"),
    @XmlEnumValue("replace")
    REPLACE("replace");

    private final String value;

    UUIDMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UUIDMode fromValue(String v) {
        for (UUIDMode c: UUIDMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return REPLACE;
    }
}
