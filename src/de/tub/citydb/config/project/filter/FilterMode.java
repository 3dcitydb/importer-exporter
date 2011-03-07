package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="FilterModeType")
public enum FilterMode {
	@XmlEnumValue("none")
    NONE("none"),
	@XmlEnumValue("simple")
    SIMPLE("simple"),
    @XmlEnumValue("complex")
    COMPLEX("complex");

    private final String value;

    FilterMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FilterMode fromValue(String v) {
        for (FilterMode c: FilterMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return COMPLEX;
    }
}
