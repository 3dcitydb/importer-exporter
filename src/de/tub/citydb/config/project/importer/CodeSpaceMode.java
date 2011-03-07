package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="CodeSpaceModeType")
public enum CodeSpaceMode {
	@XmlEnumValue("relativeFileName")
    RELATIVE("relativefileName"),
    @XmlEnumValue("absoluteFileName")
    ABSOLUTE("absoluteFileName"),
	@XmlEnumValue("user")
    USER("user");

    private final String value;

    CodeSpaceMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CodeSpaceMode fromValue(String v) {
        for (CodeSpaceMode c: CodeSpaceMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return USER;
    }
}
