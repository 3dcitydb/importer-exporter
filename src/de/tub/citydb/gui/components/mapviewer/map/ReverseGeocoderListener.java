package de.tub.citydb.gui.components.mapviewer.map;

import de.tub.citydb.gui.components.mapviewer.geocoder.GeocoderResponse;
import de.tub.citydb.gui.components.mapviewer.geocoder.Location;

public interface ReverseGeocoderListener {
	public void searching();
	public void process(Location location);
	public void error(GeocoderResponse response);
}
