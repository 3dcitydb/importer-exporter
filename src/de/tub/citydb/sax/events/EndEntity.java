package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public final class EndEntity implements SAXEvent {
	private final String name;
	private final DocumentLocation documentLocation;
	
	public EndEntity(String name, DocumentLocation documentLocation) {
		this.name = name;
		this.documentLocation = documentLocation;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		if (contentHandler instanceof LexicalHandler)
			((LexicalHandler)contentHandler).endEntity(name);
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return documentLocation;
	}

}
