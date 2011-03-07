package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class EndDocument extends SAXEvent {

	public EndDocument() {
		super(EventType.END_DOCUMENT);
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.endDocument();
	}
	
}
