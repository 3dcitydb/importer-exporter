package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "condition")
public class Condition {
	@XmlAttribute(required = true)
	protected String column;
	@XmlAttribute(required = true)
	protected String value;
	@XmlAttribute(required = true)
	protected SimpleType type;
	
	protected Condition() {
	}
	
	public Condition(String column, String value, SimpleType type) {
		this.column = column;
		this.value = value;
		this.type = type;
	}

	public String getColumn() {
		return column;
	}

	public boolean isSetColumn() {
		return  column != null && !column.isEmpty();
	}
	
	public void setColumn(String column) {
		this.column = column;
	}

	public String getValue() {
		return value;
	}

	public boolean isSetValue() {
		return value != null && !value.isEmpty();
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public SimpleType getType() {
		return type;
	}

	public boolean isSetType() {
		return type != null;
	}
	
	public void setType(SimpleType type) {
		this.type = type;
	}

}
