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
package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "attribute", propOrder = {
	    "join"
	})
public class SimpleAttribute extends AbstractAttribute {
	@XmlElements({
        @XmlElement(type = Join.class),
        @XmlElement(name = "reverseJoin", type = ReverseJoin.class)
    })
    protected AbstractJoin join;
	@XmlAttribute(required = true)
	protected String column;
	@XmlAttribute(required = true)
	protected SimpleType type;
	@XmlAttribute
	protected Boolean requiresPrefix;
	
	@XmlTransient
	protected AbstractType<?> complexType;
	@XmlTransient
	protected ComplexAttributeType attributeType;
	@XmlTransient
	protected String name;

	protected SimpleAttribute() {
	}
    
    public SimpleAttribute(String path, String column, SimpleType type, AppSchema schema) {
    	super(path, schema);
    	this.column = column;
    	this.type = type;
    }
	
	public boolean hasParentType() {
		return complexType != null;
	}
	
	public AbstractType<?> getParentType() {
		return complexType;
	}

	public boolean hasParentAttributeType() {
		return attributeType != null;
	}
	
	public ComplexAttributeType getParentAttributeType() {
		return attributeType;
	}
	
	protected void setParentType(AbstractType<?> complexType) {
		this.complexType = complexType;
		this.attributeType = null;
	}
	
	protected void setParentAttributeType(ComplexAttributeType attributeType) {
		this.attributeType = attributeType;
		this.complexType = null;
	}
	
	@Override
    public AbstractJoin getJoin() {
        return join;
    }

    @Override
    public boolean isSetJoin() {
        return join != null;
    }
    
    public void setJoin(Join join) {
    	this.join = join;
    }
	
    public void setJoin(ReverseJoin join) {
    	this.join = join;
    }
    
	public String getColumn() {
		return column;
	}

	public boolean isSetColumn() {
		return column != null && !column.isEmpty();
	}
	
	public void setColumn(String column) {
		this.column = column;
	}

	public SimpleType getType() {
		return type;
	}

	public boolean isSetType() {
		return type != null;
	}
	
	public void setType(SimpleType type) {
		this.type = type;
	}

	public boolean requiresPrefix() {
		return requiresPrefix != null && requiresPrefix;
	}

	public boolean isSetRequiresPrefix() {
		return requiresPrefix!= null;
	}

	public void setRequiresPrefix(boolean requiresPrefix) {
		this.requiresPrefix = requiresPrefix ? true : null;
	}

	public String getName() {
		if (name == null)
			name = path.startsWith("@") ? path.substring(1, path.length()) : path;

		return name;
	}

	@Override
	public PathElementType getElementType() {
		return PathElementType.SIMPLE_ATTRIBUTE;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		super.validate(schemaMapping, parent);

		if (requiresPrefix() && !path.startsWith("@"))
			throw new SchemaMappingException("The attribute 'requiresPrefix' shall only be used for XML attributes.");
		
		if (parent instanceof AbstractType<?>)
			complexType = (AbstractType<?>)parent;
		else if (parent instanceof ComplexAttributeType)
			attributeType = (ComplexAttributeType)parent;
	}

}
