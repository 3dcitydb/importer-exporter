package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.AbstractProperty;
import org.citydb.database.schema.mapping.AbstractType;
import org.citydb.database.schema.mapping.ComplexAttribute;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.database.schema.mapping.SimpleAttribute;
import org.citydb.database.schema.path.predicate.comparison.ComparisonPredicateName;
import org.citydb.database.schema.path.predicate.comparison.EqualToPredicate;
import org.citydb.database.schema.path.predicate.logical.BinaryLogicalPredicate;

public abstract class AbstractTypeNode<T extends AbstractType<T>> extends AbstractNode<T> {

	AbstractTypeNode(T objectType) {
		super(objectType);
	}
	
	AbstractTypeNode(AbstractTypeNode<T> other) {
		super(other);
	}

	@Override
	protected boolean isValidChild(AbstractPathElement candidate) {
		if (candidate instanceof AbstractProperty)			
			return pathElement.listProperties(false, true).contains(candidate);

		return false;
	}

	@Override
	protected boolean isValidPredicate(AbstractNodePredicate candidate) {
		if (candidate.getPredicateName() == ComparisonPredicateName.EQUAL_TO) {
			EqualToPredicate predicate = (EqualToPredicate)candidate;
			boolean found = false;

			for (AbstractProperty property : pathElement.listProperties(false, true)) {
				if (predicate.getLeftOperand() == property) {
					found = true;
					break;
				}

				if (predicate.getLeftOperand().getElementType() == PathElementType.SIMPLE_ATTRIBUTE && 
						property.getElementType() == PathElementType.COMPLEX_ATTRIBUTE &&
						((SimpleAttribute)predicate.getLeftOperand()).hasParentAttributeType() &&
						((SimpleAttribute)predicate.getLeftOperand()).getParentAttributeType() == ((ComplexAttribute)property).getType()) {
					found = true;
					break;
				}
			}

			if (found)				
				return predicate.getRightOperand().evalutesToSchemaType(predicate.getLeftOperand().getType());
		}

		else {
			BinaryLogicalPredicate predicate = (BinaryLogicalPredicate)candidate;
			if (isValidPredicate(predicate.getLeftOperand()))
				return isValidPredicate(predicate.getRightOperand());			
		}

		return false;
	}

}
