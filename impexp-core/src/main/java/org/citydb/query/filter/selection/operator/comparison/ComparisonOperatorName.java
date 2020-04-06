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
package org.citydb.query.filter.selection.operator.comparison;

import org.citydb.query.filter.selection.operator.OperatorName;

import java.util.EnumSet;

public enum ComparisonOperatorName implements OperatorName {
	EQUAL_TO("="),
	NOT_EQUAL_TO("<>"),
	LESS_THAN("<"),
	GREATER_THAN(">"),
	LESS_THAN_OR_EQUAL_TO("<="),
	GREATER_THAN_OR_EQUAL_TO(">="),
	BETWEEN("BETWEEN"),
	LIKE("LIKE"),
	NULL("IS_NULL");
	
	public static final EnumSet<ComparisonOperatorName> BINARY_COMPARISONS = EnumSet.of(
			EQUAL_TO, NOT_EQUAL_TO, LESS_THAN, GREATER_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN_OR_EQUAL_TO);
	
	final String symbol;
	
	ComparisonOperatorName(String symbol) {
		this.symbol = symbol;
	}
	
	@Override
	public String getSymbol() {
		return symbol;
	}
}
