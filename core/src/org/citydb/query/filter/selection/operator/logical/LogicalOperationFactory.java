package org.citydb.query.filter.selection.operator.logical;

import java.util.List;

import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;

public class LogicalOperationFactory {

	public static BinaryLogicalOperator AND(List<Predicate> operands) throws FilterException {
		return new BinaryLogicalOperator(LogicalOperatorName.AND, operands);
	}
	
	public static BinaryLogicalOperator AND(Predicate... operands) throws FilterException {
		return new BinaryLogicalOperator(LogicalOperatorName.AND, operands);
	}
	
	public static BinaryLogicalOperator OR(List<Predicate> operands) throws FilterException {
		return new BinaryLogicalOperator(LogicalOperatorName.OR, operands);
	}
	
	public static BinaryLogicalOperator OR(Predicate... operands) throws FilterException {
		return new BinaryLogicalOperator(LogicalOperatorName.OR, operands);
	}
	
	public static NotOperator NOT(Predicate operand) {
		return new NotOperator(operand);
	}
	
}
