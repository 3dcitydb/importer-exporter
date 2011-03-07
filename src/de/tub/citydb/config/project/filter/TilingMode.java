package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="TilingModeType")
@XmlEnum
public enum TilingMode {
	@XmlEnumValue("no_tiling")
    NO_TILING("no_tiling"),
    @XmlEnumValue("automatic")
    AUTOMATIC("automatic"),
    @XmlEnumValue("manual")
    MANUAL("manual");

    private final String value;

    TilingMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TilingMode fromValue(String v) {
        for (TilingMode c: TilingMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return NO_TILING;
    }
}
