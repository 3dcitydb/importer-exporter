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
package de.tub.citydb.sax;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class SAXNamespaceMapper extends XMLFilterImpl {
	private HashMap<String, String> uriMap = new HashMap<String, String>();

	public SAXNamespaceMapper(XMLReader reader) {
		super(reader);
	}

	public void setNamespaceMapping(String oldURI, String newURI) {
		uriMap.put(oldURI, newURI);
	}

	public String getNamespaceMapping(String oldURI) {
		return uriMap.get(oldURI);
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// change URI if needed
		String newURI = uriMap.get(uri);

		if (newURI != null) {
			super.startPrefixMapping(prefix, newURI);
		} else {
			super.startPrefixMapping(prefix, uri);
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		String newURI = uriMap.get(uri);

		if (newURI != null) {
			super.startElement(newURI, localName, qName, atts);
		} else {
			super.startElement(uri, localName, qName, atts);
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		String newURI = uriMap.get(uri);

		if (newURI != null) {
			super.endElement(newURI, localName, qName);
		} else {
			super.endElement(uri, localName, qName);
		}
	}
}
