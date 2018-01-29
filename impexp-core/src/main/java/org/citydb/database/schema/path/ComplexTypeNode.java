package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.ComplexType;

public final class ComplexTypeNode extends AbstractTypeNode<ComplexType> {

	ComplexTypeNode(ComplexType complexType) {
		super(complexType);
	}
	
	ComplexTypeNode(ComplexTypeNode other) {
		super(other);
	}

	@Override
	protected ComplexTypeNode copy() {
		return new ComplexTypeNode(this);
	}

}
