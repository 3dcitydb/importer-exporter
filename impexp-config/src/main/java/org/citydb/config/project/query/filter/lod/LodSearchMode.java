package org.citydb.config.project.query.filter.lod;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="LodSearchDepthModeType")
@XmlEnum
public enum LodSearchMode {
	@XmlEnumValue("all")
	ALL("all"),
	@XmlEnumValue("depth")
	DEPTH("depth");
	
	private final String value;
	
	LodSearchMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LodSearchMode fromValue(String v) {
        for (LodSearchMode c: LodSearchMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
