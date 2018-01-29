package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlEnum
@XmlType(name = "tableRole")
public enum TableRole {

	@XmlEnumValue("parent")
    PARENT("parent"),
	@XmlEnumValue("child")
    CHILD("child");
    
    private final String value;

    TableRole(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

	public static TableRole fromValue(String v) {
        for (TableRole c: TableRole.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
	
	@Override
	public String toString() {
		return value;
	}


}
