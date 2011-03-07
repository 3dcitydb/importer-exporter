package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class EndElement implements SAXEvent {
	private final String uri;
	private final String localName;
	private final String qName;
	private final DocumentLocation documentLocation;

	public EndElement(String uri, String localName, String qName, DocumentLocation documentLocation) {
		this.uri = uri;
		this.localName = localName;
		this.qName = qName;
		this.documentLocation = documentLocation;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.endElement(uri, localName, qName);
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return documentLocation;
	}

}
