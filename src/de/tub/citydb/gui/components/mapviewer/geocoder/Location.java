package de.tub.citydb.gui.components.mapviewer.geocoder;

import java.util.ArrayList;
import java.util.List;

import org.jdesktop.swingx.mapviewer.GeoPosition;

public class Location {
	private List<ResultType> resultTypes;
	private LocationType locationType = LocationType.UNKNOWN;
	private String formattedAddress;
	private GeoPosition position;
	private ViewPort viewPort;

	public Location() {
		resultTypes = new ArrayList<ResultType>();
	}
	
	public void addResultType(ResultType type) {
		resultTypes.add(type);
	}
	
	public List<ResultType> getResultTypes() {
		return resultTypes;
	}
	
	public LocationType getLocationType() {
		return locationType;
	}

	public void setLocationType(LocationType type) {
		this.locationType = type;
	}

	public String getFormattedAddress() {
		return formattedAddress;
	}
	
	public void setFormattedAddress(String formattedAddress) {
		this.formattedAddress = formattedAddress;
	}
	
	public GeoPosition getPosition() {
		return position;
	}
	
	public void setPosition(GeoPosition position) {
		this.position = position;
	}
	
	public void setViewPort(GeoPosition southWest, GeoPosition northEast) {
		viewPort = new ViewPort(southWest, northEast);
	}

	public ViewPort getViewPort() {
		return viewPort;
	}
	
	@Override
	public String toString() {
		return formattedAddress;
	}
	
}
