package org.citydb.gui.components.mapviewer.geocoder.service;

import org.citydb.config.gui.window.GeocodingServiceName;
import org.citydb.gui.components.mapviewer.geocoder.GeocoderResult;
import org.jdesktop.swingx.mapviewer.GeoPosition;

public interface GeocodingService {
    GeocoderResult geocode(String address) throws GeocodingServiceException;
    GeocoderResult lookupAddress(GeoPosition latlon, int zoomLevel) throws GeocodingServiceException;
    GeocodingServiceName getName();
}