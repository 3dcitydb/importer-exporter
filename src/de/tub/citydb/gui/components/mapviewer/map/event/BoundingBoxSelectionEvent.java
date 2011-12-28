package de.tub.citydb.gui.components.mapviewer.map.event;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import de.tub.citydb.api.event.Event;

public class BoundingBoxSelectionEvent extends Event {
	private final GeoPosition[] boundingBox;
	
	public BoundingBoxSelectionEvent(GeoPosition[] boundingBox, Object source) {
		super(MapEvents.BOUNDING_BOX_SELECTION, source);
		this.boundingBox = boundingBox;
	}

	public GeoPosition[] getBoundingBox() {
		return boundingBox;
	}
	
}
