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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "joinTable", propOrder = {
		"join",
		"inverseJoin"
})
public class JoinTable extends AbstractJoin {
	@XmlAttribute(required = true)
	protected String table;
	@XmlElement(required = true)
	protected Join join;
	@XmlElement(required = true)
	protected Join inverseJoin;

	protected JoinTable() {	
	}

	public JoinTable(String table) {
		this.table = table;
	}

	public Join[] getJoins() {
		return new Join[]{join, inverseJoin};
	}

	public boolean isSetJoin() {
		return join != null;
	}

	public Join getJoin() {
		return join;
	}

	public void setJoin(Join join) {
		this.join = join;
	}

	public boolean isSetInverseJoin() {
		return inverseJoin != null;
	}

	public Join getInverseJoin() {
		return inverseJoin;
	}

	public void setInverseJoin(Join inverseJoin) {
		this.inverseJoin = inverseJoin;
	}

	public String getTable() {
		return table;
	}

	public boolean isSetTable() {
		return table != null && !table.isEmpty();
	}

	public void setTable(String table) {
		this.table = table;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent, Object transitiveParent) throws SchemaMappingException {
		if (!isSetJoin() || !isSetInverseJoin())
			throw new SchemaMappingException("A join table requires both a join and an inverse join."); 

		if (!(parent instanceof AbstractTypeProperty<?>))
			throw new SchemaMappingException("The parent of a join table must be a type property.");

		if (!(transitiveParent instanceof AbstractType<?>) && !(transitiveParent instanceof PropertyInjection))
			throw new SchemaMappingException("The transitive parent of a join table must be a type or a property injection.");

		join.validate(schemaMapping, transitiveParent, null);
		inverseJoin.validate(schemaMapping, parent, null);
	}

}
