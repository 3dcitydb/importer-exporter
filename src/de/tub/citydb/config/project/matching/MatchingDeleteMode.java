package de.tub.citydb.config.project.matching;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="MatchingDeleteModeType")
@XmlEnum
public enum MatchingDeleteMode {
	@XmlEnumValue("merge")
    MERGE("merge"),
    @XmlEnumValue("delall")
    DELALL("delall"),
    @XmlEnumValue("rename")
    RENAME("rename");

    private final String value;

    MatchingDeleteMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static MatchingDeleteMode fromValue(String v) {
        for (MatchingDeleteMode c: MatchingDeleteMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return MERGE;
    }
}
