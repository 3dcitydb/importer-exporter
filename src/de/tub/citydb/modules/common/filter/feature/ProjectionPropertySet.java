package de.tub.citydb.modules.common.filter.feature;

import java.util.LinkedHashSet;

import org.citygml4j.model.module.ModuleType;

public class ProjectionPropertySet extends LinkedHashSet<ProjectionProperty> {
	private static final long serialVersionUID = -5777546787052988491L;

	public boolean contains(ModuleType type, String name) {
		for (ProjectionProperty property : this)
			if (property.getModuleType() == type && property.getName().equals(name))
				return true;
		
		return false;
	}
}
