package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.internal.Internal;

@XmlType(name="TileNameSuffixModeType")
@XmlEnum
public enum TileNameSuffixMode {
	@XmlEnumValue("none")
    NONE("none"),
    @XmlEnumValue("sameAsPath")
    SAME_AS_PATH("sameAsPath");

    private final String value;

    TileNameSuffixMode(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public String toString() {
    	switch (this) {
    	case NONE:
    		return Internal.I18N.getString("pref.export.boundingBox.label.tile.nameSuffix.none");
    	case SAME_AS_PATH:
    		return Internal.I18N.getString("pref.export.boundingBox.label.tile.nameSuffix.sameAsPath");
    	default:
    		return null;
    	}
    }
    
    public static TileNameSuffixMode fromValue(String v) {
        for (TileNameSuffixMode c: TileNameSuffixMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return NONE;
    }
}
