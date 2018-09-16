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

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class FeatureTypeAdapter extends XmlAdapter<String, FeatureType> {
	private final SchemaMapping schemaMapping;

	protected FeatureTypeAdapter() {
		schemaMapping = null;
	}

	public FeatureTypeAdapter(SchemaMapping schemaMapping) {
		this.schemaMapping = schemaMapping;
	}

	@Override
	public FeatureType unmarshal(String id) throws Exception {
		if (id == null || id.isEmpty())
			throw new SchemaMappingException("The attribute 'id' is not set for the feature property.");

		FeatureType type = null;
		if (schemaMapping != null)
			type = schemaMapping.getFeatureTypeById(id);
		
		if (type == null) {
			type = new FeatureType();
			type.id = id;
			type.setLocalProperty(MappingConstants.IS_XLINK, true);
		}
		
		return type;
	}

	@Override
	public String marshal(FeatureType type) throws Exception {
		return type != null ? type.id : null;
	}

}
