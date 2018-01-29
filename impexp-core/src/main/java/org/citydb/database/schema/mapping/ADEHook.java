package org.citydb.database.schema.mapping;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "adeHook")
public class ADEHook {
	@XmlAttribute(required = true)
    protected String name;
	
	protected ADEHook() {
	}
	
	public ADEHook(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public boolean isSetName() {
		return name != null && !name.isEmpty();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
}
