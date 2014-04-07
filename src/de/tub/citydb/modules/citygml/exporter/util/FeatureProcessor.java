package de.tub.citydb.modules.citygml.exporter.util;

import org.citygml4j.model.gml.feature.AbstractFeature;

public interface FeatureProcessor {
	public void process(AbstractFeature abstractFeature) throws FeatureProcessException;
}
