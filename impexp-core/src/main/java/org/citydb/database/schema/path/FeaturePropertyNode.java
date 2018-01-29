package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.FeatureProperty;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.PathElementType;

public final class FeaturePropertyNode extends AbstractNode<FeatureProperty> {

	protected FeaturePropertyNode(FeatureProperty featureProperty) {
		super(featureProperty);
	}
	
	protected FeaturePropertyNode(FeaturePropertyNode other) {
		super(other);
	}
	
	@Override
	protected boolean isValidChild(AbstractPathElement candidate) {
		if (candidate.getElementType() == PathElementType.FEATURE_TYPE) {
			FeatureType type = (FeatureType)candidate;
			
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
	protected FeaturePropertyNode copy() {
		return new FeaturePropertyNode(this);
	}
	
}
