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
package org.citydb.core.database.schema.util;

import org.citydb.core.database.schema.mapping.*;
import org.citydb.util.xml.SecureXMLProcessors;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class SchemaMappingUtil {
	private static SchemaMappingUtil instance;
	private final JAXBContext context;
	
	private SchemaMappingUtil() throws JAXBException {
		context = JAXBContext.newInstance(SchemaMapping.class);
	}
	
	public static synchronized SchemaMappingUtil getInstance() throws JAXBException {
		if (instance == null)
			instance = new SchemaMappingUtil();
		
		return instance;
	}	

	private SchemaMapping unmarshal(SchemaMapping schemaMapping, InputSource input) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		Unmarshaller unmarshaller = context.createUnmarshaller();
		unmarshaller.setAdapter(new AppSchemaAdapter(schemaMapping));
		unmarshaller.setAdapter(new ComplexAttributeTypeAdapter(schemaMapping));
		unmarshaller.setAdapter(new ComplexTypeAdapter(schemaMapping));
		unmarshaller.setAdapter(new FeatureTypeAdapter(schemaMapping));
		unmarshaller.setAdapter(new ObjectTypeAdapter(schemaMapping));
		unmarshaller.setListener(new UnmarshalListener());

		// validate schema mapping
		ValidationEvent[] events = new ValidationEvent[1];
		unmarshaller.setSchema(readSchema());
		unmarshaller.setEventHandler(event -> {
			events[0] = event;
			return false;
		});

		UnmarshallerHandler handler = unmarshaller.getUnmarshallerHandler();

		try {
			SAXParserFactory saxParserFactory = SecureXMLProcessors.newSAXParserFactory();
			saxParserFactory.setNamespaceAware(true);
			XMLReader reader = saxParserFactory.newSAXParser().getXMLReader();
			reader.setContentHandler(handler);
			reader.parse(input);
		} catch (SAXException | ParserConfigurationException | IOException e) {
			if (events[0] != null) {
				throw new SchemaMappingValidationException(events[0].getMessage());
			} else {
				throw new SchemaMappingException("Failed to parse the schema mapping.", e);
			}
		}

		// unmarshal schema mapping
		Object result = handler.getResult();
		if (!(result instanceof SchemaMapping))
			throw new SchemaMappingException("Failed to unmarshal input resource into a schema mapping.");

		return (SchemaMapping) result;
	}

	public SchemaMapping unmarshal(SchemaMapping schemaMapping, InputStream inputStream) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		return unmarshal(schemaMapping, new InputSource(inputStream));
	}

	public SchemaMapping unmarshal(InputStream inputStream) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		return unmarshal(null, new InputSource(inputStream));
	}

	public SchemaMapping unmarshal(SchemaMapping schemaMapping, URL resource) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		try (InputStream inputStream = resource.openStream()) {
			return unmarshal(schemaMapping, inputStream);
		} catch (IOException e) {
			throw new JAXBException("Failed to open schema mapping resource at URL '" + resource.toString() + "'.");
		}
	}

	public SchemaMapping unmarshal(URL resource) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		return unmarshal(null, resource);
	}

	public SchemaMapping unmarshal(SchemaMapping schemaMapping, File file) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		try (InputStream inputStream = Files.newInputStream(file.toPath())) {
			return unmarshal(schemaMapping, inputStream);
		} catch (IOException e) {
			throw new JAXBException("Failed to open schema mapping resource from file '" + file.getAbsolutePath() + "'.");
		}
	}

	public SchemaMapping unmarshal(File file) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		return unmarshal(null, file);
	}

	public SchemaMapping unmarshal(SchemaMapping schemaMapping, String xml) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		return unmarshal(schemaMapping, new InputSource(new StringReader(xml)));
	}

	public SchemaMapping unmarshal(String xml) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		return unmarshal(null, xml);
	}

	public void marshal(SchemaMapping schemaMapping, Writer writer, boolean prettyPrint) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		Marshaller m = context.createMarshaller();
		m.setListener(new MarshalListener());
		
		if (prettyPrint)
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
				
		// validate schema mapping
		ValidationEvent[] events = new ValidationEvent[1];
		m.setSchema(readSchema());
		m.setEventHandler(event -> {
			events[0] = event;
			return false;
		});

		try {
			m.marshal(schemaMapping, writer);
		} catch (JAXBException e) {
			if (events[0] != null)
				throw new SchemaMappingValidationException(events[0].getMessage());

			throw e;
		} catch (Throwable e) {
			if (e.getCause() instanceof SchemaMappingException)
				throw (SchemaMappingException)e.getCause();

			throw e;
		}
	}

	public void marshal(SchemaMapping schemaMapping, Writer writer) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		marshal(schemaMapping, writer, true);
	}
	
	public void marshal(SchemaMapping schemaMapping, File file) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8))) {
			marshal(schemaMapping, writer);
		} catch (UnsupportedEncodingException e) {
			throw new JAXBException("Failed to marshal schema mapping.", e);
		} catch (IOException e) {
			throw new JAXBException("Failed to open schema mapping resource from file '" + file.getAbsolutePath() + "'.");
		}
	}

	private Schema readSchema() throws JAXBException {
		try {
			SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
			return schemaFactory.newSchema(SchemaMappingUtil.class.getResource("/org/citydb/core/database/schema/3dcitydb-schema.xsd"));
		} catch (SAXException e) {
			throw new JAXBException("Failed to parse the schema mapping XSD schema. " +
					"Could not find '/org/citydb/core/database/schema/3dcitydb-schema.xsd' on the classpath.", e);
		}
	}
}
