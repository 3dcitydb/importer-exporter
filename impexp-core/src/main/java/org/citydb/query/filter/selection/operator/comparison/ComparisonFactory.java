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
package org.citydb.query.filter.selection.operator.comparison;

import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.Expression;

public class ComparisonFactory {

	public static BinaryComparisonOperator equalTo(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new BinaryComparisonOperator(leftOperand, ComparisonOperatorName.EQUAL_TO, rightOperand);
	}
	
	public static BinaryComparisonOperator notEqualTo(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new BinaryComparisonOperator(leftOperand, ComparisonOperatorName.NOT_EQUAL_TO, rightOperand);
	}
	
	public static BinaryComparisonOperator lessThan(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new BinaryComparisonOperator(leftOperand, ComparisonOperatorName.LESS_THAN, rightOperand);
	}
	
	public static BinaryComparisonOperator greaterThan(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new BinaryComparisonOperator(leftOperand, ComparisonOperatorName.GREATER_THAN, rightOperand);
	}
	
	public static BinaryComparisonOperator lessThanOrEqualTo(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new BinaryComparisonOperator(leftOperand, ComparisonOperatorName.LESS_THAN_OR_EQUAL_TO, rightOperand);
	}
	
	public static BinaryComparisonOperator greaterThanOrEqualTo(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new BinaryComparisonOperator(leftOperand, ComparisonOperatorName.GREATER_THAN_OR_EQUAL_TO, rightOperand);
	}
	
	public static BetweenOperator between(Expression operand, Expression lowerBoundary, Expression upperBoundary) throws FilterException {
		return new BetweenOperator(operand, lowerBoundary, upperBoundary);
	}
	
	public static LikeOperator like(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new LikeOperator(leftOperand, rightOperand);
	}
	
	public static NullOperator isNull(Expression operand) throws FilterException {
		return new NullOperator(operand);
	}
	
}
