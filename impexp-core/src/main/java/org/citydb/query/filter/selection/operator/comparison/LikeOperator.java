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

import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.Expression;
import org.citydb.query.filter.selection.expression.ExpressionName;
import org.citydb.query.filter.selection.expression.ValueReference;

public class LikeOperator extends AbstractComparisonOperator {
	private Expression leftOperand;
	private Expression rightOperand;
	private String wildCard = "*";
	private String singleCharacter = ".";
	private String escapeCharacter = "\\";
	private boolean matchCase = true;

	public LikeOperator(Expression leftOperand, Expression rightOperand) throws FilterException {
		setLeftOperand(leftOperand);
		setRightOperand(rightOperand);
	}
	
	public boolean isSetLeftOperand() {
		return leftOperand != null;
	}
		
	public Expression getLeftOperand() {
		return leftOperand;
	}
	
	public void setLeftOperand(Expression leftOperand) throws FilterException {
		if (leftOperand.getExpressionName() == ExpressionName.VALUE_REFERENCE 
				&& ((ValueReference)leftOperand).getSchemaPath().getLastNode().getPathElement().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
			throw new FilterException("The value reference of a like comparison must point to a simple thematic attribute.");

		this.leftOperand = leftOperand;
	}
	
	public boolean isSetRightOperand() {
		return rightOperand != null;
	}
	
	public Expression getRightOperand() {
		return rightOperand;
	}
	
	public void setRightOperand(Expression rightOperand) throws FilterException {
		if (rightOperand.getExpressionName() == ExpressionName.VALUE_REFERENCE 
				&& ((ValueReference)rightOperand).getSchemaPath().getLastNode().getPathElement().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
			throw new FilterException("The value reference of a like comparison must point to a simple thematic attribute.");

		this.rightOperand = rightOperand;
	}
	
	public Expression[] getOperands() {
		Expression[] result = new Expression[2];
		result[0] = leftOperand;
		result[1] = rightOperand;
		return result;
	}
	
	public String getWildCard() {
		return wildCard;
	}

	public void setWildCard(String wildCard) {
		if (wildCard != null)
			this.wildCard = wildCard;
	}

	public String getSingleCharacter() {
		return singleCharacter;
	}

	public void setSingleCharacter(String singleCharacter) {
		if (singleCharacter != null)
			this.singleCharacter = singleCharacter;
	}

	public String getEscapeCharacter() {
		return escapeCharacter;
	}

	public void setEscapeCharacter(String escapeCharacter) {
		if (escapeCharacter != null)
			this.escapeCharacter = escapeCharacter;
	}

	public boolean isMatchCase() {
		return matchCase;
	}

	public void setMatchCase(boolean matchCase) {
		this.matchCase = matchCase;
	}

	@Override
	public ComparisonOperatorName getOperatorName() {
		return ComparisonOperatorName.LIKE;
	}

	@Override
	public LikeOperator copy() throws FilterException {
		LikeOperator copy = new LikeOperator(leftOperand, rightOperand);
		copy.wildCard = wildCard;
		copy.singleCharacter = singleCharacter;
		copy.escapeCharacter = escapeCharacter;
		copy.matchCase = matchCase;
		
		return copy;
	}

}
