package de.tub.citydb.gui.components.mapviewer.geocoder;

public class GeocoderResponse {	
	private final ResponseType type;
	private StatusCode status = StatusCode.ERROR;
	private Exception exception;
	private Location[] locations;

	public GeocoderResponse(ResponseType type) {
		this.type = type;
	}
	
	public ResponseType getType() {
		return type;
	}

	public StatusCode getStatus() {
		return status;
	}

	public void setStatus(StatusCode status) {
		this.status = status;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public Location[] getLocations() {
		return locations;
	}

	public void setLocations(Location[] locations) {
		this.locations = locations;
	}

}
