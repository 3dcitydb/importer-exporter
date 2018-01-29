package org.citydb.database.schema.path;

import org.citydb.database.schema.mapping.FeatureType;

public final class FeatureTypeNode extends AbstractTypeNode<FeatureType> {
	private boolean useSchemaElement;

	FeatureTypeNode(FeatureType featureType) {
		super(featureType);
	}
	
	FeatureTypeNode(FeatureTypeNode other) {
		super(other);
		this.useSchemaElement = other.useSchemaElement;
	}

	public boolean isUseSchemaElement() {
		return useSchemaElement;
	}

	public void setUseSchemaElement(boolean useSchemaElement) {
		this.useSchemaElement = useSchemaElement;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		if (useSchemaElement)
			builder.append("schema-element(").append(super.toString()).append(")");
		else
			builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected FeatureTypeNode copy() {
		return new FeatureTypeNode(this);
	}
	
}
