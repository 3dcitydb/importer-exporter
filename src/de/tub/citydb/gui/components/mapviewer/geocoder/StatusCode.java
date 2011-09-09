package de.tub.citydb.gui.components.mapviewer.geocoder;

public enum StatusCode {
	OK("OK"),
	ZERO_RESULTS("ZERO_RESULTS"),
	OVER_QUERY_LIMIT("OVER_QUERY_LIMIT"),
	REQUEST_DENIED("REQUEST_DENIED"),
	ERROR("ERROR");
	
	private final String value;
	
	StatusCode(String value) {
		this.value = value;
	}
	
	public static StatusCode fromValue(String v) {
		for (StatusCode c: StatusCode.values()) {
			if (c.value.equalsIgnoreCase(v)) {
				return c;
			}
		}

		return ERROR;
	}
	
	public String toString() {
		return value;
	}
}
