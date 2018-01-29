package org.citydb.query.filter.selection.expression;

import org.citydb.database.schema.mapping.AbstractPathElement;
import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.database.schema.path.InvalidSchemaPathException;
import org.citydb.database.schema.path.SchemaPath;

public class ValueReference implements Expression {
	private final SchemaPath schemaPath;
	
	public ValueReference(SchemaPath schemaPath) throws InvalidSchemaPathException {
		if (schemaPath == null)
			throw new InvalidSchemaPathException("The schema path may not be null.");
		
		PathElementType type = schemaPath.getLastNode().getPathElement().getElementType();
		if (type == PathElementType.FEATURE_TYPE)
			throw new InvalidSchemaPathException("The value reference may not end with a feature element.");
		
		this.schemaPath = schemaPath;
	}

	public SchemaPath getSchemaPath() {
		return schemaPath;
	}
	
	public AbstractPathElement getTarget() {
		return schemaPath.getLastNode().getPathElement();
	}
	
	@Override
	public ExpressionName getExpressionName() {
		return ExpressionName.VALUE_REFERENCE;
	}
	
}
