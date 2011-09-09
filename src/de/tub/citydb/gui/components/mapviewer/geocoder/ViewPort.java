package de.tub.citydb.gui.components.mapviewer.geocoder;

import org.jdesktop.swingx.mapviewer.GeoPosition;

public class ViewPort {
	private GeoPosition southWest;
	private GeoPosition northEast;
	
	public ViewPort(GeoPosition southWest, GeoPosition northEast) {
		this.southWest = southWest;
		this.northEast = northEast;
	}
	
	public GeoPosition getSouthWest() {
		return southWest;
	}
	
	public void setSouthWest(GeoPosition southWest) {
		this.southWest = southWest;
	}
	
	public GeoPosition getNorthEast() {
		return northEast;
	}
	
	public void setNorthEast(GeoPosition northEast) {
		this.northEast = northEast;
	}
}
