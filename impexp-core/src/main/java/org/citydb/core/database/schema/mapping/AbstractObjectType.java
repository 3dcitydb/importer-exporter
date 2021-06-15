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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;

@XmlTransient
public abstract class AbstractObjectType<T extends AbstractObjectType<T>> extends AbstractType<T> {
	@XmlAttribute(required = true)
	@XmlJavaTypeAdapter(CollapsedStringAdapter.class)
	@XmlID
	@XmlSchemaType(name = "ID")
	protected String id;
	@XmlAttribute(required = true)
	protected String table;
	@XmlAttribute(required = true)
	protected Integer objectClassId;

	protected AbstractObjectType() {
	}

	public AbstractObjectType(String id, String path, String table, int objectClassId, AppSchema schema, SchemaMapping schemaMapping) {
		super(path, schema, schemaMapping);
		this.id = id;
		this.table = table;
		this.objectClassId = objectClassId;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public boolean isSetId() {
		return id != null && !id.isEmpty();
	}

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getTable() {
		return table;
	}

	@Override
	public boolean isSetTable() {
		return table!= null && !table.isEmpty();
	}

	@Override
	public void setTable(String table) {
		this.table = table;
	}

	public boolean hasSharedTable(boolean skipAbstractTypes) {
		// add types from other schema mappings if required
		List<AbstractObjectType<?>> candidates = schemaMapping.getAbstractObjectTypes();
		for (T candidate : listSuperTypes(false)) {
			if (!candidates.contains(candidate) && candidate.schemaMapping != schemaMapping)
				candidates.addAll(candidate.schemaMapping.getAbstractObjectTypes());
		}

		for (AbstractObjectType<?> candidate : candidates) {
			if (candidate == this)
				continue;

			if (candidate.getTable().equals(table)) {
				if (skipAbstractTypes && candidate.isAbstract())
					continue;

				return true;
			}
		}

		return false;
	}

	@Override
	public int getObjectClassId() {
		return objectClassId != null ? objectClassId.intValue() : 0;
	}

	@Override
	public boolean isSetObjectClass() {
		return objectClassId != null;
	}

	@Override
	public void setObjectClassId(int objectClassId) {
		if (objectClassId >= 0)
			this.objectClassId = objectClassId;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		super.validate(schemaMapping, parent);

		if (!isSetId())
			throw new SchemaMappingException("An object type requires an id value.");
		else if (!isSetTable())
			throw new SchemaMappingException("An object type requires a table.");
		else if (!isSetObjectClass())
			throw new SchemaMappingException("An object type requires an objectClassId.");
	}

}
