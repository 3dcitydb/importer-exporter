package org.citydb.citygml.importer.reader;

import org.apache.tika.mime.MediaType;
import org.citydb.citygml.importer.filter.CityGMLFilter;
import org.citydb.citygml.importer.reader.citygml.CityGMLReaderFactory;
import org.citydb.citygml.importer.reader.cityjson.CityJSONReaderFactory;
import org.citydb.config.Config;
import org.citydb.file.InputFile;

import java.util.HashMap;
import java.util.Map;

public class FeatureReaderFactoryBuilder {
    private Map<MediaType, FeatureReaderFactory> factories = new HashMap<>();

    public FeatureReaderFactory buildFactory(InputFile file, CityGMLFilter filter, Config config) throws FeatureReadException {
        FeatureReaderFactory factory = factories.get(file.getMediaType());
        if (factory == null) {
            if (file.getMediaType().equals(InputFile.APPLICATION_XML))
                factory = new CityGMLReaderFactory();
            else if (file.getMediaType().equals(InputFile.APPLICATION_JSON))
                factory = new CityJSONReaderFactory();

            if (factory == null)
                throw new FeatureReadException("No reader available for media type '" + file.getMediaType() + "'.");

            factory.initializeContext(filter, config);
            factories.put(file.getMediaType(), factory);
        }

        return factory;
    }

}
