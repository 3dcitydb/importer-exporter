package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class EndElement extends SAXEvent implements Locatable {
	private final String uri;
	private final String localName;
	private final Location location;

	public EndElement(String uri, String localName, Location location) {
		super(EventType.END_ELEMENT);
		this.uri = uri;
		this.localName = localName;
		this.location = location;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.endElement(uri, localName, null);
	}
	
	@Override
	public Location getLocation() {
		return location;
	}

}
