/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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

public class XMLHeaderWriter {
	private final SAXWriter saxWriter;
	private SAXBuffer saxBuffer;

	public XMLHeaderWriter(SAXWriter saxWriter) {
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
