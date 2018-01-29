package org.citydb.query.filter.selection.expression;

import org.citydb.database.schema.mapping.SimpleType;

import org.citydb.sqlbuilder.expression.PlaceHolder;

public abstract class AbstractLiteral<T> implements Expression {
	protected T value;
	
	public AbstractLiteral(T value) {
		this.value = value;
	}
	
	public abstract LiteralType getLiteralType();
	public abstract boolean evalutesToSchemaType(SimpleType schemaType);
	public abstract PlaceHolder<?> convertToSQLPlaceHolder();
	
	public T getValue() {
		return value;
	}
	
	@Override
	public ExpressionName getExpressionName() {
		return ExpressionName.LITERAL;
	}
	
}
