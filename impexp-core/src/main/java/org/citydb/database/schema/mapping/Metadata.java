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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "metadata", propOrder = {
		"name",
		"version",
		"description",
		"dbPrefix"
})
public class Metadata {
	@XmlElement(required = true)
	protected String name;
	protected String version;
	protected String description;
	@XmlElement(required = true)
	protected String dbPrefix;
	
	protected Metadata() {
	}
	
	public Metadata(String name, String dbPrefix) {
		this.name = name;
		this.dbPrefix = dbPrefix;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean isSetName() {
		return name != null;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getVersion() {
		return version;
	}
	
	public boolean isSetVersion() {
		return version != null;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}
	
	public boolean isSetDescription() {
		return description != null;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDBPrefix() {
		return dbPrefix;
	}
	
	public boolean isSetDBPrefix() {
		return dbPrefix != null;
	}

	public void setDBPrefix(String dbPrefix) {
		this.dbPrefix = dbPrefix;
	}
	
	public String getIdentifier() {
		return new StringBuilder(name)
				.append(" ")
				.append(version)
				.toString();
	}

	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		if (!isSetName())
			throw new SchemaMappingException("The metadata of a schema mapping must define a name.");
		
		if (!isSetDBPrefix())
			throw new SchemaMappingException("The metadata of a schema mapping must define a database prefix.");
	}

	@Override
	public String toString() {
		return getIdentifier();
	}
	
}
