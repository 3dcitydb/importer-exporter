package de.tub.citydb.sax;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.tub.citydb.log.Logger;

public class SAXErrorHandler implements ErrorHandler {
	private final Logger LOG = Logger.getInstance();

	public void warning(SAXParseException exc) throws SAXException {
		LOG.warn("Invalid XML content: " + exc.getMessage());
	}

	public void error(SAXParseException exc) throws SAXException {
		LOG.error("Invalid XML content: " + exc.getMessage());
	}

	public void fatalError(SAXParseException exc) throws SAXException {
		LOG.error("Invalid XML content: " + exc.getMessage());
	}
}
