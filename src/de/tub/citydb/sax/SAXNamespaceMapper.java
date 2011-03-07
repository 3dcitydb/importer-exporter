package de.tub.citydb.sax;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class SAXNamespaceMapper extends XMLFilterImpl {
	private HashMap<String, String> uriMap = new HashMap<String, String>();

	public SAXNamespaceMapper(XMLReader reader) {
		super(reader);
	}

	public void setNamespaceMapping(String oldURI, String newURI) {
		uriMap.put(oldURI, newURI);
	}

	public String getNamespaceMapping(String oldURI) {
		return uriMap.get(oldURI);
	}

	public void startPrefixMapping(String prefix, String uri) throws SAXException {
		// change URI if needed
		String newURI = uriMap.get(uri);

		if (newURI != null) {
			super.startPrefixMapping(prefix, newURI);
		} else {
			super.startPrefixMapping(prefix, uri);
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		String newURI = uriMap.get(uri);

		if (newURI != null) {
			super.startElement(newURI, localName, qName, atts);
		} else {
			super.startElement(uri, localName, qName, atts);
		}
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		String newURI = uriMap.get(uri);

		if (newURI != null) {
			super.endElement(newURI, localName, qName);
		} else {
			super.endElement(uri, localName, qName);
		}
	}
}
