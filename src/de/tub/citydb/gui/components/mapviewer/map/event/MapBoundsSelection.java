package de.tub.citydb.gui.components.mapviewer.map.event;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import de.tub.citydb.api.event.Event;

public class MapBoundsSelection extends Event {
	private final GeoPosition[] boundingBox;
	
	public MapBoundsSelection(GeoPosition[] boundingBox, Object source) {
		super(MapEvents.MAP_BOUNDS, source);
		this.boundingBox = boundingBox;
	}

	public GeoPosition[] getBoundingBox() {
		return boundingBox;
	}
	
}
