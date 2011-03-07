package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class EndDocument implements SAXEvent {
	public static final EndDocument SINGLETON = new EndDocument();

	private EndDocument() {
		// just to thwart instantiation
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.endDocument();
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return null;
	}

}
