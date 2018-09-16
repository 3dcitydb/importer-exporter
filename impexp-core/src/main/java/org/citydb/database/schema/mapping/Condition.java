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

@XmlType(name = "condition")
public class Condition {
	@XmlAttribute(required = true)
	protected String column;
	@XmlAttribute(required = true)
	protected String value;
	@XmlAttribute(required = true)
	protected SimpleType type;
	
	protected Condition() {
	}
	
	public Condition(String column, String value, SimpleType type) {
		this.column = column;
		this.value = value;
		this.type = type;
	}

	public String getColumn() {
		return column;
	}

	public boolean isSetColumn() {
		return  column != null && !column.isEmpty();
	}
	
	public void setColumn(String column) {
		this.column = column;
	}

	public String getValue() {
		return value;
	}

	public boolean isSetValue() {
		return value != null && !value.isEmpty();
	}
	
	public void setValue(String value) {
		this.value = value;
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

}
