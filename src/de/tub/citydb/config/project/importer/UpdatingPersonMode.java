package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="UpdatingPersonModeType")
public enum UpdatingPersonMode {
	@XmlEnumValue("database")
    DATABASE("database"),
	@XmlEnumValue("user")
    USER("user");
	
	private final String value;

	UpdatingPersonMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static UpdatingPersonMode fromValue(String v) {
        for (UpdatingPersonMode c: UpdatingPersonMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return DATABASE;
    }
}
