package org.citydb.database.schema.path.predicate.logical;

import org.citydb.database.schema.path.AbstractNodePredicate;
import org.citydb.database.schema.path.predicate.comparison.EqualToPredicate;

public class LogicalPredicateFactory {

	public static BinaryLogicalPredicate AND(EqualToPredicate leftOperand, AbstractNodePredicate rightOperand) {
		return new BinaryLogicalPredicate(leftOperand, LogicalPredicateName.AND, rightOperand);
	}
	
	public static BinaryLogicalPredicate OR(EqualToPredicate leftOperand, AbstractNodePredicate rightOperand) {
		return new BinaryLogicalPredicate(leftOperand, LogicalPredicateName.OR, rightOperand);
	}
	
}
