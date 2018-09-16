/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "objectProperty")
public class ObjectProperty extends AbstractRefTypeProperty<ObjectType> {
	@XmlAttribute(name = "target", required = true)
	@XmlJavaTypeAdapter(ObjectTypeAdapter.class)
	protected ObjectType type;

	protected ObjectProperty() {
	}
    
    public ObjectProperty(String path, ObjectType type, AppSchema schema) {
    	super(path, schema);
    	this.type = type;
    }

	@Override
	public ObjectType getType() {
		return type;
	}

	@Override
	public boolean isSetType() {
		return type != null;
	}

	@Override
	public void setType(ObjectType type) {
		this.type = type;
	}

	@Override
	public PathElementType getElementType() {
		return PathElementType.OBJECT_PROPERTY;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		if (type.hasLocalProperty(MappingConstants.IS_XLINK)) {
			ObjectType ref = schemaMapping.getObjectTypeById(type.getId());
			if (ref == null)
				throw new SchemaMappingException("Failed to resolve object type reference '" + type.getId() + "'.");

			type = ref;
		}
		
		super.validate(schemaMapping, parent);
	}

}
