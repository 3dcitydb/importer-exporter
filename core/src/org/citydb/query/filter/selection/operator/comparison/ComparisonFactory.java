package org.citydb.query.filter.selection.operator.comparison;

import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.Expression;

public class ComparisonFactory {

	public static BinaryComparisonOperator equalTo(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new BinaryComparisonOperator(leftOperand, ComparisonOperatorName.EQUAL_TO, rightOperand);
	}
	
	public static BinaryComparisonOperator notEqualTo(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new BinaryComparisonOperator(leftOperand, ComparisonOperatorName.NOT_EQUAL_TO, rightOperand);
	}
	
	public static BinaryComparisonOperator lessThan(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new BinaryComparisonOperator(leftOperand, ComparisonOperatorName.LESS_THAN, rightOperand);
	}
	
	public static BinaryComparisonOperator greaterThan(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new BinaryComparisonOperator(leftOperand, ComparisonOperatorName.GREATER_THAN, rightOperand);
	}
	
	public static BinaryComparisonOperator lessThanOrEqualTo(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new BinaryComparisonOperator(leftOperand, ComparisonOperatorName.LESS_THAN_OR_EQUAL_TO, rightOperand);
	}
	
	public static BinaryComparisonOperator greaterThanOrEqualTo(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new BinaryComparisonOperator(leftOperand, ComparisonOperatorName.GREATER_THAN_OR_EQUAL_TO, rightOperand);
	}
	
	public static BetweenOperator between(Expression operand, Expression lowerBoundary, Expression upperBoundary) throws FilterException {
		return new BetweenOperator(operand, lowerBoundary, upperBoundary);
	}
	
	public static LikeOperator like(Expression leftOperand, Expression rightOperand) throws FilterException {
		return new LikeOperator(leftOperand, rightOperand);
	}
	
	public static NullOperator isNull(Expression operand) throws FilterException {
		return new NullOperator(operand);
	}
	
}
