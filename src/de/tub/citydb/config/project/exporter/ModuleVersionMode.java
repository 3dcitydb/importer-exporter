package de.tub.citydb.config.project.exporter;

import org.citygml4j.model.citygml.CityGMLModule;

public interface ModuleVersionMode<T extends CityGMLModule> {
	public T getModule();
}
