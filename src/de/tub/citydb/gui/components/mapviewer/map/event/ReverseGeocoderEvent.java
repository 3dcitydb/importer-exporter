package de.tub.citydb.gui.components.mapviewer.map.event;

import de.tub.citydb.api.event.Event;
import de.tub.citydb.gui.components.mapviewer.geocoder.GeocoderResponse;
import de.tub.citydb.gui.components.mapviewer.geocoder.Location;

public class ReverseGeocoderEvent extends Event {
	private final ReverseGeocoderStatus status;
	private final Location location;
	private final GeocoderResponse response;
	
	public enum ReverseGeocoderStatus {
		SEARCHING,
		RESULT,
		ERROR
	}	
	
	public ReverseGeocoderEvent(Object source) {
		super(MapEvents.REVERSE_GEOCODER, source);
		this.status = ReverseGeocoderStatus.SEARCHING;
		location = null;
		response = null;
	}
	
	public ReverseGeocoderEvent(Location location, Object source) {
		super(MapEvents.REVERSE_GEOCODER, source);
		this.status = ReverseGeocoderStatus.RESULT;
		this.location = location;
		response = null;
	}
	
	public ReverseGeocoderEvent(GeocoderResponse response, Object source) {
		super(MapEvents.REVERSE_GEOCODER, source);
		this.status = ReverseGeocoderStatus.ERROR;
		location = null;
		this.response = response;
	}

	public ReverseGeocoderStatus getStatus() {
		return status;
	}

	public Location getLocation() {
		return location;
	}

	public GeocoderResponse getResponse() {
		return response;
	}
	
}
