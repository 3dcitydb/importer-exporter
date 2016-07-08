/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.citydb.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.citydb.config.project.ProjectSchemaWriter;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

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

		m.marshal(object, file);
	}

	public static Object unmarshal(File file, JAXBContext ctx) throws JAXBException, IOException {
		return unmarshal(new FileInputStream(file), ctx);
	}
	
	public static Object unmarshal(InputStream inputStream, JAXBContext ctx) throws JAXBException, IOException {
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
			mapper.parse(new InputSource(inputStream));			
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
				uri = "http://www.3dcitydb.org/importer-exporter/config";
			
			// support config files from previous releases 
			else if (uri.startsWith("http://www.gis.tu-berlin.de/3dcitydb-impexp/config"))
				uri = uri.replaceFirst("http://www.gis.tu-berlin.de/3dcitydb-impexp/config", "http://www.3dcitydb.org/importer-exporter/config");

			super.startPrefixMapping(prefix, uri);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
			if (uri == null || uri.length() == 0)
				uri = "http://www.3dcitydb.org/importer-exporter/config";
			
			// support config files from previous releases
			else if (uri.startsWith("http://www.gis.tu-berlin.de/3dcitydb-impexp/config"))
				uri = uri.replaceFirst("http://www.gis.tu-berlin.de/3dcitydb-impexp/config", "http://www.3dcitydb.org/importer-exporter/config");

			super.startElement(uri, localName, qName, atts);
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (uri == null || uri.length() == 0)
				uri = "http://www.3dcitydb.org/importer-exporter/config";
			
			// support config files from previous releases
			else if (uri.startsWith("http://www.gis.tu-berlin.de/3dcitydb-impexp/config"))
				uri = uri.replaceFirst("http://www.gis.tu-berlin.de/3dcitydb-impexp/config", "http://www.3dcitydb.org/importer-exporter/config");

			super.endElement(uri, localName, qName);
		}	
	}

}
