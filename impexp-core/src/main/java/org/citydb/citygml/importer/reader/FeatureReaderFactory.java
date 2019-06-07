package org.citydb.citygml.importer.reader;

import org.citydb.citygml.importer.filter.CityGMLFilter;
import org.citydb.config.Config;
import org.citygml4j.builder.jaxb.CityGMLBuilder;

public interface FeatureReaderFactory {
    void initializeContext(CityGMLFilter filter, CityGMLBuilder cityGMLBuilder, Config config) throws FeatureReadException;
    FeatureReader createFeatureReader() throws FeatureReadException;
}
