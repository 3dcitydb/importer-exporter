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
package org.citydb.database.schema.path.predicate.comparison;

import org.citydb.database.schema.mapping.SimpleAttribute;
import org.citydb.database.schema.path.AbstractNodePredicate;
import org.citydb.query.filter.selection.expression.AbstractLiteral;
import org.citydb.query.filter.selection.expression.DateLiteral;
import org.citydb.query.filter.selection.expression.TimestampLiteral;

import java.util.Objects;

public class EqualToPredicate extends AbstractNodePredicate {
	private final SimpleAttribute leftOperand;
	private final AbstractLiteral<?> rightOperand;
	
	public EqualToPredicate(SimpleAttribute leftOperand, AbstractLiteral<?> rightOperand) {
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}
	
	public SimpleAttribute getLeftOperand() {
		return leftOperand;
	}

	public AbstractLiteral<?> getRightOperand() {
		return rightOperand;
	}
	
	@Override
	public boolean isEqualTo(AbstractNodePredicate other) {
		if (other == this)
			return true;
		
		if (!(other instanceof EqualToPredicate))
			return false;
		
		EqualToPredicate predicate = (EqualToPredicate)other;
		return leftOperand == predicate.leftOperand && Objects.equals(rightOperand.getValue(), predicate.rightOperand.getValue());
	}

	@Override
	public ComparisonPredicateName getPredicateName() {
		return ComparisonPredicateName.EQUAL_TO;
	}

	@Override
	public String toString(boolean removeAttributePrefixes) {
		StringBuilder builder = new StringBuilder();
		
		String operandName = leftOperand.getName();		
		if (contextNode != null 
				&& (contextNode.getPathElement().getPath().equals(operandName) || contextNode.getPathElement() == leftOperand))
			builder.append(".");
		else {
			boolean usePrefix = true;

			if (leftOperand.getPath().startsWith("@")) {
				builder.append("@");

				if (removeAttributePrefixes 
						&& contextNode != null 
						&& contextNode.getPathElement().getSchema() == leftOperand.getSchema() 
						&& !operandName.equals("id"))
					usePrefix = false;
			}

			if (usePrefix)
				builder.append(leftOperand.getSchema().isSetXMLPrefix() ? leftOperand.getSchema().getXMLPrefix() : leftOperand.getSchema().getId()).append(":");
			
			builder.append(operandName);
		}
		
		builder.append("=");
		switch (rightOperand.getLiteralType()) {
		case STRING:
			builder.append("'").append(rightOperand.getValue()).append("'");
			break;
		case DATE:
			builder.append("'").append(((DateLiteral)rightOperand).getXMLLiteral()).append("'");
			break;
		case TIMESTAMP:
			builder.append("'").append(((TimestampLiteral)rightOperand).getXMLLiteral()).append("'");
			break;
		default:
			builder.append(rightOperand.getValue());
		}			
		
		return builder.toString();
	}
	
}
