package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.AbstractAttribute;
import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.ComplexAttribute;
import org.citydb.database.schema.path.predicate.comparison.ComparisonPredicateName;
import org.citydb.database.schema.path.predicate.comparison.EqualToPredicate;
import org.citydb.database.schema.path.predicate.logical.BinaryLogicalPredicate;

public final class ComplexAttributeNode extends AbstractNode<ComplexAttribute> {

	protected ComplexAttributeNode(ComplexAttribute complexAttribute) {
		super(complexAttribute);
	}
	
	protected ComplexAttributeNode(ComplexAttributeNode other) {
		super(other);
	}
	
	@Override
	protected boolean isValidChild(AbstractPathElement candidate) {
		if (candidate instanceof AbstractAttribute) {
			for (AbstractAttribute attribute : pathElement.getType().getAttributes())
				if (attribute == candidate)
					return true;			
		}

		return false;
	}

	@Override
	protected boolean isValidPredicate(AbstractNodePredicate candidate) {
		if (candidate.getPredicateName() == ComparisonPredicateName.EQUAL_TO) {
			EqualToPredicate predicate = (EqualToPredicate)candidate;

			if (pathElement.getType().getAttributes().contains(predicate.getLeftOperand()))				
				return predicate.getRightOperand().evalutesToSchemaType(predicate.getLeftOperand().getType());
		}
		
		else {
			BinaryLogicalPredicate predicate = (BinaryLogicalPredicate)candidate;
			if (isValidPredicate(predicate.getLeftOperand()))
				return isValidPredicate(predicate.getRightOperand());			
		}

		return false;
	}

	@Override
	protected ComplexAttributeNode copy() {
		return new ComplexAttributeNode(this);
	}
	
}
