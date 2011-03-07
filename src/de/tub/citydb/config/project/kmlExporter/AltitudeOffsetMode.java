package de.tub.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="AltitudeOffsetMode")
@XmlEnum
public enum AltitudeOffsetMode {
	@XmlEnumValue("no_offset")
    NO_OFFSET("no_offset"),
    @XmlEnumValue("constant")
    CONSTANT("constant"),
    @XmlEnumValue("generic_attribute")
    GENERIC_ATTRIBUTE("generic_attribute");

    private final String value;

    AltitudeOffsetMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AltitudeOffsetMode fromValue(String v) {
        for (AltitudeOffsetMode c: AltitudeOffsetMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return NO_OFFSET;
    }
}
