/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.gui.components.mapviewer.geocoder;

import java.net.URL;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tub.citydb.config.internal.Internal;
import de.tub.citydb.gui.components.mapviewer.MapWindow;

public class Geocoder {
	private static final String GEOCODER_REQUEST_PREFIX_FOR_XML = "http://maps.google.com/maps/api/geocode/xml";
	private static final String ADDRESS_REQUEST = "address";
	private static final String LATLON_REQUEST = "latlng";
	
	public static final GeocoderResponse geocode(String address) {
		return geocode(ADDRESS_REQUEST, address);
	}
	
	public static final GeocoderResponse geocode(GeoPosition latlon) {
		return geocode(LATLON_REQUEST, latlon.getLatitude() + "," + latlon.getLongitude());
	}
	
	public static final GeocoderResponse geocode(String requestType, String requestString) {
		GeocoderResponse geocodingResult = new GeocoderResponse(ResponseType.ADDRESS);
		
		// get language from GUI settings
		String language = Internal.I18N.getLocale().getLanguage();
		if (language.length() != 0)
			language = "&language=" + language;
		
		try {
			URL url = new URL(GEOCODER_REQUEST_PREFIX_FOR_XML + "?" + requestType + "=" + URLEncoder.encode(requestString, "UTF-8") + "&sensor=false" + language);
			Document response = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
			XPath xpath = XPathFactory.newInstance().newXPath();

			// check the response status
			Node status = (Node)xpath.evaluate("/GeocodeResponse/status", response, XPathConstants.NODE);
			geocodingResult.setStatus(StatusCode.fromValue(status.getTextContent()));

			// only proceed if we received some locations
			if (geocodingResult.getStatus() == StatusCode.OK) {
				NodeList resultNodeList = (NodeList)xpath.evaluate("/GeocodeResponse/result", response, XPathConstants.NODESET);
				Location[] locations = new Location[resultNodeList.getLength()];

				for (int j = 0; j < resultNodeList.getLength(); ++j) {
					Location location = new Location();
					Node result = resultNodeList.item(j);

					// get formatted_address
					Node formattedAddress = (Node)xpath.evaluate("formatted_address", result, XPathConstants.NODE);
					location.setFormattedAddress(formattedAddress.getTextContent());

					// get types
					NodeList types = (NodeList)xpath.evaluate("type", result, XPathConstants.NODESET);
					for (int i=0; i < types.getLength(); ++i)
						location.addResultType(ResultType.fromValue(types.item(i).getTextContent()));
					
					// get coordinates
					GeoPosition coordinates = getLatLonFromResponse((NodeList)xpath.evaluate("geometry/location/*", result, XPathConstants.NODESET));
					location.setPosition(coordinates);

					// get location_type
					Node locationType = (Node)xpath.evaluate("geometry/location_type ", result, XPathConstants.NODE);
					location.setLocationType(LocationType.fromValue(locationType.getTextContent()));

					// get viewport
					GeoPosition southWest = getLatLonFromResponse((NodeList)xpath.evaluate("geometry/viewport/southwest/*", result, XPathConstants.NODESET));
					GeoPosition northEast = getLatLonFromResponse((NodeList)xpath.evaluate("geometry/viewport/northeast/*", result, XPathConstants.NODESET));

					if (southWest != null && northEast != null)
						location.setViewPort(southWest, northEast);

					locations[j] = location;
				}

				if (locations.length > 0)
					geocodingResult.setLocations(locations);
			}

			return geocodingResult;
		}  catch (Exception e) {
			geocodingResult.setException(e);
		}

		return geocodingResult;
	}

	public static final GeocoderResponse parseLatLon(String search) {
		GeocoderResponse result = null;

		// parse strings of form "longitude, latitude" where
		// both longitude and latitude are decimal numbers
		String separators = "(\\s*[,|;|\\s]\\s*?)";
		String regex = "([-|\\+]?\\d{1,3}(\\.\\d+?)??)";
		regex += separators;
		regex += "([-|\\+]?\\d{1,2}(\\.\\d+?)??)";

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(search.trim());

		if (matcher.matches()) {
			result = new GeocoderResponse(ResponseType.LAT_LON);
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
			} catch (ParseException e) {
				return null;
			}

			location.setLocationType(LocationType.ROOFTOP);
			result.setLocations(new Location[]{ location });
			result.setStatus(StatusCode.OK);
		}

		return result;
	}

	private static final GeoPosition getLatLonFromResponse(NodeList nodeList) {
		try {
			double lat = Double.NaN;
			double lon = Double.NaN;

			for (int i=0; i < nodeList.getLength(); ++i) {
				Node node = nodeList.item(i);

				if("lat".equals(node.getNodeName())) 
					lat = Double.parseDouble(node.getTextContent());

				if("lng".equals(node.getNodeName())) 
					lon = Double.parseDouble(node.getTextContent());
			}

			return new GeoPosition(lat, lon);
		} catch (Exception e) {
			return null;
		}
	}

}
