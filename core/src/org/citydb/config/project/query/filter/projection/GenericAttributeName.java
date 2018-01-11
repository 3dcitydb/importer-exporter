package org.citydb.config.project.query.filter.projection;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlType(name = "GenericAttributeNameType")
public class GenericAttributeName extends AbstractPropertyName {
	@XmlAttribute
	private GenericAttributeType type;
	@XmlValue
	private String name;

	public GenericAttributeType getType() {
		return type;
	}

	public void setType(GenericAttributeType type) {
		this.type = type;
	}
	
	public boolean isSetType() {
		return type != null;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof GenericAttributeName))
			return false;

		GenericAttributeName other = (GenericAttributeName)obj;
		return name.equals(other.name) && type == other.type;
	}
	
}
