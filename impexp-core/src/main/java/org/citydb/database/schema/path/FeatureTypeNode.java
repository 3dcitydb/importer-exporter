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
package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.FeatureType;

public final class FeatureTypeNode extends AbstractTypeNode<FeatureType> {
	private boolean useSchemaElement;

	FeatureTypeNode(FeatureType featureType) {
		super(featureType);
	}
	
	FeatureTypeNode(FeatureTypeNode other) {
		super(other);
		this.useSchemaElement = other.useSchemaElement;
	}

	public boolean isUseSchemaElement() {
		return useSchemaElement;
	}

	public void setUseSchemaElement(boolean useSchemaElement) {
		this.useSchemaElement = useSchemaElement;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		if (useSchemaElement)
			builder.append("schema-element(").append(super.toString()).append(")");
		else
			builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected FeatureTypeNode copy() {
		return new FeatureTypeNode(this);
	}
	
}
