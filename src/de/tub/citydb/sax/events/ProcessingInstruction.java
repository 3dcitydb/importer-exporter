package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class ProcessingInstruction implements SAXEvent {
	private final String target;
	private final String data;
	private final DocumentLocation documentLocation;

	public ProcessingInstruction(String target, String data, DocumentLocation documentLocation) {
		this.target = target;
		this.data = data;
		this.documentLocation = documentLocation;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.processingInstruction(target, data);
	}
	
	@Override
	public DocumentLocation getDocumentLocation() {
		return documentLocation;
	}

}
