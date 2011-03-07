package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class StartPrefixMapping implements SAXEvent {
	private final String prefix;
	private final String uri;
	private final DocumentLocation documentLocation;

	public StartPrefixMapping(String prefix, String uri, DocumentLocation documentLocation) {
		this.prefix = prefix;
		this.uri = uri;
		this.documentLocation = documentLocation;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.startPrefixMapping(prefix, uri);
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return documentLocation;
	}

}
