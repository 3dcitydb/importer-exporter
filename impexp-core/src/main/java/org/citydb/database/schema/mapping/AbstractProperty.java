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

@XmlType(name = "abstractProperty")
public abstract class AbstractProperty extends AbstractPathElement implements Joinable {
	@XmlAttribute
	protected Integer minOccurs = null;
	@XmlAttribute
	protected Integer maxOccurs = null;

	protected AbstractProperty() {
	}

	public AbstractProperty(String path, AppSchema schema) {
		super(path, schema);
	}

	public int getMinOccurs() {
		return minOccurs != null ? minOccurs : 0;
	}

	public boolean isSetMinOccurs() {
		return minOccurs != null;
	}

	public void setMinOccurs(int minOccurs) {
		this.minOccurs = minOccurs > 0 ? minOccurs : null;
	}

	public Integer getMaxOccurs() {
		return maxOccurs;
	}

	public boolean isSetMaxOccurs() {
		return maxOccurs != null;
	}

	public void setMaxOccurs(Integer maxOccurs) {
		this.maxOccurs = maxOccurs > 0 ? maxOccurs : null;
	}

	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		super.validate(schemaMapping, parent);
		
		if (isSetJoin())
			getJoin().validate(schemaMapping, this, parent);

		if (isSetMaxOccurs() && getMaxOccurs() < getMinOccurs())
			throw new SchemaMappingException("Invalid occurrence constraint: " +
					"'minOccurs' ('" + getMinOccurs() + "') must not be greater than 'maxOccurs ('" + getMaxOccurs() + "').");
	}
}
