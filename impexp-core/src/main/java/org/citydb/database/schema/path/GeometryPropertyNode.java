package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.GeometryProperty;

public final class GeometryPropertyNode extends AbstractNode<GeometryProperty> {

	protected GeometryPropertyNode(GeometryProperty geometryProperty) {
		super(geometryProperty);
	}
	
	protected GeometryPropertyNode(GeometryPropertyNode other) {
		super(other);
	}
	
	@Override
	protected boolean isValidChild(AbstractPathElement candidate) {
		return false;
	}

	@Override
	protected boolean isValidPredicate(AbstractNodePredicate candidate) {
		return false;
	}

	@Override
	protected GeometryPropertyNode copy() {
		return new GeometryPropertyNode(this);
	}
	
}
