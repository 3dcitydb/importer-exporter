package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

public final class StartDTD implements SAXEvent {
	private final String name;
	private final String publicId;
	private final String systemId;
	private final DocumentLocation documentLocation;

	public StartDTD(String name, String publicId, String systemId, DocumentLocation documentLocation) {
		this.name = name;
		this.publicId = publicId;
		this.systemId = systemId;
		this.documentLocation = documentLocation;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		if (contentHandler instanceof LexicalHandler)
			((LexicalHandler)contentHandler).startDTD(name, publicId, systemId);
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return documentLocation;
	}

}
