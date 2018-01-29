package org.citydb.query.filter.projection;

import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import org.citydb.database.schema.mapping.AppSchema;
import org.citygml4j.model.citygml.CityGMLClass;

public class CombinedProjectionFilter {
	private final List<ProjectionFilter> filters;
	
	public CombinedProjectionFilter(List<ProjectionFilter> filters) {
		this.filters = filters;
	}
	
	public CombinedProjectionFilter(ProjectionFilter... filters) {
		this.filters = Arrays.asList(filters);
	}
	
	public boolean containsProperty(String name, String namespaceURI) {
		for (ProjectionFilter filter : filters) {
			if (filter.containsProperty(name, namespaceURI))
				return true;
		}
		
		return false;
	}

	public boolean containsProperty(QName name) {
		for (ProjectionFilter filter : filters) {
			if (filter.containsProperty(name))
				return true;
		}
		
		return false;
	}

	public boolean containsProperty(String name, AppSchema schema) {
		for (ProjectionFilter filter : filters) {
			if (filter.containsProperty(name, schema))
				return true;
		}
		
		return false;
	}

	public boolean containsGenericAttribute(GenericAttribute genericAttribute) {
		for (ProjectionFilter filter : filters) {
			if (filter.containsGenericAttribute(genericAttribute))
				return true;
		}
		
		return false;
	}

	public boolean containsGenericAttribute(String name, CityGMLClass type) {
		for (ProjectionFilter filter : filters) {
			if (filter.containsGenericAttribute(name, type))
				return true;
		}
		
		return false;
	}
	
}
