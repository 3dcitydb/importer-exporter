package de.tub.citydb.config.project.matching;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="MatchingGmlNameModeType")
@XmlEnum
public enum MatchingGmlNameMode {
	@XmlEnumValue("append")
    APPEND("append"),
    @XmlEnumValue("ignore")
    IGNORE("ignore"),
    @XmlEnumValue("replace")
    REPLACE("replace");

    private final String value;

    MatchingGmlNameMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MatchingGmlNameMode fromValue(String v) {
        for (MatchingGmlNameMode c: MatchingGmlNameMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return APPEND;
    }
}
