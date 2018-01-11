package org.citydb.config.project.query.filter.projection;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import org.citygml4j.model.citygml.CityGMLClass;

@XmlType(name="GenericAttributeTypeType")
@XmlEnum
public enum GenericAttributeType {
	@XmlEnumValue("stringAttribute")
	STRING_ATTRIBUTE("stringAttribute", CityGMLClass.STRING_ATTRIBUTE),
	@XmlEnumValue("intAttribute")
	INT_ATTRIBUTE("intAttribute", CityGMLClass.INT_ATTRIBUTE),
	@XmlEnumValue("doubleAttribute")
	DOUBLE_ATTRIBUTE("doubleAttribute", CityGMLClass.DOUBLE_ATTRIBUTE),
	@XmlEnumValue("dateAttribute")
	DATE_ATTRIBUTE("dateAttribute", CityGMLClass.DATE_ATTRIBUTE),
	@XmlEnumValue("uriAttribute")
	URI_ATTRIBUTE("uriAttribute", CityGMLClass.URI_ATTRIBUTE),
	@XmlEnumValue("measureAttribute")
	MEASURE_ATTRIBUTE("measureAttribute", CityGMLClass.MEASURE_ATTRIBUTE),
	@XmlEnumValue("genericAttributeSet")
	GENERIC_ATTRIBUTE_SET("genericAttributeSet", CityGMLClass.GENERIC_ATTRIBUTE_SET);
	
	private final String value;
	private final CityGMLClass featureClass;
	
	private GenericAttributeType(String v, CityGMLClass featureClass) {
		this.value = v;
		this.featureClass = featureClass;
	}
	
	public String value() {
        return value;
    }
	
	public CityGMLClass getCityGMLClass() {
		return featureClass;
	}
	
	public static GenericAttributeType fromValue(String v) {
        for (GenericAttributeType c: GenericAttributeType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException();
    }
	
}