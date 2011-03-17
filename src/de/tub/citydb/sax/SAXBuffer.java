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

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import de.tub.citydb.sax.events.Characters;
import de.tub.citydb.sax.events.EndDocument;
import de.tub.citydb.sax.events.EndElement;
import de.tub.citydb.sax.events.EndPrefixMapping;
import de.tub.citydb.sax.events.Location;
import de.tub.citydb.sax.events.SAXEvent;
import de.tub.citydb.sax.events.SAXEvent.EventType;
import de.tub.citydb.sax.events.StartDocument;
import de.tub.citydb.sax.events.StartElement;
import de.tub.citydb.sax.events.StartPrefixMapping;

public class SAXBuffer implements ContentHandler {
	private SAXEvent head;
	private SAXEvent tail;

	private SAXEvent lastElement = new StartDocument();
	private boolean trackLocation;
	private Locator locator;

	public SAXBuffer() {
		this(false);
	}

	public SAXBuffer(boolean trackLocation) {
		this.trackLocation = trackLocation;
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		// we do not record this event
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		if (trackLocation)
			this.locator = locator;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (lastElement.getType() == EventType.START_ELEMENT)
			addEvent(new Characters(ch, start, length, getLocation()));
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		// we do not record this event
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		// we do not record this event
	}

	@Override
	public void startDocument() throws SAXException {
		addEvent(new StartDocument());
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		StartElement element = new StartElement(uri, localName, atts, getLocation());
		if (lastElement.getType() == EventType.START_ELEMENT)
			tail = lastElement;
		
		addEvent(element);
		lastElement = element;
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		addEvent(new StartPrefixMapping(prefix, uri));
	}

	@Override
	public void endDocument() throws SAXException {
		addEvent(new EndDocument());
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		EndElement element = new EndElement(uri, localName, getLocation());
		addEvent(element);
		lastElement = element;
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		addEvent(new EndPrefixMapping(prefix));
	}

	public boolean isEmpty() {
		return head == null;
	}

	public void clear() {
		head = tail = null;
		lastElement = new StartDocument();
	}

	public void addEvent(SAXEvent event) {
		if (!isEmpty()) {
			tail.setNext(event);
			tail = event;
		} else
			head = tail = event;
	}

	public SAXEvent getFirstEvent() {
		return head;
	}

	public void removeFirstEvent() {
		head = head.next();
	}
	
	public boolean isTrackLocation() {
		return trackLocation;
	}

	private Location getLocation() {
		return trackLocation ? 
				new Location(
						locator.getLineNumber(), 
						locator.getColumnNumber()) : null;
	}
}
