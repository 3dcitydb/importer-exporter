package de.tub.citydb.config.project.database;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DBOperationModeType")
@XmlEnum
public enum DBOperationMode {
	@XmlEnumValue("report")
    REPORT("report"),
    @XmlEnumValue("boundingBox")
    BBOX("boundingBox");

    private final String value;

    DBOperationMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DBOperationMode fromValue(String v) {
        for (DBOperationMode c:DBOperationMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return REPORT;
    }
}
