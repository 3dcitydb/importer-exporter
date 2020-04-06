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
package org.citydb.query.filter.projection;

import org.citydb.query.filter.FilterException;
import org.citygml4j.model.citygml.CityGMLClass;

import java.util.EnumSet;
import java.util.Objects;

public class GenericAttribute {
	private final String name;
	private final CityGMLClass type;
	
	private final EnumSet<CityGMLClass> types = EnumSet.of(
			CityGMLClass.STRING_ATTRIBUTE,
			CityGMLClass.DOUBLE_ATTRIBUTE,
			CityGMLClass.INT_ATTRIBUTE,
			CityGMLClass.DATE_ATTRIBUTE,
			CityGMLClass.URI_ATTRIBUTE,
			CityGMLClass.MEASURE_ATTRIBUTE,
			CityGMLClass.GENERIC_ATTRIBUTE_SET,
			CityGMLClass.UNDEFINED);
	
	public GenericAttribute(String name, CityGMLClass type) throws FilterException {
		if (type == null)
			type = CityGMLClass.UNDEFINED;
		
		if (!types.contains(type))
			throw new FilterException(type + " is not a valid generic attribute type.");
		
		this.name = name;
		this.type = type;
	}
	
	public GenericAttribute(String name) throws FilterException {
		this(name, CityGMLClass.UNDEFINED);
	}

	public String getName() {
		return name;
	}

	public CityGMLClass getType() {
		return type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof GenericAttribute))
			return false;

		GenericAttribute other = (GenericAttribute)obj;
		return name.equals(other.name) && type == other.type;
	}
	
}
