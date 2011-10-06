/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
package de.tub.citydb.modules.kml.database;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.tub.citydb.config.project.global.HttpProxy;
import de.tub.citydb.log.Logger;

public class ElevationServiceHandler extends DefaultHandler {

	private static final String STATUS = "status";
	private static final String LAT = "lat";
	private static final String LNG = "lng";
	private static final String ELEVATION = "elevation";
	private static final String OK = "OK";

	private static final double TOLERANCE = 0.000001;
	private static final int POINTS_IN_A_URL = 55;

	SAXParser saxParser = null;
	String currentElement = "";
	StringBuffer textBuffer = null;

	String status = "";
	double elevation = 0;

	double minElevation = Double.MAX_VALUE;
	int location = -1;

	double minElevationLat = 0;
	double minElevationLong = 0;
	double lastLat = 0;
	double lastLong = 0;

	HttpProxy proxy;

	public ElevationServiceHandler (HttpProxy proxy) {
		this.proxy = proxy;
	}

	public double getZOffset(double[] candidateCoords) throws Exception {

		double zOffset = 0;
		location = -1;
		minElevation = Double.MAX_VALUE;

		if (saxParser == null) {
			// Use the default (non-validating) parser
			SAXParserFactory factory = SAXParserFactory.newInstance();
			try {
				saxParser = factory.newSAXParser();
			}
			catch (Throwable t) {
				t.printStackTrace();
			}
		}

		List<String> elevationStringList = new ArrayList<String>();
		int index = 0;
		while (index < candidateCoords.length) { 
			String elevationString = "http://maps.google.com/maps/api/elevation/xml?sensor=false&locations=";
			for (int i = 0; i < POINTS_IN_A_URL; i++) { // URL length must be under 2048
				String latitude = new BigDecimal(candidateCoords[index+1]).toPlainString();
				if (latitude.length() > 15) latitude = latitude.substring(0, 15);
				String longitude = new BigDecimal(candidateCoords[index]).toPlainString();
				if (longitude.length() > 15) longitude = longitude.substring(0, 15);

				elevationString = elevationString + latitude + "," + longitude + "|";
				index = index + 3;
				if (index >= candidateCoords.length) break;
			}
			elevationString = elevationString.substring(0, elevationString.length()-1); // remove last pipe
			elevationStringList.add(elevationString);
		}

		try {
			for (String elevationString: elevationStringList) {
				URL elevationService = new URL(elevationString);
				URLConnection connection;
				
				if (proxy.isSetUseProxy() && proxy.hasValidProxySettings()) {
					connection = (HttpURLConnection)elevationService.openConnection(proxy.getProxy());
					if (proxy.hasUserCredentials())
						connection.setRequestProperty("Proxy-Authorization", "Basic " + proxy.getBase64EncodedCredentials());
				}
				else {
					connection = elevationService.openConnection();
				}

				saxParser.parse(connection.getInputStream(), this);
			}
		}
		catch (Throwable t) {
			Logger.getInstance().error("Could not access Elevation API. Please check your network settings.");
			//			t.printStackTrace();
		}

		if (!status.equalsIgnoreCase(OK)) {
			if (status.length() > 0) {
				Logger.getInstance().warn("Elevation API returned " + status);
			}
			throw new Exception("Elevation API returned " + status);
		}

		for (int i = 0; i < candidateCoords.length; i = i + 3) {
			if (Math.abs(minElevationLong - candidateCoords[i]) > TOLERANCE) {
				continue;
			}
			if (Math.abs(minElevationLat - candidateCoords[i+1]) > TOLERANCE) {
				continue;
			}
			zOffset = minElevation - candidateCoords[i+2];
			break;
		}

		return zOffset;
	}

	public void startDocument() throws SAXException	{}

	public void endDocument() throws SAXException {}

	public void startElement(String namespaceURI,
			String sName, // simple name
			String qName, // qualified name
			Attributes attrs) throws SAXException {
		String eName = sName; // element name
		if ("".equals(eName)) {
			eName = qName; // not namespace-aware
		}

		currentElement = eName;
	}

	public void endElement(String namespaceURI,
			String sName, // simple name
			String qName  // qualified name
			) throws SAXException {
		String eName = sName; // element name
		if ("".equals(eName)) eName = qName; // not namespace-aware

		String value = textBuffer.toString().trim();
		if(!value.equals("")) {
			if (currentElement.equalsIgnoreCase(STATUS)) {
				status = value;
			}

			else if (currentElement.equalsIgnoreCase(LAT)) {
				lastLat = Double.parseDouble(value);
			}
			else if (currentElement.equalsIgnoreCase(LNG)) {
				lastLong = Double.parseDouble(value);
			}
			else if (currentElement.equalsIgnoreCase(ELEVATION)) {
				elevation = Double.parseDouble(value);
				location++;
				if (elevation < minElevation) {
					minElevation = elevation;
					minElevationLat = lastLat;
					minElevationLong = lastLong;
				}
			}
		}

		textBuffer = null; 
	} 


	public void characters(char buf[], int offset, int len) throws SAXException
	{
		String s = new String(buf, offset, len);
		if (textBuffer == null) {
			textBuffer = new StringBuffer(s);
		}
		else {
			textBuffer.append(s);
		}
	} 

}
