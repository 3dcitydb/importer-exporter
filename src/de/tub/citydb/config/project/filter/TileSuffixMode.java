package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.internal.Internal;

@XmlType(name="TileSuffixModeType")
@XmlEnum
public enum TileSuffixMode {
	@XmlEnumValue("row_column")
    ROW_COLUMN("row_column"),
    @XmlEnumValue("xMin_yMin")
    XMIN_YMIN("xMin_yMin"),
    @XmlEnumValue("xMax_yMin")
    XMAX_YMIN("xMax_yMin"),
    @XmlEnumValue("xMin_yMax")
    XMIN_YMAX("xMin_yMax"),
    @XmlEnumValue("xMax_yMax")
    XMAX_YMAX("xMax_yMax"),
    @XmlEnumValue("xMin_yMin_xMax_yMax")
    XMIN_YMIN_XMAX_YMAX("xMin_yMin_xMax_yMax");

    private final String value;

    TileSuffixMode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
    
    public String toString() {
    	switch (this) {
    	case ROW_COLUMN:
    		return Internal.I18N.getString("pref.export.boundingBox.label.tile.pathSuffix.rowColumn");
    	case XMIN_YMIN:
    		return "Xmin / Ymin";
    	case XMIN_YMAX:
    		return "Xmin / Ymax";
    	case XMAX_YMIN:
    		return "Xmax / Ymin";
    	case XMAX_YMAX:
    		return "Xmax / Ymax";
    	case XMIN_YMIN_XMAX_YMAX:
    		return "Xmin / Ymin / Xmax / Ymax";
    	default:
    		return null;
    	}
    }

    public static TileSuffixMode fromValue(String v) {
        for (TileSuffixMode c: TileSuffixMode.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }

        return ROW_COLUMN;
    }
}
