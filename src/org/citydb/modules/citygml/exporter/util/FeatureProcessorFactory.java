package org.citydb.modules.citygml.exporter.util;

public interface FeatureProcessorFactory<T extends FeatureProcessor> {
	public T createFeatureProcessor();
}
