package de.tub.citydb.sax.events;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public final class StartElement implements SAXEvent {
	private final String uri;
	private final String localName;
	private final String qName;
	private final Attributes atts;
	private final DocumentLocation documentLocation;

	public StartElement(String uri, String localName, String qName, Attributes atts, DocumentLocation documentLocation) {
		this.uri = uri;
		this.localName = localName;
		this.qName = qName;
		this.atts = new AttributesImpl(atts);
		this.documentLocation = documentLocation;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.startElement(uri, localName, qName, atts);
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return documentLocation;
	}

}
