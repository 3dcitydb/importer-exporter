package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.ComplexProperty;
import org.citydb.database.schema.mapping.ComplexType;
import org.citydb.database.schema.mapping.PathElementType;

public final class ComplexPropertyNode extends AbstractNode<ComplexProperty> {

	protected ComplexPropertyNode(ComplexProperty complexProperty) {
		super(complexProperty);
	}
	
	protected ComplexPropertyNode(ComplexPropertyNode other) {
		super(other);
	}
	
	@Override
	protected boolean isValidChild(AbstractPathElement candidate) {
		if (candidate.getElementType() == PathElementType.COMPLEX_TYPE) {
			ComplexType type = (ComplexType)candidate;
			
			while (type != null) {
				if (pathElement.getType() == type)
					return true;
				
				type = type.isSetExtension() ? type.getExtension().getBase() : null;
			}
		}

		return false;
	}

	@Override
	protected boolean isValidPredicate(AbstractNodePredicate candidate) {
		return false;
	}

	@Override
	protected ComplexPropertyNode copy() {
		return new ComplexPropertyNode(this);
	}

}
