package de.tub.citydb.sax;

import java.util.Iterator;
import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

public class SAXBuffer implements ContentHandler, LexicalHandler {

	protected Vector<SAXEvent> saxEvents;

	public SAXBuffer() {
		saxEvents = new Vector<SAXEvent>();
	}

	public SAXBuffer(Vector<SAXEvent> saxEvents) {
		this.saxEvents = saxEvents;
	}

	public SAXBuffer(SAXBuffer saxBuffer) {
		this.saxEvents = new Vector<SAXEvent>(saxBuffer.saxEvents);
	}

	// ContentHandler Interface Implementation

	@Override
	public void skippedEntity(String name) throws SAXException {
		saxEvents.add(new SkippedEntity(name));
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		// we do not record this event
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		saxEvents.add(new Characters(ch, start, length));
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		saxEvents.add(new IgnorableWhitespace(ch, start, length));
	}

	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		saxEvents.add(new ProcessingInstruction(target, data));
	}

	@Override
	public void startDocument() throws SAXException {
		saxEvents.add(StartDocument.SINGLETON);
	}

	@Override
	public void startElement(String uri, String localName, String qName,	Attributes atts) throws SAXException {
		saxEvents.add(new StartElement(uri, localName, qName, atts));
	}

	@Override
	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		saxEvents.add(new StartPrefixMapping(prefix, uri));
	}

	@Override
	public void endDocument() throws SAXException {
		saxEvents.add(EndDocument.SINGLETON);
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		saxEvents.add(new EndElement(uri, localName, qName));
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		saxEvents.add(new EndPrefixMapping(prefix));
	}

	// LexicalHandler Interface Implementation

	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		saxEvents.add(new Comment(ch, start, length));
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
		saxEvents.add(new EndEntity(name));
	}

	@Override
	public void startCDATA() throws SAXException {
		saxEvents.add(StartCDATA.SINGELTON);
	}

	@Override
	public void startDTD(String name, String publicId, String systemId)	throws SAXException {
		saxEvents.add(new StartDTD(name, publicId, systemId));
	}

	@Override
	public void startEntity(String name) throws SAXException {
		saxEvents.add(new StartEntity(name));
	}

	// Public methods

	public boolean isEmpty() {
		return saxEvents.isEmpty();
	}

	public Vector<SAXEvent> getEvents() {
		return saxEvents;
	}

	public void toSAX(ContentHandler contentHandler) throws SAXException {
		for (SAXEvent event : saxEvents) {
			event.send(contentHandler);
		}
	}

	public String toString() {
		final StringBuffer value = new StringBuffer();

		for (final SAXEvent event : saxEvents) {
			if (event instanceof Characters) {
				((Characters)event).append(value);
			}
		}

		return value.toString();
	}

	public void clear() {
		saxEvents.clear();
	}

	public void renew() {
		saxEvents = new Vector<SAXEvent>();
	}

	public final void addEvent(SAXEvent event) {
		saxEvents.add(event);
	}

	public final Iterator<SAXEvent> iterator() {
		return saxEvents.iterator();
	}

	// SaxEvents

	public interface SAXEvent {
		public void send(ContentHandler contentHandler) throws SAXException;
	}

	public final static class Characters implements SAXEvent {

		private final char[] ch;

		public Characters(char[] ch, int start, int length) {
			// make a copy of the char array ch.
			// we do not want to have a reference to potentially large arrays

			this.ch = new char[length];
			System.arraycopy(ch, start, this.ch, 0, length);
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			contentHandler.characters(ch, 0, ch.length);
		}

		public String toString() {
			return new String(ch);
		}

		public void append(StringBuffer buffer) {
			buffer.append(ch);
		}
	}

	public final static class Comment implements SAXEvent {

		private final char[] ch;

		public Comment(char[] ch, int start, int length) {
			// make a copy of the char array ch.
			// we do not want to have a reference to potentially large arrays

			this.ch = new char[length];
			System.arraycopy(ch, start, this.ch, 0, length);
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			if (contentHandler instanceof LexicalHandler)
				((LexicalHandler)contentHandler).comment(ch, 0, ch.length);
		}
	}

	public final static class EndCDATA implements SAXEvent {

		public static final EndCDATA SINGLETON = new EndCDATA();

		private EndCDATA() {
			// just to thwart instantiation
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			if (contentHandler instanceof LexicalHandler)
				((LexicalHandler)contentHandler).endCDATA();
		}
	}

	public final static class EndDocument implements SAXEvent {
		public static final EndDocument SINGLETON = new EndDocument();

		private EndDocument() {
			// just to thwart instantiation
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			contentHandler.endDocument();
		}
	}

	public final static class EndDTD implements SAXEvent {

		public static final EndDTD SINGLETON = new EndDTD();

		private EndDTD() {
			// just to thwart instantiation
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			if (contentHandler instanceof LexicalHandler)
				((LexicalHandler)contentHandler).endDTD();
		}
	}

	public final static class EndElement implements SAXEvent {

		private final String uri;
		private final String localName;
		private final String qName;

		public EndElement(String uri, String localName, String qName) {
			this.uri = uri;
			this.localName = localName;
			this.qName = qName;
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			contentHandler.endElement(uri, localName, qName);
		}
	}

	public final static class EndEntity implements SAXEvent {

		private final String name;

		public EndEntity(String name) {
			this.name = name;
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			if (contentHandler instanceof LexicalHandler)
				((LexicalHandler)contentHandler).endEntity(name);
		}
	}

	public final static class EndPrefixMapping implements SAXEvent {

		private final String prefix;

		public EndPrefixMapping(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			contentHandler.endPrefixMapping(prefix);
		}
	}

	public final static class IgnorableWhitespace implements SAXEvent {

		private final char[] ch;

		public IgnorableWhitespace(char[] ch, int start, int length) {
			// make a copy of the char array ch.
			// we do not want to have a reference to potentially large arrays

			this.ch = new char[length];
			System.arraycopy(ch, start, this.ch, 0, length);
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			contentHandler.ignorableWhitespace(ch, 0, ch.length);
		}
	}

	public final static class ProcessingInstruction implements SAXEvent {

		private final String target;
		private final String data;

		public ProcessingInstruction(String target, String data) {
			this.target = target;
			this.data = data;
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			contentHandler.processingInstruction(target, data);
		}
	}

	public final static class SkippedEntity implements SAXEvent {

		private final String name;

		public SkippedEntity(String name) {
			this.name = name;
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			contentHandler.skippedEntity(name);
		}
	}

	public final static class StartCDATA implements SAXEvent {

		public static final StartCDATA SINGELTON = new StartCDATA();

		private StartCDATA() {
			// just to thwart instantiation
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			if (contentHandler instanceof LexicalHandler)
				((LexicalHandler)contentHandler).startCDATA();
		}
	}

	public final static class StartDocument implements SAXEvent {
		public static final StartDocument SINGLETON = new StartDocument();

		private StartDocument() {
			// just to thwart instantiation
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			contentHandler.startDocument();
		}
	}

	public final static class StartDTD implements SAXEvent {

		private final String name;
		private final String publicId;
		private final String systemId;

		public StartDTD(String name, String publicId, String systemId) {
			this.name = name;
			this.publicId = publicId;
			this.systemId = systemId;
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			if (contentHandler instanceof LexicalHandler)
				((LexicalHandler)contentHandler).startDTD(name, publicId, systemId);
		}
	}

	public final static class StartElement implements SAXEvent {

		private final String uri;
		private final String localName;
		private final String qName;
		private final Attributes atts;

		public StartElement(String uri, String localName, String qName, Attributes atts) {
			this.uri = uri;
			this.localName = localName;
			this.qName = qName;
			this.atts = new AttributesImpl(atts);
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			contentHandler.startElement(uri, localName, qName, atts);
		}
	}

	public final static class StartEntity implements SAXEvent {

		private final String name;

		public StartEntity(String name) {
			this.name = name;
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			if (contentHandler instanceof LexicalHandler)
				((LexicalHandler)contentHandler).startEntity(name);
		}
	}

	public final static class StartPrefixMapping implements SAXEvent {

		private final String prefix;
		private final String uri;

		public StartPrefixMapping(String prefix, String uri) {
			this.prefix = prefix;
			this.uri = uri;
		}

		@Override
		public void send(ContentHandler contentHandler) throws SAXException {
			contentHandler.startPrefixMapping(prefix, uri);
		}
	}
}
