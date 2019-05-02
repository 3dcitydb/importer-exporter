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
package org.citydb.query.filter.selection.expression;

import org.citydb.database.schema.mapping.SimpleType;
import org.citydb.sqlbuilder.expression.PlaceHolder;

public abstract class AbstractLiteral<T> implements Expression {
	protected T value;
	
	public AbstractLiteral(T value) {
		this.value = value;
	}
	
	public abstract LiteralType getLiteralType();
	public abstract boolean evalutesToSchemaType(SimpleType schemaType);
	public abstract PlaceHolder<?> convertToSQLPlaceHolder();
	
	public T getValue() {
		return value;
	}
	
	@Override
	public ExpressionName getExpressionName() {
		return ExpressionName.LITERAL;
	}
	
}
