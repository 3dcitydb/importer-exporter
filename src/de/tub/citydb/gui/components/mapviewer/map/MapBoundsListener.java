package de.tub.citydb.gui.components.mapviewer.map;

import org.jdesktop.swingx.mapviewer.GeoPosition;

public interface MapBoundsListener {
	public void getMapBounds(GeoPosition[] bbox);
}
