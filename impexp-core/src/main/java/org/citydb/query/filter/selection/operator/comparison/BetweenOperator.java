package org.citydb.query.filter.selection.operator.comparison;

import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.Expression;
import org.citydb.query.filter.selection.expression.ExpressionName;
import org.citydb.query.filter.selection.expression.ValueReference;

public class BetweenOperator extends AbstractComparisonOperator {
	private Expression operand;
	private Expression lowerBoundary;
	private Expression upperBoundary;	
	
	public BetweenOperator(Expression operand, Expression lowerBoundary, Expression upperBoundary) throws FilterException {
		setOperand(operand);
		this.lowerBoundary = lowerBoundary;
		this.upperBoundary = upperBoundary;
	}
	
	public boolean isSetOperand() {
		return operand != null;
	}
	
	public Expression getOperand() {
		return operand;
	}

	public void setOperand(Expression operand) throws FilterException {
		if (operand.getExpressionName() == ExpressionName.VALUE_REFERENCE 
				&& ((ValueReference)operand).getSchemaPath().getLastNode().getPathElement().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
			throw new FilterException("The value reference of a between comparison must point to a simple thematic attribute.");

		this.operand = operand;
	}
	
	public boolean isSetLowerBoundary() {
		return lowerBoundary != null;
	}

	public Expression getLowerBoundary() {
		return lowerBoundary;
	}

	public void setLowerBoundary(Expression lowerBoundary) {
		this.lowerBoundary = lowerBoundary;
	}

	public boolean isSetUpperBoundary() {
		return upperBoundary != null;
	}
	
	public Expression getUpperBoundary() {
		return upperBoundary;
	}

	public void setUpperBoundary(Expression upperBoundary) {
		this.upperBoundary = upperBoundary;
	}

	@Override
	public ComparisonOperatorName getOperatorName() {
		return ComparisonOperatorName.BETWEEN;
	}

	@Override
	public BetweenOperator copy() throws FilterException {
		return new BetweenOperator(operand, lowerBoundary, upperBoundary);
	}

}
