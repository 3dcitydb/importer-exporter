package org.citydb.query.filter.selection.expression;

import org.citydb.database.schema.mapping.SimpleType;

import org.citydb.sqlbuilder.expression.PlaceHolder;

public class IntegerLiteral extends AbstractLiteral<Integer> {
	
	public IntegerLiteral(int value) {
		super(value);
	}
	
	@Override
	public boolean evalutesToSchemaType(SimpleType schemaType) {
		switch (schemaType) {
		case INTEGER:
			return true;
		default:
			return false;
		}
	}

	@Override
	public PlaceHolder<?> convertToSQLPlaceHolder() {
		return new PlaceHolder<>(value);
	}

	@Override
	public LiteralType getLiteralType() {
		return LiteralType.INTEGER;
	}
	
}
