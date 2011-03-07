package de.tub.citydb.config.project.importer;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="LocalXMLSchemaType")
@XmlEnum
public enum LocalXMLSchemaType {
	@XmlEnumValue("CityGML_v1.0.0")
    CityGML_v1_0_0("CityGML v1.0.0 Base Profile"),
	@XmlEnumValue("CityGML_v0.4.0")
    CityGML_v0_4_0("CityGML v0.4.0");
	
	private final String value;

	LocalXMLSchemaType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LocalXMLSchemaType fromValue(String v) {
        for (LocalXMLSchemaType c: LocalXMLSchemaType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return CityGML_v1_0_0;
    }
}
