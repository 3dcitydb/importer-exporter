/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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
package org.citydb.modules.kml.util;

import org.citydb.config.Config;
import org.citydb.log.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ElevationServiceHandler {
	private static final ReentrantLock runLock = new ReentrantLock();
	private final Logger log = Logger.getInstance();

	private final String apiKey;
	private final String STATUS = "status";
	private final String LAT = "lat";
	private final String LNG = "lng";
	private final String ELEVATION = "elevation";
	private final String OK = "OK";
	private final String ERROR_MESSAGE = "error_message";

	private final double TOLERANCE = 0.000001;
	private final int POINTS_IN_A_URL = 55;

	SAXParser saxParser;
	String currentElement = "";
	StringBuilder textBuffer;

	String status = "";
	double elevation = 0;
	String errorMessage;

	double minElevation = Double.MAX_VALUE;
	int location = -1;

	double minElevationLat = 0;
	double minElevationLong = 0;
	double lastLat = 0;
	double lastLong = 0;

	public ElevationServiceHandler(Config config) {
		apiKey = config.getGlobalConfig().getApiKeys().isSetGoogleElevation() ?
				config.getGlobalConfig().getApiKeys().getGoogleElevation() : "";
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
				log.logStackTrace(t);
			}
		}

		List<String> elevationStringList = new ArrayList<>();
		int index = 0;
		while (index < candidateCoords.length) {
			StringBuilder builder = new StringBuilder("https://maps.googleapis.com/maps/api/elevation/xml?")
					.append("&key=").append(apiKey).append("&locations=");

			for (int i = 0; i < POINTS_IN_A_URL; i++) { // URL length must be under 2048
				String latitude = new BigDecimal(candidateCoords[index+1]).toPlainString();
				if (latitude.length() > 15) latitude = latitude.substring(0, 15);
				String longitude = new BigDecimal(candidateCoords[index]).toPlainString();
				if (longitude.length() > 15) longitude = longitude.substring(0, 15);

				builder.append(latitude).append(",").append(longitude).append("|");
				index = index + 3;
				if (index >= candidateCoords.length) break;
			}

			String elevationString = builder.toString().substring(0, builder.length()-1); // remove last pipe
			elevationStringList.add(elevationString);
		}

		for (String elevationString: elevationStringList) {
			waitForAccess(); // avoid "OVER_QUERY_LIMIT" from elevation service; max 10 calls/sec are allowed
			ElevationServiceCaller elevationServiceCaller = new ElevationServiceCaller(elevationString);
			elevationServiceCaller.run();
		}

		if (!status.equalsIgnoreCase(OK)) {
			if (status.length() > 0) {
				log.error("Elevation API returned " + status);
				if (errorMessage != null)
					log.error(errorMessage);
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

	private static void waitForAccess() {
		try {
			runLock.lock();
			// pause interval: 100 millis should be enough, but experience says it is not!
			Thread.sleep(200);
		}
		catch (Exception ignored) {}
		finally {
			runLock.unlock();
		}
	}
	
	private class ElevationServiceCaller extends DefaultHandler implements Runnable {
		private String elevationString;

		private ElevationServiceCaller (String elevationString) {
			this.elevationString = elevationString;
		}
		
		public void run() {
			try {
				// for debugging purposes
//				Thread.currentThread().setName(this.getClass().getSimpleName());
//				SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS");
//				log.info("ElevationServiceCaller started at " + dateFormatter.format(new Date(System.currentTimeMillis())));

				URL elevationService = new URL(elevationString);
				saxParser.parse(elevationService.openStream(), this);
			}
			catch (Throwable t) {
				log.error("Could not access Elevation API. Please check your network settings.");
			}
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
				else if (currentElement.equalsIgnoreCase(ERROR_MESSAGE)) {
					errorMessage = value;
				}
			}

			textBuffer = null; 
		} 


		public void characters(char buf[], int offset, int len) throws SAXException {
			String s = new String(buf, offset, len);
			if (textBuffer == null) {
				textBuffer = new StringBuilder(s);
			}
			else {
				textBuffer.append(s);
			}
		} 
	}

}
