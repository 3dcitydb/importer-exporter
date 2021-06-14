/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.gui.map.geocoder;

import org.citydb.config.gui.window.GeocodingServiceName;
import org.citydb.gui.map.MapWindow;
import org.citydb.gui.map.geocoder.service.GeocodingService;
import org.citydb.gui.map.geocoder.service.GeocodingServiceException;
import org.citydb.gui.map.geocoder.service.OSMGeocoder;
import org.jdesktop.swingx.mapviewer.GeoPosition;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Geocoder {
	private static Geocoder instance;
	private GeocodingService service;

	private Geocoder() {
		// just to thwart instantiation
		service = new OSMGeocoder();
	}

	public static synchronized Geocoder getInstance() {
		if (instance == null)
			instance = new Geocoder();

		return instance;
	}

	public GeocoderResult geocode(String address) throws GeocodingServiceException {
		GeocoderResult result = parseLatLon(address);
		if (result == null)
			result = service.geocode(address);

		return result != null ? result : new GeocoderResult();
	}
	
	public GeocoderResult lookupAddress(GeoPosition latlon, int zoomLevel) throws GeocodingServiceException {
		GeocoderResult result = service.lookupAddress(latlon, zoomLevel);
		return result != null ? result : new GeocoderResult();
	}

	public GeocodingServiceName getGeocodingServiceName() {
		return service.getName();
	}

	public void setGeocodingService(GeocodingService service) {
		if (service != null)
			this.service = service;
	}

	private GeocoderResult parseLatLon(String search) {
		// parse strings of form "longitude, latitude" where
		// both longitude and latitude are decimal numbers
		String separators = "(\\s*[,|;|\\s]\\s*?)";
		String regex = "([-|\\+]?\\d{1,3}(\\.\\d+?)??)";
		regex += separators;
		regex += "([-|\\+]?\\d{1,2}(\\.\\d+?)??)";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(search.trim());

		if (matcher.matches()) {
			GeocoderResult result = new GeocoderResult();
			Location location = new Location();

			try {
				NumberFormat format = NumberFormat.getInstance(Locale.ENGLISH);
				double lat = format.parse(matcher.group(1)).doubleValue();
				double lon = format.parse(matcher.group(4)).doubleValue();

				if (Math.abs(lon) >= 180 || Math.abs(lat) >= 85)
					return null;

				double offset = 0.001349;
				location.setPosition(new GeoPosition(lat, lon));
				location.setViewPort(
						new GeoPosition(lat - offset, lon - offset),
						new GeoPosition(lat + offset, lon + offset));

				location.setFormattedAddress(MapWindow.LAT_LON_FORMATTER.format(location.getPosition().getLatitude())
						+ ", " + MapWindow.LAT_LON_FORMATTER.format(location.getPosition().getLongitude()));

				location.setLocationType(LocationType.PRECISE);
				result.addLocation(location);

				return result;
			} catch (ParseException e) {
				//
			}
		}

		return null;
	}

}
