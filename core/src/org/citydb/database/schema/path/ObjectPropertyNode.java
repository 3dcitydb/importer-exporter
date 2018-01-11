package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.ObjectProperty;
import org.citydb.database.schema.mapping.ObjectType;
import org.citydb.database.schema.mapping.PathElementType;

public final class ObjectPropertyNode extends AbstractNode<ObjectProperty> {

	protected ObjectPropertyNode(ObjectProperty objectProperty) {
		super(objectProperty);
	}
	
	protected ObjectPropertyNode(ObjectPropertyNode other) {
		super(other);
	}
	
	@Override
	protected boolean isValidChild(AbstractPathElement candidate) {
		if (candidate.getElementType() == PathElementType.OBJECT_TYPE) {
			ObjectType type = (ObjectType)candidate;
			
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
	protected ObjectPropertyNode copy() {
		return new ObjectPropertyNode(this);
	}

}
