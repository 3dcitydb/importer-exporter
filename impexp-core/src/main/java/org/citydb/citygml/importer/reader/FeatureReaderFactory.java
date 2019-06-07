package org.citydb.citygml.importer.reader;

import org.citydb.citygml.importer.filter.CityGMLFilter;
import org.citydb.config.Config;

public interface FeatureReaderFactory {
    void initializeContext(CityGMLFilter filter, Config config) throws FeatureReadException;
    FeatureReader createFeatureReader() throws FeatureReadException;
}
