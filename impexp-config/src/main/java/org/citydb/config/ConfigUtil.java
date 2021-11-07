/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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

import org.citydb.config.gui.GuiConfig;
import org.citydb.config.util.ConfigNamespaceFilter;
import org.citydb.config.util.ProjectSchemaWriter;
import org.citydb.config.util.QueryWrapper;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.bind.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ConfigUtil {
	public static final String CITYDB_CONFIG_NAMESPACE_URI = "http://www.3dcitydb.org/importer-exporter/config";
	private static ConfigUtil instance;

	private final Set<Class<?>> configClasses = new HashSet<>();
	private JAXBContext context;

	public static synchronized ConfigUtil getInstance() {
		if (instance == null) {
			instance = new ConfigUtil().withConfigClasses(ProjectConfig.class, GuiConfig.class, QueryWrapper.class);
		}

		return instance;
	}

	public ConfigUtil withConfigClass(Class<?> configClass) {
		configClasses.add(configClass);
		context = null;
		return this;
	}

	public ConfigUtil withConfigClasses(Class<?>... configClasses) {
		Arrays.stream(configClasses).forEach(this::withConfigClass);
		return this;
	}

	private ConfigUtil createJAXBContext() throws JAXBException {
		if (context == null) {
			context = JAXBContext.newInstance(configClasses.toArray(new Class[]{}));
		}

		return this;
	}

	public void marshal(Object object, File file) throws JAXBException {
		Marshaller marshaller = createJAXBContext().context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
		marshaller.marshal(object, file);
	}

	public Object unmarshal(File file) throws JAXBException, IOException {
		try (FileInputStream inputStream = new FileInputStream(file)) {
			return unmarshal(inputStream);
		}
	}
	
	public Object unmarshal(InputStream inputStream) throws JAXBException, IOException {
		Unmarshaller unmarshaller = createJAXBContext().context.createUnmarshaller();
		UnmarshallerHandler handler = unmarshaller.getUnmarshallerHandler();
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
		if (result instanceof ProjectConfig)
			((ProjectConfig) result).setNamespaceFilter(namespaceFilter);

		return result;
	}

	public void generateSchema(File file) throws JAXBException, IOException {
		createJAXBContext().context.generateSchema(new ProjectSchemaWriter(file));
	}

	public JAXBContext getJAXBContext() throws JAXBException {
		return createJAXBContext().context;
	}
}
