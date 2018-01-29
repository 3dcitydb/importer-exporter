package org.citydb.citygml.exporter.writer;

import java.io.Writer;

public interface FeatureWriterFactory {
	public FeatureWriter createFeatureWriter(Writer writer) throws FeatureWriteException;
}
