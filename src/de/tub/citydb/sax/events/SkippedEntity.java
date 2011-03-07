package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class SkippedEntity implements SAXEvent {
	private final String name;
	private final DocumentLocation documentLocation;

	public SkippedEntity(String name, DocumentLocation documentLocation) {
		this.name = name;
		this.documentLocation = documentLocation;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.skippedEntity(name);
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return documentLocation;
	}

}
