package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="TexturePathModeType")
@XmlEnum
public enum TexturePathMode {
	@XmlEnumValue("relative")
    RELATIVE("relative"),
    @XmlEnumValue("absolute")
    ABSOLUTE("absolute");

    private final String value;

    TexturePathMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TexturePathMode fromValue(String v) {
        for (TexturePathMode c: TexturePathMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return RELATIVE;
    }
}
