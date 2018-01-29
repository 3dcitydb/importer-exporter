package org.citydb.database.schema.path.predicate.comparison;

import org.citydb.database.schema.mapping.SimpleAttribute;
import org.citydb.query.filter.selection.expression.AbstractLiteral;

public class ComparisonPredicateFactory {
	
	public static EqualToPredicate equalTo(SimpleAttribute leftOperand, AbstractLiteral<?> rightOperand) {
		return new EqualToPredicate(leftOperand, rightOperand);
	}
}
