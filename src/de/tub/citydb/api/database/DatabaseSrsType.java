package de.tub.citydb.api.database;

public enum DatabaseSrsType {
    GEOGCS("Geographic"),
    PROJCS("Projected"),
	
//	PROJECTED("Projected"),
//	GEOGRAPHIC2D("Geographic2D"),
//	GEOCENTRIC("Geocentric"),
//	VERTICAL("Vertical"),
//	ENGINEERING("Engineering"),
//	COMPOUND("Compound"),
//	GEOGENTRIC("Geogentric"),
//	GEOGRAPHIC3D("Geographic3D"),
	UNKNOWN("n/a");
	
	private final String value;

	DatabaseSrsType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DatabaseSrsType fromValue(String v) {
        for (DatabaseSrsType c : DatabaseSrsType.values()) {
            if (c.value.toLowerCase().equals(v.toLowerCase())) {
                return c;
            }
        }

        return UNKNOWN;
    }
    
    public String toString() {
		return value;
	}
}
