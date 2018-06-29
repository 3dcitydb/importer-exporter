package org.citydb.citygml.exporter.writer;

import org.citydb.config.Config;
import org.citydb.query.Query;

public class FeatureWriterFactoryBuilder {

	public static FeatureWriterFactory buildFactory(Query query, Config config) throws FeatureWriteException {
		return new CityGMLWriterFactory(query, config);
	}
	
}
