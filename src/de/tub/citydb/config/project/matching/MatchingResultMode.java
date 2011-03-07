package de.tub.citydb.config.project.matching;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="MatchingResultModeType")
@XmlEnum
public enum MatchingResultMode {
	@XmlEnumValue("fix")
    FIX("fix"),
    @XmlEnumValue("user")
    USER("user"),
    @XmlEnumValue("all")
    ALL("all");

    private final String value;

    MatchingResultMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MatchingResultMode fromValue(String v) {
        for (MatchingResultMode c: MatchingResultMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return FIX;
    }
}
