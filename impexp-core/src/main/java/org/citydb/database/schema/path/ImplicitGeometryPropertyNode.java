package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.ImplicitGeometryProperty;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.ObjectType;
import org.citydb.database.schema.mapping.PathElementType;

public final class ImplicitGeometryPropertyNode extends AbstractNode<ImplicitGeometryProperty> {

	protected ImplicitGeometryPropertyNode(ImplicitGeometryProperty implicitGeometryProperty) {
		super(implicitGeometryProperty);
	}
	
	protected ImplicitGeometryPropertyNode(ImplicitGeometryPropertyNode other) {
		super(other);
	}
	
	@Override
	protected boolean isValidChild(AbstractPathElement candidate) {
		if (candidate.getElementType() == PathElementType.OBJECT_TYPE) {
			ObjectType type = (ObjectType)candidate;
			if (type.getPath() == MappingConstants.IMPLICIT_GEOMETRY_PATH)
				return true;
		}

		return false;
	}

	@Override
	protected boolean isValidPredicate(AbstractNodePredicate candidate) {
		return false;
	}

	@Override
	protected ImplicitGeometryPropertyNode copy() {
		return new ImplicitGeometryPropertyNode(this);
	}
	
}
