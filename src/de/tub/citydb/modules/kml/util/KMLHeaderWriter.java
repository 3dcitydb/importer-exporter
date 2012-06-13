/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.modules.kml.util;

import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.citygml4j.util.xml.SAXEventBuffer;
import org.citygml4j.util.xml.SAXWriter;
import org.citygml4j.util.xml.saxevents.EndElement;
import org.citygml4j.util.xml.saxevents.SAXEvent;
import org.citygml4j.util.xml.saxevents.StartElement;
import org.xml.sax.SAXException;

public class KMLHeaderWriter {
	private final SAXWriter saxWriter;
	private SAXEventBuffer saxBuffer;

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
		saxBuffer = new SAXEventBuffer();

		Marshaller marshaller = jaxbContext.createMarshaller();
		for (Object key : marshallerProps.keySet())
			marshaller.setProperty(key.toString(), marshallerProps.get(key));

		marshaller.marshal(jaxbRootElement, saxBuffer);	}

}
