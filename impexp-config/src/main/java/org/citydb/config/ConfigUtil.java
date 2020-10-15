/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import org.citydb.config.project.Project;
import org.citydb.config.project.ProjectSchemaWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ConfigUtil {
	public static final String CITYDB_CONFIG_NAMESPACE_URI = "http://www.3dcitydb.org/importer-exporter/config";

	public static void marshal(Object object, File file, JAXBContext ctx) throws JAXBException {
		Marshaller m = ctx.createMarshaller();	
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		m.marshal(object, file);
	}

	public static Object unmarshal(File file, JAXBContext ctx) throws JAXBException, IOException {
		try (FileInputStream inputStream = new FileInputStream(file)) {
			return unmarshal(inputStream, ctx);
		}
	}
	
	public static Object unmarshal(InputStream inputStream, JAXBContext ctx) throws JAXBException, IOException {
		Unmarshaller um = ctx.createUnmarshaller();
		UnmarshallerHandler handler = um.getUnmarshallerHandler();

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);

		ConfigNamespaceFilter namespaceFilter;
		try {
			XMLReader reader = factory.newSAXParser().getXMLReader();
			namespaceFilter = new ConfigNamespaceFilter(reader);	
			namespaceFilter.setContentHandler(handler);
			namespaceFilter.parse(new InputSource(inputStream));			
		} catch (SAXException e) {
			throw new JAXBException(e.getMessage());
		} catch (ParserConfigurationException e) {
			throw new IOException(e.getMessage());
		}

		Object result = handler.getResult();
		if (result instanceof Project)
			((Project) result).setNamespaceFilter(namespaceFilter);

		return result;
	}

	public static void generateSchema(JAXBContext ctx, File file) throws IOException {
		ctx.generateSchema(new ProjectSchemaWriter(file));
	}
}
