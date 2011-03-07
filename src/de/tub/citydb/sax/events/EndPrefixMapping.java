package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class EndPrefixMapping extends SAXEvent {
	private final String prefix;

	public EndPrefixMapping(String prefix) {
		super(EventType.END_PREFIX_MAPPING);
		this.prefix = prefix;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.endPrefixMapping(prefix);
	}

}
