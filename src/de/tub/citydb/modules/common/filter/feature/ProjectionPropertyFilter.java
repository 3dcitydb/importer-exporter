package de.tub.citydb.modules.common.filter.feature;

import org.citygml4j.model.module.ModuleType;

public class ProjectionPropertyFilter {
	private ProjectionPropertySet propertySet;
	
	public ProjectionPropertyFilter(ProjectionPropertySet propertySet) {
		this.propertySet = propertySet;
	}
	
	public boolean filter(ModuleType module, String propertyName) {
		return !pass(module, propertyName);
	}
	
	public boolean pass(ModuleType module, String propertyName) {
		return propertySet == null || propertySet.contains(module, propertyName);
	}
	
	public void combine(ProjectionPropertyFilter projectionPropertyFilter) {
		if (projectionPropertyFilter.propertySet != null) {
			if (propertySet != null)
				propertySet.addAll(projectionPropertyFilter.propertySet);
			else
				propertySet = projectionPropertyFilter.propertySet;
		}
	}
	
}
