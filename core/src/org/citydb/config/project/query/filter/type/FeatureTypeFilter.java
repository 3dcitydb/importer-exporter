package org.citydb.config.project.query.filter.type;

import java.util.Collection;
import java.util.LinkedHashSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

@XmlType(name="FeatureTypeFilterType", propOrder={
		"typeNames"
})
public class FeatureTypeFilter {
	@XmlElement(name="typeName", required = true)
	private LinkedHashSet<QName> typeNames;
	
	public FeatureTypeFilter() {
		typeNames = new LinkedHashSet<>();
	}
	
	public void addTypeName(QName typeName) {
		typeNames.add(typeName);
	}
	
	public boolean containsTypeName(QName typeName) {
		return typeNames.contains(typeName);
	}

	public LinkedHashSet<QName> getTypeNames() {
		return typeNames;
	}

	public void setTypeNames(Collection<QName> typeNames) {
		this.typeNames = new LinkedHashSet<>(typeNames);
	}
	
	public boolean isEmpty() {
		return typeNames.isEmpty();
	}
	
	public void reset() {
		typeNames.clear();
	}
	
}