package de.tub.citydb.gui.components.mapviewer.geocoder;

public enum ResultType {
	STREET_ADDRESS("street_address"),
	ROUTE("route"),
	INTERSECTION("intersection"),
	POLITICAL("political"),
	COUNTRY("country"),
	ADMINISTRATIVE_AREA_LEVEL_1("administrative_area_level_1"),
	ADMINISTRATIVE_AREA_LEVEL_2("administrative_area_level_2"),
	ADMINISTRATIVE_AREA_LEVEL_3("administrative_area_level_3"),
	COLLOQUIAL_AREA("colloquial_area"),
	LOCALITY("locality"),
	SUBLOCALITY("sublocality"),
	NEIGHBORHOOD("neighborhood"),
	PREMISE("premise"),
	SUBPREMISE("subpremise"),
	POSTAL_CODE("postal_code"),
	NATURAL_FEATURE("natural_feature"),
	AIRPORT("airport"),
	PARK("park"),
	POINT_OF_INTEREST("point_of_interest"),
	UNKNOWN("unknown");
	
	private final String value;
	
	ResultType(String value) {
		this.value = value;
	}
	
	public static ResultType fromValue(String v) {
		for (ResultType c: ResultType.values()) {
			if (c.value.equalsIgnoreCase(v)) {
				return c;
			}
		}

		return UNKNOWN;
	}
	
	public String toString() {
		return value;
	}
}
