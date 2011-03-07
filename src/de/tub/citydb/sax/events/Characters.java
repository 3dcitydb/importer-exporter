package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class Characters extends SAXEvent implements Locatable {
	private final char[] ch;
	private final Location location;
	
	public Characters(char[] ch, int start, int length, Location location) {
		super(EventType.CHARACTERS);
		
		// make a copy of the char array.
		// we do not want to have a reference to potentially large arrays
		this.ch = new char[length];
		System.arraycopy(ch, start, this.ch, 0, length);
		this.location = location;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.characters(ch, 0, ch.length);
	}

	@Override
	public Location getLocation() {
		return location;
	}

}
