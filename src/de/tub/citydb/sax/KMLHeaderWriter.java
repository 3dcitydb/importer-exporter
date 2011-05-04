package de.tub.citydb.sax;

import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.xml.sax.SAXException;

import de.tub.citydb.sax.events.EndElement;
import de.tub.citydb.sax.events.SAXEvent;
import de.tub.citydb.sax.events.StartElement;
import de.tub.citydb.util.JAXBUtil;

public class KMLHeaderWriter {

	private final SAXWriter saxWriter;
	private SAXBuffer saxBuffer;

	public KMLHeaderWriter(SAXWriter saxWriter) {
		this.saxWriter = saxWriter;
	}

	public void startRootElement() throws SAXException {
		saxWriter.startDocument();

		if (saxBuffer != null && !saxBuffer.isEmpty()) {
			SAXEvent event = saxBuffer.getFirstEvent();
			long depth = 0;

			do {
				if (event instanceof StartElement)
					++depth;

				if (event instanceof EndElement) {
					--depth;
					if (depth <= 0)
						break;

					EndElement endElement = (EndElement) event;
					if ("Document".equals(endElement.getLocalName()))
					break;
				}

				event.send(saxWriter);
				saxBuffer.removeFirstEvent();
			} while ((event = event.next()) != null);			
		}
	}


	public void endRootElement() throws SAXException {
		if (saxBuffer != null && !saxBuffer.isEmpty()) {
			SAXEvent event = saxBuffer.getFirstEvent();

			do {
				event.send(saxWriter);
				saxBuffer.removeFirstEvent();
			} while ((event = event.next()) != null);	
		}

		saxWriter.endDocument();
	}

	public void setRootElement(JAXBElement<?> jaxbRootElement,
			JAXBContext jaxbContext,
			Properties marshallerProps) throws JAXBException {
		saxBuffer = JAXBUtil.jaxbElem2saxEvents(jaxbContext, jaxbRootElement, marshallerProps);
	}

}
