package de.tub.citydb.config.project.general;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="FeatureClassModeType")
@XmlEnum
public enum FeatureClassMode {
	@XmlEnumValue("CityObject")
    CITYOBJECT("CityObject"),
	@XmlEnumValue("Building")
    BUILDING("Building"),
	@XmlEnumValue("WaterBody")
    WATERBODY("WaterBody"),
	@XmlEnumValue("LandUse")
    LANDUSE("LandUse"),
	@XmlEnumValue("Vegetation")
    VEGETATION("Vegetation"),
	@XmlEnumValue("Transportation")
    TRANSPORTATION("Transportation"),
	@XmlEnumValue("ReliefFeature")
    RELIEFFEATURE("ReliefFeature"),
	@XmlEnumValue("CityFurniture")
    CITYFURNITURE("CityFurniture"),
	@XmlEnumValue("GenericCityObject")
    GENERICCITYOBJECT("GenericCityObject"),
	@XmlEnumValue("CityObjectGroup")
    CITYOBJECTGROUP("CityObjectGroup");
	
	private final String value;

	FeatureClassMode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
    
    public String toString() {
    	return value;
    }

    public static FeatureClassMode fromValue(String v) {
        for (FeatureClassMode c: FeatureClassMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return CITYOBJECT;
    }
}
