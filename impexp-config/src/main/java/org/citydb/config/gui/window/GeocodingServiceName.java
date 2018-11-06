package org.citydb.config.gui.window;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="GeocodingServiceNameType")
@XmlEnum
public enum GeocodingServiceName {
    @XmlEnumValue("nominatim")
    OSM_NOMINATIM("OSM Nominatim"),
    @XmlEnumValue("google")
    GOOGLE_GEOCODING_API("Google Geocoding API");

    private final String value;

    GeocodingServiceName(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
