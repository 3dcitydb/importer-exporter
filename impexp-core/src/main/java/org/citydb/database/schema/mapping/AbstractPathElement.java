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

import java.util.HashMap;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

import org.citygml4j.model.module.citygml.CityGMLVersion;

@XmlType(name = "abstractPathElement")
public abstract class AbstractPathElement {
    @XmlAttribute(required = true)
    protected String path;
    @XmlAttribute(required = true)
    @XmlJavaTypeAdapter(AppSchemaAdapter.class)
    protected AppSchema schema;
    @XmlAttribute
    protected Boolean queryable;
    
    protected AbstractPathElement() {
	}
    
    public AbstractPathElement(String path, AppSchema schema) {
    	this.path = path;
    	this.schema = schema;
    }
    
    @XmlTransient
    private HashMap<String, Object> localProperties;

	public abstract PathElementType getElementType();

    public String getPath() {
        return path;
    }

    public boolean isSetPath() {
        return path != null && !path.isEmpty();
    }
    
    public void setPath(String path) {
    	this.path = path;
    }

    public AppSchema getSchema() {
        return schema;
    }

    public boolean isSetSchema() {
        return schema != null;
    }
    
    public void setSchema(AppSchema schema) {
    	this.schema = schema;
    }

    public boolean isQueryable() {
        return queryable == null || queryable;
    }

    public boolean isSetQueryable() {
        return queryable != null;
    }

    public void setQueryable(boolean queryable) {
        this.queryable = queryable ? null : false;
    }
    
    public Object getLocalProperty(String name) {
		if (localProperties != null)
			return localProperties.get(name);
			
		return null;
	}

	public void setLocalProperty(String name, Object value) {
		if (localProperties == null)
			localProperties = new HashMap<String, Object>();
		
		localProperties.put(name, value);
	}

	public boolean hasLocalProperty(String name) {
		return localProperties != null && localProperties.containsKey(name);
	}

	public Object unsetLocalProperty(String name) {
		if (localProperties != null)
			return localProperties.remove(name);
		
		return null;
	}
	
	public boolean matchesName(String name, String namespaceURI) {
		return schema.matchesNamespaceURI(namespaceURI) && path.equals(name);
	}
	
	public boolean matchesName(QName name) {
		return matchesName(name.getLocalPart(), name.getNamespaceURI());
	}
	
	public boolean isAvailableForCityGML(CityGMLVersion version) {
		return schema.isAvailableForCityGML(version);
	}
	
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		if (!isSetPath())
			throw new SchemaMappingException("A path element of type " + getElementType() + " lacks a path name.");
		
		if (!isSetSchema())
			throw new SchemaMappingException("The path element of name '" + path + "' and type " + getElementType() + " lacks an application schema.");
		
		if (schema.hasLocalProperty(MappingConstants.IS_XLINK)) {
			AppSchema ref = schemaMapping.getSchemaById(schema.id);
			if (ref == null)
				throw new SchemaMappingException("Failed to resolve application schema reference '" + schema.id + "'.");
			
			schema = ref;
		}
	}

	@Override
	public String toString() {
		if (!schema.isSetXMLPrefix())
			schema.generateXMLPrefix();
		
		return schema.getXMLPrefix() + ":" + path;
	}
	
}
