package de.tub.citydb.api.database;

public enum DatabaseSrsType {
    PROJECTED("PROJCS", "Projected"),
	GEOGRAPHIC2D("GEOGCS", "Geographic2D"),
	GEOCENTRIC("n/a", "Geocentric"),
	VERTICAL("n/a", "Vertical"),
	ENGINEERING("n/a", "Engineering"),
	COMPOUND("n/a", "Compound"),
	GEOGENTRIC("n/a", "Geogentric"),
	GEOGRAPHIC3D("n/a", "Geographic3D"),
	UNKNOWN("", "n/a");
	
	private final String dbName;
	private final String printName;

	DatabaseSrsType(String dbName, String printName) {
        this.dbName = dbName;
        this.printName = printName;
    }

    public String getDatabaseName() {
        return dbName;
    }
    
    public String getPrintName() {
    	return printName;
    }

    public static DatabaseSrsType fromValue(String v) {
        for (DatabaseSrsType c : DatabaseSrsType.values()) {
            if (c.dbName.toLowerCase().equals(v.toLowerCase())) {
                return c;
            }
        }

        return UNKNOWN;
    }
    
    public String toString() {
		return printName;
	}
}
