/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

import de.tub.citydb.config.project.ProjectSchemaWriter;

public class ConfigUtil {

	public static String createConfigPath(String configPath) {
		File createPath = new File(configPath);
		boolean success = true;

		if (!createPath.exists())
			success = createPath.mkdirs();

		return success ? createPath.getAbsolutePath() : null;
	}

	public static void marshal(Object object, File file, JAXBContext ctx) throws JAXBException {		
		Marshaller m = ctx.createMarshaller();	
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,Boolean.TRUE);
		m.setProperty("com.sun.xml.bind.indentString", "  ");

		m.marshal(object, file);
	}

	public static Object unmarshal(File file, JAXBContext ctx) throws JAXBException, IOException {
		Unmarshaller um = ctx.createUnmarshaller();
		UnmarshallerHandler handler = um.getUnmarshallerHandler();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);

		XMLReader reader = null;

		try {
			reader = factory.newSAXParser().getXMLReader();

			// the namespace mapper ensures that we can also
			// read project files not declaring proper namespaces
			Mapper mapper = new Mapper(reader);
			mapper.setContentHandler(handler);
			mapper.parse(new InputSource(new FileInputStream(file)));			
		} catch (SAXException e) {
			throw new JAXBException(e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new IOException(e.getMessage());
		}

		return handler.getResult();
	}

	public static void generateSchema(JAXBContext ctx, File file) throws IOException {
		ctx.generateSchema(new ProjectSchemaWriter(file));
	}

	private static class Mapper extends XMLFilterImpl {

		Mapper(XMLReader reader) {
			super(reader);
		}

		@Override
		public void startPrefixMapping(String prefix, String uri) throws SAXException {
			if (uri == null || uri.length() == 0)
				uri = "http://www.gis.tu-berlin.de/3dcitydb-impexp/config";

			super.startPrefixMapping(prefix, uri);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			if (uri == null || uri.length() == 0)
				uri = "http://www.gis.tu-berlin.de/3dcitydb-impexp/config";

			super.startElement(uri, localName, qName, atts);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (uri == null || uri.length() == 0)
				uri = "http://www.gis.tu-berlin.de/3dcitydb-impexp/config";

			super.endElement(uri, localName, qName);
		}	
	}

}
