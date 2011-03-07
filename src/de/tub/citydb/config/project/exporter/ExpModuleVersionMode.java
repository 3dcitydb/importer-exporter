package de.tub.citydb.config.project.exporter;

import de.tub.citygml4j.model.citygml.CityGMLModule;

public interface ExpModuleVersionMode<T extends CityGMLModule> {
	public T getModule();
}
