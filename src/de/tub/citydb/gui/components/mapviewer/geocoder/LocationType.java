package de.tub.citydb.gui.components.mapviewer.geocoder;

public enum LocationType {
	ROOFTOP("ROOFTOP"),
	RANGE_INTERPOLATED("RANGE_INTERPOLATED"),
	GEOMETRIC_CENTER("GEOMETRIC_CENTER"),
	APPROXIMATE("APPROXIMATE"),
	UNKNOWN("UNKNOWN");
	
	private final String value;
	
	LocationType(String value) {
		this.value = value;
	}
	
	public static LocationType fromValue(String v) {
		for (LocationType c: LocationType.values()) {
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
