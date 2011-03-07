package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ExportTexturePathModeType")
@XmlEnum
public enum ExpTexturePathMode {
	@XmlEnumValue("relative")
    RELATIVE("relative"),
    @XmlEnumValue("absolute")
    ABSOLUTE("absolute");

    private final String value;

    ExpTexturePathMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ExpTexturePathMode fromValue(String v) {
        for (ExpTexturePathMode c: ExpTexturePathMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return RELATIVE;
    }
}
