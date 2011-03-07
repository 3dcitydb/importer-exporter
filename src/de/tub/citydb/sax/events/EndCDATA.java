package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public final class EndCDATA implements SAXEvent {
	public static final EndCDATA SINGLETON = new EndCDATA();

	private EndCDATA() {
		// just to thwart instantiation
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		if (contentHandler instanceof LexicalHandler)
			((LexicalHandler)contentHandler).endCDATA();
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return null;
	}

}
