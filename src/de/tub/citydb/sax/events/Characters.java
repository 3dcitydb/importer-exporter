package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class Characters implements SAXEvent {
	private final char[] ch;
	private final DocumentLocation documentLocation;
	
	public Characters(char[] ch, int start, int length, DocumentLocation documentLocation) {
		// make a copy of the char array ch.
		// we do not want to have a reference to potentially large arrays

		this.ch = new char[length];
		System.arraycopy(ch, start, this.ch, 0, length);
		this.documentLocation = documentLocation;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.characters(ch, 0, ch.length);
	}

	public String toString() {
		return new String(ch);
	}

	public void append(StringBuffer buffer) {
		buffer.append(ch);
	}

	@Override
	public DocumentLocation getDocumentLocation() {
		return documentLocation;
	}

}
