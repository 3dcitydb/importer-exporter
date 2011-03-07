package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public final class StartCDATA implements SAXEvent {
	public static final StartCDATA SINGELTON = new StartCDATA();

	private StartCDATA() {
		// just to thwart instantiation
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		if (contentHandler instanceof LexicalHandler)
			((LexicalHandler)contentHandler).startCDATA();
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return null;
	}

}
