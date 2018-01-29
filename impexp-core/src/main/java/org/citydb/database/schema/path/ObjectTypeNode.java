package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.ObjectType;

public final class ObjectTypeNode extends AbstractTypeNode<ObjectType> {

	ObjectTypeNode(ObjectType objectType) {
		super(objectType);
	}
	
	ObjectTypeNode(ObjectTypeNode other) {
		super(other);
	}

	@Override
	protected ObjectTypeNode copy() {
		return new ObjectTypeNode(this);
	}

}
