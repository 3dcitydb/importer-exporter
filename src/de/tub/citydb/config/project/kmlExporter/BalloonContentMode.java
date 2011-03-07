package de.tub.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="BalloonContentMode")
@XmlEnum
public enum BalloonContentMode {
	@XmlEnumValue("gen_attrib")
    GEN_ATTRIB("gen_attrib"),
    @XmlEnumValue("file")
    FILE("file"),
    @XmlEnumValue("gen_attrib_and_file")
    GEN_ATTRIB_AND_FILE("gen_attrib_and_file");

    private final String value;

    BalloonContentMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BalloonContentMode fromValue(String v) {
        for (BalloonContentMode c: BalloonContentMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return GEN_ATTRIB;
    }
}
