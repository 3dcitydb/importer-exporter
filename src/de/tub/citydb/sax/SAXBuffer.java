package de.tub.citydb.sax;

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import de.tub.citydb.sax.events.Characters;
import de.tub.citydb.sax.events.Comment;
import de.tub.citydb.sax.events.DocumentLocation;
import de.tub.citydb.sax.events.EndCDATA;
import de.tub.citydb.sax.events.EndDTD;
import de.tub.citydb.sax.events.EndDocument;
import de.tub.citydb.sax.events.EndElement;
import de.tub.citydb.sax.events.EndEntity;
import de.tub.citydb.sax.events.EndPrefixMapping;
import de.tub.citydb.sax.events.IgnorableWhitespace;
import de.tub.citydb.sax.events.ProcessingInstruction;
import de.tub.citydb.sax.events.SAXEvent;
import de.tub.citydb.sax.events.SkippedEntity;
import de.tub.citydb.sax.events.StartCDATA;
import de.tub.citydb.sax.events.StartDTD;
import de.tub.citydb.sax.events.StartDocument;
import de.tub.citydb.sax.events.StartElement;
import de.tub.citydb.sax.events.StartEntity;
import de.tub.citydb.sax.events.StartPrefixMapping;

public class SAXBuffer implements ContentHandler, LexicalHandler {
	protected Vector<SAXEvent> saxEvents;
	private Locator locator;
	private boolean trackLocation;

	public SAXBuffer() {
		this(false);
	}
	
	public SAXBuffer(boolean trackLocation) {
		saxEvents = new Vector<SAXEvent>();
		this.trackLocation = trackLocation;
	}
	
	// ContentHandler Interface Implementation
	@Override
	public void skippedEntity(String name) throws SAXException {
		saxEvents.add(new SkippedEntity(name, getDocumentLocation()));
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		if (trackLocation)
			this.locator = locator;
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		saxEvents.add(new Characters(ch, start, length, getDocumentLocation()));
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		saxEvents.add(new IgnorableWhitespace(ch, start, length, getDocumentLocation()));
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		saxEvents.add(new ProcessingInstruction(target, data, getDocumentLocation()));
	}

	@Override
	public void startDocument() throws SAXException {
		saxEvents.add(StartDocument.SINGLETON);
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		saxEvents.add(new StartElement(uri, localName, qName, atts, getDocumentLocation()));
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		saxEvents.add(new StartPrefixMapping(prefix, uri, getDocumentLocation()));
	}

	@Override
	public void endDocument() throws SAXException {
		saxEvents.add(EndDocument.SINGLETON);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		saxEvents.add(new EndElement(uri, localName, qName, getDocumentLocation()));
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		saxEvents.add(new EndPrefixMapping(prefix, getDocumentLocation()));
	}

	// LexicalHandler Interface Implementation
	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		saxEvents.add(new Comment(ch, start, length, getDocumentLocation()));
	}

	@Override
	public void endCDATA() throws SAXException {
		saxEvents.add(EndCDATA.SINGLETON);
	}

	@Override
	public void endDTD() throws SAXException {
		saxEvents.add(EndDTD.SINGLETON);
	}

	@Override
	public void endEntity(String name) throws SAXException {
		saxEvents.add(new EndEntity(name, getDocumentLocation()));
	}

	@Override
	public void startCDATA() throws SAXException {
		saxEvents.add(StartCDATA.SINGELTON);
	}

	@Override
	public void startDTD(String name, String publicId, String systemId)	throws SAXException {
		saxEvents.add(new StartDTD(name, publicId, systemId, getDocumentLocation()));
	}

	@Override
	public void startEntity(String name) throws SAXException {
		saxEvents.add(new StartEntity(name, getDocumentLocation()));
	}

	// Public methods
	public boolean isEmpty() {
		return saxEvents.isEmpty();
	}

	public Vector<SAXEvent> getEvents() {
		return saxEvents;
	}
	
	public void clear() {
		saxEvents.clear();
	}

	public void renew() {
		saxEvents = new Vector<SAXEvent>();
	}

	public void addEvent(SAXEvent event) {
		saxEvents.add(event);
	}
	
	// Private methods	
	private DocumentLocation getDocumentLocation() {
		if (locator != null)
			return new DocumentLocation(locator.getLineNumber(), locator.getColumnNumber());

		return null;
	}
}
