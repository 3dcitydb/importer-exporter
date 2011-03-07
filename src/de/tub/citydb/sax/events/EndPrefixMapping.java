package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class EndPrefixMapping implements SAXEvent {
	private final String prefix;
	private final DocumentLocation documentLocation;

	public EndPrefixMapping(String prefix, DocumentLocation documentLocation) {
		this.prefix = prefix;
		this.documentLocation = documentLocation;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.endPrefixMapping(prefix);
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return documentLocation;
	}

}
