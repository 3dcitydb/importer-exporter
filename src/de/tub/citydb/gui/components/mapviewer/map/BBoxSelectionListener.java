package de.tub.citydb.gui.components.mapviewer.map;

import org.jdesktop.swingx.mapviewer.GeoPosition;

public interface BBoxSelectionListener {
	public void bboxSelected(GeoPosition[] bbox);
}
