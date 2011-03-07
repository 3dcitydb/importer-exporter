package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="ImportGmlIdCodeSpaceModeType")
public enum ImpGmlIdCodeSpaceMode {
	@XmlEnumValue("relativeFileName")
    RELATIVE("relativefileName"),
    @XmlEnumValue("absoluteFileName")
    ABSOLUTE("absoluteFileName"),
	@XmlEnumValue("user")
    USER("user");

    private final String value;

    ImpGmlIdCodeSpaceMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ImpGmlIdCodeSpaceMode fromValue(String v) {
        for (ImpGmlIdCodeSpaceMode c: ImpGmlIdCodeSpaceMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return USER;
    }
}
