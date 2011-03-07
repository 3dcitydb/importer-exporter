package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ExportXLinkModeType")
@XmlEnum
public enum ExpXLinkMode {
	@XmlEnumValue("xlink")
    XLINK("xlink"),
    @XmlEnumValue("copy")
    COPY("copy");

    private final String value;

    ExpXLinkMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ExpXLinkMode fromValue(String v) {
        for (ExpXLinkMode c: ExpXLinkMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return XLINK;
    }
}
