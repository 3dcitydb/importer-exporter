package de.tub.citydb.util;

import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import de.tub.citydb.sax.SAXBuffer;

public class JAXBUtil {

	public static SAXBuffer jaxbElem2saxEvents(JAXBContext jaxbContext,
													   JAXBElement<?> jaxbElement,
													   Properties marshallerProps) throws JAXBException {
		SAXBuffer saxBuffer = new SAXBuffer();
		Marshaller marshaller = jaxbContext.createMarshaller();

		for (Object key : marshallerProps.keySet())
			marshaller.setProperty(key.toString(), marshallerProps.get(key));

		marshaller.marshal(jaxbElement, saxBuffer);
		marshaller = null;
		
		return saxBuffer;
	}

}
