package de.tub.citydb.util;

import java.util.Properties;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import de.tub.citydb.sax.SAXBuffer;
import de.tub.citydb.sax.events.SAXEvent;

public class JAXBUtil {

	public static Vector<SAXEvent> jaxbElem2saxEvents(JAXBContext jaxbContext,
													   JAXBElement<?> jaxbElement,
													   Properties marshallerProps) throws JAXBException {
		SAXBuffer saxBuffer = new SAXBuffer();
		Marshaller marshaller = jaxbContext.createMarshaller();

		for (Object key : marshallerProps.keySet())
			marshaller.setProperty(key.toString(), marshallerProps.get(key));

		marshaller.marshal(jaxbElement, saxBuffer);
		return saxBuffer.getEvents();
	}

}
