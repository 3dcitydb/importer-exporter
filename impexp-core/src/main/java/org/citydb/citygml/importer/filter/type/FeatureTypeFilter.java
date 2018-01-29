package org.citydb.citygml.importer.filter.type;

import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.citydb.query.filter.FilterException;

public class FeatureTypeFilter {
	private final Set<QName> typeNames;
	
	public FeatureTypeFilter() {
		typeNames = new HashSet<>();
	}
	
	public FeatureTypeFilter(org.citydb.config.project.query.filter.type.FeatureTypeFilter typeFilter) throws FilterException {
		if (typeFilter == null)
			throw new FilterException("The feature type filter must not be null.");
		
		typeNames = typeFilter.getTypeNames();
	}
	
	public boolean isSatisfiedBy(QName name) {
		return typeNames.isEmpty() || typeNames.contains(name);
	}
	
}
