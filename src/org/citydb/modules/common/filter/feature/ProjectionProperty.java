package org.citydb.modules.common.filter.feature;

import org.citygml4j.model.module.ModuleType;

public class ProjectionProperty {
	private final String name;
	private final ModuleType moduleType;

	public ProjectionProperty(ModuleType moduleType, String name) {
		this.moduleType = moduleType;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public ModuleType getModuleType() {
		return moduleType;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof ProjectionProperty))
			return false;

		if (obj == this)
			return true;
		
		ProjectionProperty property = (ProjectionProperty)obj;
		return moduleType.equals(property.moduleType) && name.equals(property.name);
	}

	@Override
	public int hashCode() {
		return moduleType.hashCode() ^ name.hashCode();
	}

}
