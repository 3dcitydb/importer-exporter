package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class StartDocument implements SAXEvent {
	public static final StartDocument SINGLETON = new StartDocument();

	private StartDocument() {
		// just to thwart instantiation
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.startDocument();
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return null;
	}

}
