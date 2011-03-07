package de.tub.citydb.config.project.kmlExporter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.internal.Internal;

@XmlType(name="AltitudeMode")
@XmlEnum
public enum AltitudeMode {
	@XmlEnumValue("relative")
    RELATIVE("relative"),
    @XmlEnumValue("absolute")
    ABSOLUTE("absolute");

    private final String value;

    AltitudeMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AltitudeMode fromValue(String v) {
        for (AltitudeMode c: AltitudeMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return ABSOLUTE;
    }
    
	public String toString() {
		return Internal.I18N.getString("pref.kmlexport.altitude.mode.label." + value());
	}

}
