package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="IndexModeType")
@XmlEnum
public enum IndexMode {
	@XmlEnumValue("unchanged")
    UNCHANGED("unchanged"),
	@XmlEnumValue("deactivate")
    DEACTIVATE("deactivate"),
    @XmlEnumValue("deactivate_activate")
    DEACTIVATE_ACTIVATE("deactivate_activate");

    private final String value;

    IndexMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static IndexMode fromValue(String v) {
        for (IndexMode c: IndexMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return UNCHANGED;
    }
}
