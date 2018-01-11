package org.citydb.query.filter.selection.operator.comparison;

import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.Expression;
import org.citydb.query.filter.selection.expression.ExpressionName;
import org.citydb.query.filter.selection.expression.ValueReference;

public class BinaryComparisonOperator extends AbstractComparisonOperator {
	private final ComparisonOperatorName name;
	private Expression leftOperand;
	private Expression rightOperand;
	private boolean matchCase = true;
	private MatchAction matchAction = MatchAction.ANY;
	
	public BinaryComparisonOperator(Expression leftOperand, ComparisonOperatorName name, Expression rightOperand) throws FilterException {
		if (!ComparisonOperatorName.BINARY_COMPARISONS.contains(name))
			throw new FilterException("Allowed binary comparisons only include " + ComparisonOperatorName.BINARY_COMPARISONS);

		setLeftOperand(leftOperand);
		setRightOperand(rightOperand);
		this.name = name;
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
			throw new FilterException("The value reference of a binary comparison must point to a simple thematic attribute.");
		
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
			throw new FilterException("The value reference of a binary comparison must point to a simple thematic attribute.");

		this.rightOperand = rightOperand;
	}
	
	public Expression[] getOperands() {
		Expression[] result = new Expression[2];
		result[0] = leftOperand;
		result[1] = rightOperand;
		return result;
	}
	
	@Override
	public ComparisonOperatorName getOperatorName() {
		return name;
	}

	public boolean isMatchCase() {
		return matchCase;
	}

	public void setMatchCase(boolean matchCase) {
		this.matchCase = matchCase;
	}

	public MatchAction getMatchAction() {
		return matchAction;
	}

	public void setMatchAction(MatchAction matchAction) {
		this.matchAction = matchAction;
	}

	@Override
	public BinaryComparisonOperator copy() throws FilterException {
		BinaryComparisonOperator copy = new BinaryComparisonOperator(leftOperand, name, rightOperand);
		copy.matchAction = matchAction;
		copy.matchCase = matchCase;
		
		return copy;
	}
	
}