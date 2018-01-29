package org.citydb.query.filter.selection.expression;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.citydb.database.schema.mapping.SimpleType;

import org.citydb.sqlbuilder.expression.PlaceHolder;

public class TimestampLiteral extends AbstractLiteral<Date> {
	private String xmlLiteral;
	private boolean isDate;
	
	public TimestampLiteral(Date value) {
		super(value);
	}
	
	public TimestampLiteral(Calendar calendar) {
		this(calendar.getTime());
	}
	
	public TimestampLiteral(GregorianCalendar calendar) {
		this(calendar.getTime());
	}
	
	public String getXMLLiteral() {
		return xmlLiteral;
	}

	public void setXMLLiteral(String xmlLiteral) {
		this.xmlLiteral = xmlLiteral;
	}

	public boolean isDate() {
		return isDate;
	}

	public void setDate(boolean isDate) {
		this.isDate = isDate;
	}

	@Override
	public boolean evalutesToSchemaType(SimpleType schemaType) {
		switch (schemaType) {
		case TIMESTAMP:
			return true;
		default:
			return false;
		}
	}

	@Override
	public PlaceHolder<?> convertToSQLPlaceHolder() {
		return !isDate ? new PlaceHolder<>(new Timestamp(value.getTime())) : new PlaceHolder<>(new java.sql.Date(value.getTime()));
	}

	@Override
	public LiteralType getLiteralType() {
		return LiteralType.TIMESTAMP;
	}
	
}
