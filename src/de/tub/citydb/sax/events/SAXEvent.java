package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public interface SAXEvent {
	public void send(ContentHandler contentHandler) throws SAXException;
	public DocumentLocation getDocumentLocation();
}
