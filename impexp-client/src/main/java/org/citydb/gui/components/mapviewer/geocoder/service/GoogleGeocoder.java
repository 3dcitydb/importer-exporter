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
package org.citydb.gui.components.mapviewer.geocoder.service;

import org.citydb.config.gui.window.GeocodingServiceName;
import org.citydb.config.i18n.Language;
import org.citydb.gui.components.mapviewer.geocoder.GeocoderResult;
import org.citydb.gui.components.mapviewer.geocoder.Location;
import org.citydb.gui.components.mapviewer.geocoder.LocationType;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GoogleGeocoder implements GeocodingService {
    private final String apiKey;

    public GoogleGeocoder(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public GeocoderResult geocode(String address) throws GeocodingServiceException {
        return geocode("address", address);
    }

    @Override
    public GeocoderResult lookupAddress(GeoPosition latlon, int zoomLevel) throws GeocodingServiceException {
        return geocode("latlng", latlon.getLatitude() + "," + latlon.getLongitude());
    }

    private GeocoderResult geocode(String operation, String requestString) throws GeocodingServiceException {
        String serviceCall;
        try {
            serviceCall = "https://maps.googleapis.com/maps/api/geocode/xml?" +
                    operation + '=' + URLEncoder.encode(requestString, StandardCharsets.UTF_8.displayName()) +
                    "&key=" + apiKey;

            // add language parameter
            String language = Language.I18N.getLocale().getLanguage();
            if (!language.isEmpty())
                serviceCall += "&language=" + language;

        } catch (UnsupportedEncodingException e) {
            throw new GeocodingServiceException("Failed to construct the geocoding service call.", e);
        }

        try (InputStream stream = new URL(serviceCall).openStream()) {
            Document response = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
            XPath xpath = XPathFactory.newInstance().newXPath();

            GeocoderResult geocodingResult = new GeocoderResult();

            // check the response status
            Node statusNode = (Node) xpath.evaluate("/GeocodeResponse/status", response, XPathConstants.NODE);
            String status = statusNode.getTextContent();

            if ("OK".equalsIgnoreCase(status)) {
                NodeList resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result", response, XPathConstants.NODESET);
                for (int j = 0; j < resultNodeList.getLength(); ++j) {
                    Location location = new Location();
                    Node result = resultNodeList.item(j);

                    // get formatted_address
                    Node formattedAddress = (Node) xpath.evaluate("formatted_address", result, XPathConstants.NODE);
                    location.setFormattedAddress(formattedAddress.getTextContent());

                    // get types
                    NodeList typesNode = (NodeList) xpath.evaluate("type", result, XPathConstants.NODESET);
                    List<String> types = new ArrayList<>();
                    for (int i = 0; i < typesNode.getLength(); ++i)
                        types.add(typesNode.item(i).getTextContent());

                    if (!types.isEmpty())
                        location.addAttribute("types", types);

                    // get coordinates
                    GeoPosition coordinates = getLatLonFromResponse((NodeList) xpath.evaluate("geometry/location/*", result, XPathConstants.NODESET));
                    location.setPosition(coordinates);

                    // get location_type
                    Node locationType = (Node) xpath.evaluate("geometry/location_type ", result, XPathConstants.NODE);
                    location.setLocationType("PRECISE".equalsIgnoreCase(locationType.getTextContent()) ?
                            LocationType.PRECISE : LocationType.APPROXIMATE);

                    // get viewport
                    GeoPosition southWest = getLatLonFromResponse((NodeList) xpath.evaluate("geometry/viewport/southwest/*", result, XPathConstants.NODESET));
                    GeoPosition northEast = getLatLonFromResponse((NodeList) xpath.evaluate("geometry/viewport/northeast/*", result, XPathConstants.NODESET));
                    location.setViewPort(southWest, northEast);

                    geocodingResult.addLocation(location);
                }
            } else if (!"ZERO_RESULTS".equalsIgnoreCase(status)) {
                GeocodingServiceException e = new GeocodingServiceException("The geocoding service responded with the status " + status + ".");
                Node errorMessage = (Node) xpath.evaluate("/GeocodeResponse/error_message", response, XPathConstants.NODE);
                if (errorMessage != null)
                    e.addMessage(errorMessage.getTextContent());

                throw e;
            }

            return geocodingResult;
        } catch (IOException e) {
            throw new GeocodingServiceException("Failed to invoke the geocoding service.", e);
        } catch (GeocodingServiceException e) {
            throw e;
        } catch (Throwable e) {
            throw new GeocodingServiceException("Failed to parse the geocoding service response.", e);
        }
    }

    private GeoPosition getLatLonFromResponse(NodeList nodeList) throws GeocodingServiceException {
        try {
            double lat = Double.NaN;
            double lon = Double.NaN;

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node node = nodeList.item(i);

                if ("lat".equalsIgnoreCase(node.getNodeName()))
                    lat = Double.parseDouble(node.getTextContent());

                if ("lng".equalsIgnoreCase(node.getNodeName()))
                    lon = Double.parseDouble(node.getTextContent());
            }

            return new GeoPosition(lat, lon);
        } catch (Throwable e) {
            throw new GeocodingServiceException("Failed to parse the geocoding service response.", e);
        }
    }

    @Override
    public GeocodingServiceName getName() {
        return GeocodingServiceName.GOOGLE_GEOCODING_API;
    }
}
