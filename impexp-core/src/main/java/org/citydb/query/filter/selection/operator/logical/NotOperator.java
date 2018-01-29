package org.citydb.query.filter.selection.operator.logical;

import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.Predicate;

public class NotOperator extends AbstractLogicalOperator {
	private Predicate operand;

	public NotOperator(Predicate operand) {
		this.operand = operand;
	}
	
	public boolean isSetOperand() {
		return operand != null;
	}
		
	public Predicate getOperand() {
		return operand;
	}

	public void setOperand(Predicate operand) {
		this.operand = operand;
	}

	@Override
	public LogicalOperatorName getOperatorName() {
		return LogicalOperatorName.NOT;
	}

	@Override
	public NotOperator copy() throws FilterException {
		return new NotOperator(operand);
	}
	
}