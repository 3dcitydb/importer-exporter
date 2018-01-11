package org.citydb.config.project.query.filter.projection;

import java.util.LinkedHashSet;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlType(name = "PropertyProjectionContextType")
public class ProjectionContext {
	@XmlAttribute(required = true)
	private QName typeName;
	@XmlAttribute(name="mode")
	private ProjectionMode mode;
	@XmlElements({
        @XmlElement(name = "propertyName", type = PropertyName.class),
        @XmlElement(name = "genericAttributeName", type = GenericAttributeName.class)
    })
	private LinkedHashSet<AbstractPropertyName> propertyNames;

	public QName getTypeName() {
		return typeName;
	}

	public void setTypeName(QName typeName) {
		this.typeName = typeName;
	}

	public ProjectionMode getMode() {
		return mode != null ? mode : ProjectionMode.KEEP;
	}

	public void setMode(ProjectionMode mode) {
		this.mode = mode;
	}

	public LinkedHashSet<AbstractPropertyName> getPropertyNames() {
		return propertyNames;
	}

	public void setPropertyNames(LinkedHashSet<AbstractPropertyName> propertyNames) {
		this.propertyNames = propertyNames;
	}
	
	public boolean isSetPropertyNames() {
		return propertyNames != null && !propertyNames.isEmpty();
	}

	@Override
	public final boolean equals(Object obj) {
		if (!(obj instanceof ProjectionContext))
			return false;
		
		return typeName != null && typeName.equals(((ProjectionContext)obj).typeName);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(typeName);
	}
	
}
