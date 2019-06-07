package org.citydb.citygml.importer.reader;

import org.apache.tika.mime.MediaType;
import org.citydb.citygml.importer.filter.CityGMLFilter;
import org.citydb.citygml.importer.reader.citygml.CityGMLReaderFactory;
import org.citydb.config.Config;
import org.citydb.file.InputFile;
import org.citygml4j.builder.jaxb.CityGMLBuilder;

import java.util.HashMap;
import java.util.Map;

public class FeatureReaderFactoryBuilder {
    private Map<MediaType, FeatureReaderFactory> factories = new HashMap<>();

    public FeatureReaderFactory buildFactory(InputFile file, CityGMLFilter filter, CityGMLBuilder cityGMLBuilder, Config config) throws FeatureReadException {
        FeatureReaderFactory factory = factories.get(file.getMediaType());
        if (factory == null) {
            if (file.getMediaType() == MediaType.APPLICATION_XML)
                factory = new CityGMLReaderFactory();

            if (factory == null)
                throw new FeatureReadException("No reader available for media type '" + file.getMediaType() + "'.");

            factory.initializeContext(filter, cityGMLBuilder, config);
            factories.put(file.getMediaType(), factory);
        }

        return factory;
    }

}
