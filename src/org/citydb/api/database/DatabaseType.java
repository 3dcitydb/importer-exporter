package org.citydb.api.database;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="DatabaseTypeType")
@XmlEnum
public enum DatabaseType {
	@XmlEnumValue("Oracle")
	ORACLE("Oracle"),
	@XmlEnumValue("PostGIS")
	POSTGIS("PostgreSQL/PostGIS");

	private String value;

	private DatabaseType(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static DatabaseType fromValue(String v) {
		for (DatabaseType c : DatabaseType.values()) {
			if (c.value.toLowerCase().equals(v.toLowerCase())) {
				return c;
			}
		}

		return ORACLE;
	}

	public String toString() {
		return value;
	}
}
