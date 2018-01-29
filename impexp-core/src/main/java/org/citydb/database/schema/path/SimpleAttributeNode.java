package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.SimpleAttribute;
import org.citydb.database.schema.path.predicate.comparison.ComparisonPredicateName;
import org.citydb.database.schema.path.predicate.comparison.EqualToPredicate;
import org.citydb.database.schema.path.predicate.logical.BinaryLogicalPredicate;

public final class SimpleAttributeNode extends AbstractNode<SimpleAttribute> {

	protected SimpleAttributeNode(SimpleAttribute attribute) {
		super(attribute);
	}
	
	protected SimpleAttributeNode(SimpleAttributeNode other) {
		super(other);
	}

	@Override
	protected boolean isValidChild(AbstractPathElement candidate) {
		return false;
	}

	@Override
	protected boolean isValidPredicate(AbstractNodePredicate candidate) {
		if (candidate.getPredicateName() == ComparisonPredicateName.EQUAL_TO)
			return ((EqualToPredicate)candidate).getLeftOperand() == this.pathElement;

		else {
			BinaryLogicalPredicate predicate = (BinaryLogicalPredicate)candidate;
			if (isValidPredicate(predicate.getLeftOperand()))
				return isValidPredicate(predicate.getRightOperand());			
		}

		return false;
	}

	@Override
	protected SimpleAttributeNode copy() {
		return new SimpleAttributeNode(this);
	}

	public String toString(boolean removeAttributePrefixes) {
		StringBuilder builder = new StringBuilder();

		String name = pathElement.getName();
		boolean usePrefix = true;

		if (pathElement.getPath().startsWith("@")) {
			builder.append("@");

			if (removeAttributePrefixes 
					&& parent != null 
					&& parent.getPathElement().getSchema() == pathElement.getSchema()
					&& !name.equals("id"))
				usePrefix = false;
		}

		if (usePrefix)
			builder.append(pathElement.getSchema().isSetXMLPrefix() ? pathElement.getSchema().getXMLPrefix() : pathElement.getSchema().getId()).append(":");

		return builder.append(name).toString();
	}

	@Override
	public String toString() {
		return toString(false);
	}
}
