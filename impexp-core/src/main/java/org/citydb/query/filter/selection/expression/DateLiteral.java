package org.citydb.query.filter.selection.expression;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.citydb.database.schema.mapping.SimpleType;

import org.citydb.sqlbuilder.expression.PlaceHolder;

public class DateLiteral extends AbstractLiteral<Date> {
	private String xmlLiteral;
	
	public DateLiteral(Date value) {
		super(value);
	}
	
	public DateLiteral(Calendar calendar) {
		this(calendar.getTime());
	}
	
	public DateLiteral(GregorianCalendar calendar) {
		this(calendar.getTime());
	}

	public String getXMLLiteral() {
		return xmlLiteral;
	}

	public void setXMLLiteral(String xmlLiteral) {
		this.xmlLiteral = xmlLiteral;
	}

	@Override
	public boolean evalutesToSchemaType(SimpleType schemaType) {
		switch (schemaType) {
		case DATE:
			return true;
		default:
			return false;
		}
	}

	@Override
	public PlaceHolder<?> convertToSQLPlaceHolder() {
		return new PlaceHolder<>(new java.sql.Date(value.getTime()));
	}

	@Override
	public LiteralType getLiteralType() {
		return LiteralType.DATE;
	}
	
}
