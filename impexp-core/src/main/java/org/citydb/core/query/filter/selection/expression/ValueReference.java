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
package org.citydb.core.query.filter.selection.expression;

import org.citydb.core.database.schema.mapping.AbstractPathElement;
import org.citydb.core.database.schema.mapping.PathElementType;
import org.citydb.core.database.schema.path.InvalidSchemaPathException;
import org.citydb.core.database.schema.path.SchemaPath;

public class ValueReference implements Expression {
	private final SchemaPath schemaPath;
	
	public ValueReference(SchemaPath schemaPath) throws InvalidSchemaPathException {
		if (schemaPath == null)
			throw new InvalidSchemaPathException("The schema path may not be null.");
		
		PathElementType type = schemaPath.getLastNode().getPathElement().getElementType();
		if (type == PathElementType.FEATURE_TYPE)
			throw new InvalidSchemaPathException("The value reference may not end with a feature element.");
		
		this.schemaPath = schemaPath;
	}

	public SchemaPath getSchemaPath() {
		return schemaPath;
	}
	
	public AbstractPathElement getTarget() {
		return schemaPath.getLastNode().getPathElement();
	}
	
	@Override
	public ExpressionName getExpressionName() {
		return ExpressionName.VALUE_REFERENCE;
	}
	
}
