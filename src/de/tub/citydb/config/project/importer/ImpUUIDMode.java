package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ImportUUIDModeType")
@XmlEnum
public enum ImpUUIDMode {
	@XmlEnumValue("complement")
    COMPLEMENT("complement"),
    @XmlEnumValue("replace")
    REPLACE("replace");

    private final String value;

    ImpUUIDMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ImpUUIDMode fromValue(String v) {
        for (ImpUUIDMode c: ImpUUIDMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return REPLACE;
    }
}
