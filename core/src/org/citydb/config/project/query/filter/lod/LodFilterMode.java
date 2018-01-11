package org.citydb.config.project.query.filter.lod;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="LodFilterModeType")
@XmlEnum
public enum LodFilterMode {
	@XmlEnumValue("or")
	OR("Or"),
	@XmlEnumValue("and")
	AND("And");
	
	private final String value;

	LodFilterMode(String v) {
        value = v;
    }

    public static LodFilterMode fromValue(String v) {
        for (LodFilterMode c : LodFilterMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return OR;
    }
    
    @Override
    public String toString() {
        return value;
    }
}
