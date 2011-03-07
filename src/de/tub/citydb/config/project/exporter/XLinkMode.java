package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="XLinkModeType")
@XmlEnum
public enum XLinkMode {
	@XmlEnumValue("xlink")
    XLINK("xlink"),
    @XmlEnumValue("copy")
    COPY("copy");

    private final String value;

    XLinkMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static XLinkMode fromValue(String v) {
        for (XLinkMode c: XLinkMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return XLINK;
    }
}
