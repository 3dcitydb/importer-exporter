package de.tub.citydb.sax.events;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public final class StartElement extends SAXEvent implements Locatable {
	private final String uri;
	private final String localName;
	private final Attributes attributes;
	private final Location location;

	public StartElement(String uri, String localName, Attributes attributes, Location location) {
		super(EventType.START_ELEMENT);
		this.uri = uri;
		this.localName = localName;
		this.attributes = new AttributesImpl(attributes);
		this.location = location;
	}
	
	public String getURI() {
		return uri;
	}

	public String getLocalName() {
		return localName;
	}
	
	public Attributes getAttributes() {
		return attributes;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.startElement(uri, localName, null, attributes);
	}
	
	@Override
	public Location getLocation() {
		return location;
	}

}
