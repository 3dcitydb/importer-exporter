package de.tub.citydb.gui.components.mapviewer.map;

import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Waypoint;

public class DefaultWaypoint extends Waypoint {
	private final WaypointType type;
	
	public enum WaypointType {
		PRECISE,
		APPROXIMATE,
		REVERSE
	}
	
	public DefaultWaypoint(GeoPosition pos, WaypointType type) {
		super(pos);
		this.type = type;
	}
	
	public DefaultWaypoint(double latitude, double longitude, WaypointType type) {
		super(latitude, longitude);
		this.type = type;
	}
	
	public WaypointType getType() {
		return type;
	}
}
