package org.citydb.database.schema.path.predicate.comparison;

import java.util.Objects;

import org.citydb.database.schema.mapping.SimpleAttribute;
import org.citydb.database.schema.path.AbstractNodePredicate;
import org.citydb.query.filter.selection.expression.AbstractLiteral;
import org.citydb.query.filter.selection.expression.DateLiteral;
import org.citydb.query.filter.selection.expression.TimestampLiteral;

public class EqualToPredicate extends AbstractNodePredicate {
	private final SimpleAttribute leftOperand;
	private final AbstractLiteral<?> rightOperand;
	
	public EqualToPredicate(SimpleAttribute leftOperand, AbstractLiteral<?> rightOperand) {
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
	}
	
	public SimpleAttribute getLeftOperand() {
		return leftOperand;
	}

	public AbstractLiteral<?> getRightOperand() {
		return rightOperand;
	}
	
	@Override
	public boolean isEqualTo(AbstractNodePredicate other) {
		if (other == this)
			return true;
		
		if (!(other instanceof EqualToPredicate))
			return false;
		
		EqualToPredicate predicate = (EqualToPredicate)other;
		return leftOperand == predicate.leftOperand && Objects.equals(rightOperand.getValue(), predicate.rightOperand.getValue());
	}

	@Override
	public ComparisonPredicateName getPredicateName() {
		return ComparisonPredicateName.EQUAL_TO;
	}

	@Override
	public String toString(boolean removeAttributePrefixes) {
		StringBuilder builder = new StringBuilder();
		
		String operandName = leftOperand.getName();		
		if (contextNode != null 
				&& (contextNode.getPathElement().getPath().equals(operandName) || contextNode.getPathElement() == leftOperand))
			builder.append(".");
		else {
			boolean usePrefix = true;

			if (leftOperand.getPath().startsWith("@")) {
				builder.append("@");

				if (removeAttributePrefixes 
						&& contextNode != null 
						&& contextNode.getPathElement().getSchema() == leftOperand.getSchema() 
						&& !operandName.equals("id"))
					usePrefix = false;
			}

			if (usePrefix)
				builder.append(leftOperand.getSchema().isSetXMLPrefix() ? leftOperand.getSchema().getXMLPrefix() : leftOperand.getSchema().getId()).append(":");
			
			builder.append(operandName);
		}
		
		builder.append("=");
		switch (rightOperand.getLiteralType()) {
		case STRING:
			builder.append("'").append(rightOperand.getValue()).append("'");
			break;
		case DATE:
			builder.append("'").append(((DateLiteral)rightOperand).getXMLLiteral()).append("'");
			break;
		case TIMESTAMP:
			builder.append("'").append(((TimestampLiteral)rightOperand).getXMLLiteral()).append("'");
			break;
		default:
			builder.append(rightOperand.getValue());
		}			
		
		return builder.toString();
	}
	
}
