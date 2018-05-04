package org.citydb.citygml.exporter.writer;

import org.citygml4j.model.gml.feature.AbstractFeature;

public interface FeatureWriter extends AutoCloseable {
	public void write(AbstractFeature feature) throws FeatureWriteException;
	public void close() throws FeatureWriteException;

	default boolean supportsFlatHierarchies() {
		return true;
	}
}
