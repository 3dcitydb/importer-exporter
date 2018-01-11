package org.citydb.query.filter.selection.operator.comparison;

import org.citydb.database.schema.mapping.AbstractProperty;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.Expression;
import org.citydb.query.filter.selection.expression.ExpressionName;
import org.citydb.query.filter.selection.expression.ValueReference;

public class NullOperator extends AbstractComparisonOperator {
	private Expression operand;
	
	public NullOperator(Expression operand) throws FilterException {
		setOperand(operand);
	}
	
	public boolean isSetOperand() {
		return operand != null;
	}
	
	public Expression getOperand() {
		return operand;
	}

	public void setOperand(Expression operand) throws FilterException {
		if (operand.getExpressionName() == ExpressionName.VALUE_REFERENCE 
				&& !(((ValueReference)operand).getSchemaPath().getLastNode().getPathElement() instanceof AbstractProperty))
			throw new FilterException("The value reference of a null comparison must point to a property.");

		this.operand = operand;
	}
	
	@Override
	public ComparisonOperatorName getOperatorName() {
		return ComparisonOperatorName.NULL;
	}

	@Override
	public NullOperator copy() throws FilterException {
		return new NullOperator(operand);
	}

}
