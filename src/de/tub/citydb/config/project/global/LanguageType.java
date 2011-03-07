package de.tub.citydb.config.project.global;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="LanguageType")
@XmlEnum
public enum LanguageType {
	@XmlEnumValue("de")
    DE("de"),
    @XmlEnumValue("en")
    EN("en");

    private final String value;

    LanguageType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LanguageType fromValue(String v) {
        for (LanguageType c: LanguageType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return EN;
    }
}
