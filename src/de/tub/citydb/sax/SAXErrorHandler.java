package de.tub.citydb.sax;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SAXErrorHandler implements ErrorHandler {

	public void warning(SAXParseException exc) throws SAXException {
		//LOG.warn("Warning: " + exc.getSystemId() + ", Message " + exc.getMessage());
	}

	public void error(SAXParseException exc) throws SAXException {
		//LOG.error("Error: " + exc.getSystemId() + ", Message " + exc.getMessage());
	}

	public void fatalError(SAXParseException exc) throws SAXException {
		//LOG.fatal("FatalError: " + exc.getSystemId() + ", Message " + exc.getMessage());
	}
}
