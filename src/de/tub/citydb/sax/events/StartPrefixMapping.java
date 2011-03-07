package de.tub.citydb.sax.events;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public final class StartPrefixMapping extends SAXEvent {
	private final String prefix;
	private final String uri;

	public StartPrefixMapping(String prefix, String uri) {
		super(EventType.START_PREFIX_MAPPING);
		this.prefix = prefix;
		this.uri = uri;
	}

	@Override
	public void send(ContentHandler contentHandler) throws SAXException {
		contentHandler.startPrefixMapping(prefix, uri);
	}
	
}
