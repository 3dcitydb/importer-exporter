package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ImportUpdatingPersonModeType")
public enum ImpUpdatingPersonMode {
	@XmlEnumValue("database")
    DATABASE("database"),
	@XmlEnumValue("user")
    USER("user");
	
	private final String value;

	ImpUpdatingPersonMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ImpUpdatingPersonMode fromValue(String v) {
        for (ImpUpdatingPersonMode c: ImpUpdatingPersonMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return DATABASE;
    }
}
