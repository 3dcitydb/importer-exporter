package de.tub.citydb.log;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="LogLevelType")
@XmlEnum
public enum LogLevelType {
	@XmlEnumValue("error")
	ERROR("ERROR"),
	@XmlEnumValue("warn")
	WARN("WARN"),
	@XmlEnumValue("info")
	INFO("INFO"),
	@XmlEnumValue("debug")
	DEBUG("DEBUG");
	
	private final String value;

	LogLevelType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LogLevelType fromValue(String v) {
        for (LogLevelType c: LogLevelType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return INFO;
    }
}
