package org.citydb.query.filter.selection.expression;

import org.citydb.database.schema.mapping.SimpleType;

import org.citydb.sqlbuilder.expression.PlaceHolder;

public class StringLiteral extends AbstractLiteral<String> {

	public StringLiteral(String value) {
		super(value);
	}

	@Override
	public boolean evalutesToSchemaType(SimpleType schemaType) {
		switch (schemaType) {
		case STRING:
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
		return LiteralType.STRING;
	}

}
