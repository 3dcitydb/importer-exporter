package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class StartDocument extends SAXEvent {

	public StartDocument() {
		super(EventType.START_DOCUMENT);
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.startDocument();
	}

}
