/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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
package org.citydb.core.query.geometry;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SrsNameParser {
    private final Matcher matcher;
    private Pattern httpPattern;
    private Pattern urnPattern;
    private Pattern epsgPattern;

    private final String httpEncoding = "http://www.opengis.net/def/crs/epsg/0/(EPSG code)";
    private final String urnEncoding = "urn:ogc:def:crs:EPSG:(version):(EPSG code)";
    private final String epsgEncoding = "EPSG:(EPSG code)";

    public SrsNameParser() {
        matcher = Pattern.compile("").matcher("");
    }

    public int getEPSGCode(String srsName) throws SrsParseException {
        if (srsName == null)
            return 0;

        if (srsName.startsWith("http"))
            return parseHttp(srsName);
        else if (srsName.startsWith("urn"))
            return parseURN(srsName);
        else if (srsName.startsWith("EPSG"))
            return parseEPSG(srsName);
        else {
            StringBuilder message = new StringBuilder()
                    .append("Unsupported encoding of srsName. Supported encoding schemes are ")
                    .append("'").append(httpEncoding).append("', ")
                    .append("'").append(urnEncoding).append("', ")
                    .append("'").append(epsgEncoding).append("'.");

            throw new SrsParseException(message.toString());
        }
    }

    private int parseURN(String srsName) throws SrsParseException {
        if (urnPattern == null)
            urnPattern = Pattern.compile("urn:ogc:def:crs(?:,crs)?:([^:]+?):(?:[^:]*?):([^,]+?)(?:,.*)?");

        matcher.reset(srsName).usePattern(urnPattern);
        if (matcher.matches()) {
            if (matcher.group(1).equalsIgnoreCase("epsg")) {
                try {
                    return Integer.parseInt(matcher.group(2));
                } catch (NumberFormatException e) {
                    throw new SrsParseException("Failed to interpret EPSG code from the srsName '" + srsName + "'.");
                }
            } else
                throw new SrsParseException("Only EPSG is supported as CRS authority.");

        } else
            throw new SrsParseException("The srsName attribute value has to be encoded as '" + httpEncoding + "'.");
    }

    private int parseHttp(String srsName) throws SrsParseException {
        if (httpPattern == null)
            httpPattern = Pattern.compile("http://www.opengis.net/def/crs/([^/]+?)/0/([^/]+?)(?:/.*)?");

        matcher.reset(srsName).usePattern(httpPattern);
        if (matcher.matches()) {
            if (matcher.group(1).equalsIgnoreCase("epsg")) {
                try {
                    return Integer.parseInt(matcher.group(2));
                } catch (NumberFormatException e) {
                    throw new SrsParseException("Failed to interpret EPSG code from the srsName '" + srsName + "'.");
                }
            } else
                throw new SrsParseException("Only EPSG is supported as CRS authority.");

        } else
            throw new SrsParseException("The srsName attribute value has to be encoded as '" + httpEncoding + "'.");
    }

    private int parseEPSG(String srsName) throws SrsParseException {
        if (epsgPattern == null)
            epsgPattern = Pattern.compile("EPSG:([0-9]+)");

        matcher.reset(srsName).usePattern(epsgPattern);
        if (matcher.matches()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                throw new SrsParseException("Failed to interpret EPSG code from the srsName '" + srsName + "'.");
            }
        } else
            throw new SrsParseException("The srsName attribute value has to be encoded as '" + epsgEncoding + "'.");
    }

}
