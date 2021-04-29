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
package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "namespace")
public class Namespace {
    @XmlAttribute(required = true)
    protected CityGMLContext context;
    @XmlAttribute
    protected String schemaLocation;
    @XmlValue
    protected String uri;
    
    protected Namespace() {
    }
    
    public Namespace(String uri, CityGMLContext context) {
    	this.uri = uri;
    	this.context = context;
    }

	public CityGMLContext getContext() {
		return context;
	}
	
	public boolean isSetContext() {
		return context != null;
	}
	
	public void setContext(CityGMLContext context) {
		this.context = context;
	}

	public String getSchemaLocation() {
		return schemaLocation;
	}
	
	public boolean isSetSchemaLocation() {
		return schemaLocation != null && !schemaLocation.isEmpty();
	}

	public void setSchemaLocation(String schemaLocation) {
		this.schemaLocation = schemaLocation;
	}

	public String getURI() {
		return isSetURI() ? uri : "";
	}

	public boolean isSetURI() {
		return uri != null && !uri.isEmpty();
	}
	
	public void setURI(String uri) {
		this.uri = uri;
	}
	
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		if (!(parent instanceof AppSchema))
			throw new SchemaMappingException("Unexpected parent element for namespace: " + parent);
		
		AppSchema schema = (AppSchema)parent;
		if (!isSetURI())
			throw new SchemaMappingException("The application schema '" + schema.id + "' lacks a namespace URI declaration.");
		else if (!isSetContext())
			throw new SchemaMappingException("The application schema '" + schema.id + "' lacks a CityGML context definition.");
	}

}
