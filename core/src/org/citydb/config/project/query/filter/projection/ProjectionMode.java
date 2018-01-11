package org.citydb.config.project.query.filter.projection;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="PropertyProjectionModeType")
@XmlEnum
public enum ProjectionMode {
	@XmlEnumValue("keep")
	KEEP("keep"),
	@XmlEnumValue("remove")
	REMOVE("remove");
	
	private final String value;
	
	private ProjectionMode(String v) {
		value = v;
	}
	
	public String value() {
        return value;
    }
	
	public static ProjectionMode fromValue(String v) {
        for (ProjectionMode c: ProjectionMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return KEEP;
    }
	
}