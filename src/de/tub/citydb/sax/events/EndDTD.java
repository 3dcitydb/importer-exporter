package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public final class EndDTD implements SAXEvent {
	public static final EndDTD SINGLETON = new EndDTD();

	private EndDTD() {
		// just to thwart instantiation
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		if (contentHandler instanceof LexicalHandler)
			((LexicalHandler)contentHandler).endDTD();
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return null;
	}

}
