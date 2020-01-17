package org.citydb.citygml.importer.reader.cityjson;

import org.citydb.citygml.importer.filter.CityGMLFilter;
import org.citydb.citygml.importer.filter.selection.counter.CounterFilter;
import org.citydb.citygml.importer.reader.FeatureReadException;
import org.citydb.citygml.importer.reader.FeatureReader;
import org.citydb.citygml.importer.reader.FeatureReaderFactory;
import org.citydb.config.Config;
import org.citygml4j.CityGMLContext;
import org.citygml4j.builder.cityjson.CityJSONBuilder;
import org.citygml4j.builder.cityjson.CityJSONBuilderException;
import org.citygml4j.builder.cityjson.json.io.reader.CityJSONInputFactory;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;

public class CityJSONReaderFactory implements FeatureReaderFactory {
    private CityJSONInputFactory factory;
    private CityGMLInputFilter typeFilter;
    private CounterFilter counterFilter;

    @Override
    public void initializeContext(CityGMLFilter filter, Config config) throws FeatureReadException {
        CityJSONBuilder builder = CityGMLContext.getInstance().createCityJSONBuilder();
        try {
            factory = builder.createCityJSONInputFactory();
        } catch (CityJSONBuilderException e) {
            throw new FeatureReadException("Failed to initialize CityJSON input factory.", e);
        }

        // prepare feature filter
        typeFilter = name -> {
            Module module = Modules.getModule(name.getNamespaceURI());
            if (module != null && module.getType() == CityGMLModuleType.APPEARANCE && name.getLocalPart().equals("Appearance"))
                return config.getProject().getImporter().getAppearances().isSetImportAppearance();
            else
                return filter.getFeatureTypeFilter().isSatisfiedBy(name, true);
        };

        counterFilter = filter.getCounterFilter();
    }

    @Override
    public FeatureReader createFeatureReader() throws FeatureReadException {
        return new CityJSONReader(typeFilter, counterFilter, factory);
    }
}
