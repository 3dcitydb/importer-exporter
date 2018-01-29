package org.citydb.config.project.query.filter.projection;

import java.util.Objects;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;

@XmlType(name = "PropertyNameType")
public class PropertyName extends AbstractPropertyName {
	@XmlValue
	private QName name;

	public QName getName() {
		return name;
	}

	public void setName(QName name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;

		if (!(obj instanceof PropertyName))
			return false;

		PropertyName other = (PropertyName)obj;
		return Objects.equals(name, other.name);
	}
	
}
