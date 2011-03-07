package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ImportIndexModeType")
@XmlEnum
public enum ImpIndexMode {
	@XmlEnumValue("unchanged")
    UNCHANGED("unchanged"),
	@XmlEnumValue("deactivate")
    DEACTIVATE("deactivate"),
    @XmlEnumValue("deactivate_activate")
    DEACTIVATE_ACTIVATE("deactivate_activate");

    private final String value;

    ImpIndexMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ImpIndexMode fromValue(String v) {
        for (ImpIndexMode c: ImpIndexMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return UNCHANGED;
    }
}
