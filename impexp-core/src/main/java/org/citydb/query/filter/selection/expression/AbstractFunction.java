package org.citydb.query.filter.selection.expression;

public abstract class AbstractFunction implements Expression {

	@Override
	public ExpressionName getExpressionName() {
		return ExpressionName.FUNCTION;
	}
	
}
