package de.tub.citydb.config.project.filter;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import de.tub.citydb.config.internal.Internal;

@XmlType(name="SRSType")
public enum FilterSRSType {
	@XmlEnumValue("0")
	SAME_AS_IN_DB(0, "Same as in database"),
	@XmlEnumValue("4326")
	WGS84(4326, "WGS 84"),
	@XmlEnumValue("25831")
	ETRS89_UTM_31N(25831, "ETRS89 / UTM zone 31N"),
	@XmlEnumValue("25832")
	ETRS89_UTM_32N(25832, "ETRS89 / UTM zone 32N"),
	@XmlEnumValue("25833")
	ETRS89_UTM_33N(25833, "ETRS89 / UTM zone 33N"),	
	@XmlEnumValue("31466")
	DHDN_3GK_Z2(31466, "DHDN / 3GK zone 2"),
	@XmlEnumValue("31467")
	DHDN_3GK_Z3(31467, "DHDN / 3GK zone 3"),
	@XmlEnumValue("31468")
	DHDN_3GK_Z4(31468, "DHDN / 3GK zone 4"),
	@XmlEnumValue("31469")
	DHDN_3GK_Z5(31466, "DHDN / 3GK zone 5");

	private final int srid;
	private final String name;

	FilterSRSType(int srid, String name) {
		this.srid = srid;
		this.name = name;
	}

	public int getSrid() {
		return srid;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return this != SAME_AS_IN_DB ? name :
			Internal.I18N.getString("filter.label.boundingBox.crs.sameAsInDB");
	}

	public static FilterSRSType fromValue(int srid) {
		for (FilterSRSType c: FilterSRSType.values()) {
			if (c.srid == srid) {
				return c;
			}
		}

		return SAME_AS_IN_DB;
	}

}
