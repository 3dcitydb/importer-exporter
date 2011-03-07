package de.tub.citydb.config.internal;


public enum DBVersioning {
	ON("ON"),
	OFF("OFF"),
	PARTLY("PARTLY");
	
	private final String value;
	
	private DBVersioning(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

	public static DBVersioning fromValue(String v) {
		for (DBVersioning c: DBVersioning.values()) {
			if (c.value.equals(v)) {
				return c;
			}
		}

		return OFF;
	}
	
	public String toString() {
		return value;
	}
}
