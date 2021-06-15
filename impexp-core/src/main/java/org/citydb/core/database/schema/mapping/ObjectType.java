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
package org.citydb.core.database.schema.mapping;

import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlType(name = "objectType", propOrder = {
		"extension",
		"properties"
})
public class ObjectType extends AbstractObjectType<ObjectType> {
	protected ObjectTypeExtension extension;

	protected ObjectType() {
	}
    
    public ObjectType(String id, String path, String table, int objectClassId, AppSchema schema, SchemaMapping schemaMapping) {
    	super(id, path, table, objectClassId, schema, schemaMapping);
    }

	@Override
	public AbstractExtension<ObjectType> getExtension() {
		return extension;
	}

	@Override
	public boolean isSetExtension() {
		return extension != null;
	}
	
	@Override
	public void setExtension(AbstractExtension<ObjectType> extension) {
		this.extension = (ObjectTypeExtension)extension;
	}

	@Override
	public List<ObjectType> listSubTypes(boolean skipAbstractTypes) {
		return listSubTypes(schemaMapping.objectTypes, skipAbstractTypes);
	}

	@Override
	public PathElementType getElementType() {
		return PathElementType.OBJECT_TYPE;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		super.validate(schemaMapping, parent);
		schema.addObjectType(this);

		if (isSetExtension())
			extension.validate(schemaMapping, this);
	}

}
