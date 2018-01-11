package org.citydb.database.schema.path.predicate.logical;

import org.citydb.database.schema.path.AbstractNodePredicate;
import org.citydb.database.schema.path.predicate.comparison.EqualToPredicate;

public class BinaryLogicalPredicate extends AbstractNodePredicate {
	private final EqualToPredicate leftOperand;
	private final AbstractNodePredicate rightOperand;
	private final LogicalPredicateName name;
	
	public BinaryLogicalPredicate(EqualToPredicate leftOperand, LogicalPredicateName name, AbstractNodePredicate rightOperand) {
		this.leftOperand = leftOperand;
		this.rightOperand = rightOperand;
		this.name = name;
	}
	
	public EqualToPredicate getLeftOperand() {
		return leftOperand;
	}

	public AbstractNodePredicate getRightOperand() {
		return rightOperand;
	}

	public LogicalPredicateName getName() {
		return name;
	}
	
	@Override
	public boolean isEqualTo(AbstractNodePredicate other) {
		if (other == this)
			return true;
		
		if (!(other instanceof BinaryLogicalPredicate))
			return false;
		
		BinaryLogicalPredicate predicate = (BinaryLogicalPredicate)other;
		if (name != predicate.name)
			return false;
		
		return ((leftOperand.isEqualTo(predicate.leftOperand) && rightOperand.isEqualTo(predicate.rightOperand))
				|| (leftOperand.isEqualTo(predicate.rightOperand) && rightOperand.isEqualTo(predicate.leftOperand)));
	}

	@Override
	public LogicalPredicateName getPredicateName() {
		return name;
	}

	@Override
	public String toString(boolean removeAttributePrefixes) {
		return new StringBuilder()
		.append(leftOperand.toString(removeAttributePrefixes))
		.append(" ").append(name.getSymbol()).append(" ")
		.append(rightOperand.toString(removeAttributePrefixes)).toString();
	}

}
