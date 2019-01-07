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
package org.citydb.database.schema.util;

import org.citydb.database.schema.mapping.AppSchemaAdapter;
import org.citydb.database.schema.mapping.ComplexAttributeTypeAdapter;
import org.citydb.database.schema.mapping.ComplexTypeAdapter;
import org.citydb.database.schema.mapping.FeatureTypeAdapter;
import org.citydb.database.schema.mapping.MarshalListener;
import org.citydb.database.schema.mapping.ObjectTypeAdapter;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.mapping.SchemaMappingException;
import org.citydb.database.schema.mapping.SchemaMappingValidationException;
import org.citydb.database.schema.mapping.UnmarshalListener;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;

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

	private SchemaMapping unmarshal(SchemaMapping schemaMapping, Object input) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		Unmarshaller um = context.createUnmarshaller();
		um.setAdapter(new AppSchemaAdapter(schemaMapping));
		um.setAdapter(new ComplexAttributeTypeAdapter(schemaMapping));
		um.setAdapter(new ComplexTypeAdapter(schemaMapping));
		um.setAdapter(new FeatureTypeAdapter(schemaMapping));
		um.setAdapter(new ObjectTypeAdapter(schemaMapping));
		um.setListener(new UnmarshalListener());

		// validate schema mapping
		ValidationEvent[] events = new ValidationEvent[1];
		um.setSchema(readSchema());
		um.setEventHandler(new ValidationEventHandler() {
			public boolean handleEvent(ValidationEvent event) {
				events[0] = event;
				return false;
			}
		});

		// unmarshal schema mapping
		Object result = null;
		try {
			if (input instanceof InputStream)
				result = um.unmarshal((InputStream) input);
			else if (input instanceof Reader)
				result = um.unmarshal((Reader) input);

			if (!(result instanceof SchemaMapping))
				throw new SchemaMappingException("Failed to unmarshal input resource into a schema mapping.");

			return (SchemaMapping)result;
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

	public SchemaMapping unmarshal(SchemaMapping schemaMapping, InputStream inputStream) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		return unmarshal(schemaMapping, (Object) inputStream);
	}

	public SchemaMapping unmarshal(InputStream inputStream) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		return unmarshal(null, (Object) inputStream);
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
		try (InputStream inputStream = new FileInputStream(file)) {
			return unmarshal(schemaMapping, inputStream);
		} catch (IOException e) {
			throw new JAXBException("Failed to open schema mapping resource from file '" + file.getAbsolutePath() + "'.");
		}
	}

	public SchemaMapping unmarshal(File file) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		return unmarshal(null, file);
	}

	public SchemaMapping unmarshal(SchemaMapping schemaMapping, String xml) throws SchemaMappingException, SchemaMappingValidationException, JAXBException {
		try (Reader reader = new StringReader(xml)) {
			return unmarshal(schemaMapping, reader);
		} catch (IOException e) {
			throw new JAXBException("Failed to open schema mapping resource from XML string.");
		}
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
		m.setEventHandler(new ValidationEventHandler() {
			public boolean handleEvent(ValidationEvent event) {
				events[0] = event;
				return false;
			}
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
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
			marshal(schemaMapping, writer);
		} catch (UnsupportedEncodingException e) {
			throw new JAXBException("Failed to marshal schema mapping.", e);
		} catch (IOException e) {
			throw new JAXBException("Failed to open schema mapping resource from file '" + file.getAbsolutePath() + "'.");
		}
	}

	private Schema readSchema() throws JAXBException {
		try {
			return SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(SchemaMappingUtil.class.getResource("/org/citydb/database/schema/3dcitydb-schema.xsd"));
		} catch (SAXException e) {
			throw new JAXBException("Failed to parse the schema mapping XSD schema. Could not find '/org/citydb/database/schema/3dcitydb-schema.xsd' on the classpath.", e);
		}
	}
}
