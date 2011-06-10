package de.tub.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.citygml4j.model.module.citygml.CityGMLVersion;

@XmlType(name="CityGMLVersionType")
@XmlEnum
public enum CityGMLVersionType {
	@XmlEnumValue("v1.0.0")
	v1_0_0("v1.0.0 (OGC Encoding Standard)"),
	@XmlEnumValue("v0.4.0")
	v0_4_0("v0.4.0");

	private final String value;

	CityGMLVersionType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
	
	public CityGMLVersion toCityGMLVersion() {
		switch (this) {
		case v0_4_0:
			return CityGMLVersion.v0_4_0;
		default:
			return CityGMLVersion.v1_0_0;
		}
	}

	public static CityGMLVersionType fromCityGMLVersion(CityGMLVersion version) {
		if (version == CityGMLVersion.v0_4_0)
			return v0_4_0;
		else
			return v1_0_0;
	}
	
	public static CityGMLVersionType fromValue(String value) {
		for (CityGMLVersionType type : values())
			if (type.toString().equals(value))
				return type;
		
		return v1_0_0;
	}

}
