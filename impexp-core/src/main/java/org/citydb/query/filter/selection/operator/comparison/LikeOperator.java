package org.citydb.query.filter.selection.operator.comparison;

import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.Expression;
import org.citydb.query.filter.selection.expression.ExpressionName;
import org.citydb.query.filter.selection.expression.ValueReference;

public class LikeOperator extends AbstractComparisonOperator {
	private Expression leftOperand;
	private Expression rightOperand;
	private String wildCard = "";
	private String singleCharacter = "";
	private String escapeCharacter = "";
	private boolean matchCase = true;

	public LikeOperator(Expression leftOperand, Expression rightOperand) throws FilterException {
		setLeftOperand(leftOperand);
		setRightOperand(rightOperand);
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
			throw new FilterException("The value reference of a like comparison must point to a simple thematic attribute.");

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
			throw new FilterException("The value reference of a like comparison must point to a simple thematic attribute.");

		this.rightOperand = rightOperand;
	}
	
	public Expression[] getOperands() {
		Expression[] result = new Expression[2];
		result[0] = leftOperand;
		result[1] = rightOperand;
		return result;
	}
	
	public String getWildCard() {
		return wildCard;
	}

	public void setWildCard(String wildCard) {
		if (wildCard == null)
			wildCard = "";
		
		this.wildCard = wildCard;
	}

	public String getSingleCharacter() {
		return singleCharacter;
	}

	public void setSingleCharacter(String singleCharacter) {
		if (singleCharacter == null)
			singleCharacter = "";
		
		this.singleCharacter = singleCharacter;
	}

	public String getEscapeCharacter() {
		return escapeCharacter;
	}

	public void setEscapeCharacter(String escapeCharacter) {
		if (escapeCharacter == null)
			escapeCharacter = "";
		
		this.escapeCharacter = escapeCharacter;
	}

	public boolean isMatchCase() {
		return matchCase;
	}

	public void setMatchCase(boolean matchCase) {
		this.matchCase = matchCase;
	}

	@Override
	public ComparisonOperatorName getOperatorName() {
		return ComparisonOperatorName.LIKE;
	}

	@Override
	public LikeOperator copy() throws FilterException {
		LikeOperator copy = new LikeOperator(leftOperand, rightOperand);
		copy.wildCard = wildCard;
		copy.singleCharacter = singleCharacter;
		copy.escapeCharacter = escapeCharacter;
		copy.matchCase = matchCase;
		
		return copy;
	}

}
