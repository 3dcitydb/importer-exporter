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
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlType(name = "featureProperty")
public class FeatureProperty extends AbstractRefTypeProperty<FeatureType> {
	@XmlAttribute(name = "target", required = true)
	@XmlJavaTypeAdapter(FeatureTypeAdapter.class)
	protected FeatureType type;

	protected FeatureProperty() {
	}
    
    public FeatureProperty(String path, FeatureType type, AppSchema schema) {
    	super(path, schema);
    	this.type = type;
    }

	@Override
	public FeatureType getType() {
		return type;
	}

	@Override
	public boolean isSetType() {
		return type != null;
	}

	@Override
	public void setType(FeatureType type) {
		this.type = type;
	}

	@Override
	public PathElementType getElementType() {
		return PathElementType.FEATURE_PROPERTY;
	}

	@Override
	protected void validate(SchemaMapping schemaMapping, Object parent) throws SchemaMappingException {
		if (type.hasLocalProperty(MappingConstants.IS_XLINK)) {
			FeatureType ref = schemaMapping.getFeatureTypeById(type.getId());
			if (ref == null)
				throw new SchemaMappingException("Failed to resolve feature type reference '" + type.getId() + "'.");

			type = ref;
		}
		
		super.validate(schemaMapping, parent);
	}

}
