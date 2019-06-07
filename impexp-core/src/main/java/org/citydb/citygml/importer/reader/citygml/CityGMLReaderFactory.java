package org.citydb.citygml.importer.reader.citygml;

import org.citydb.citygml.importer.filter.CityGMLFilter;
import org.citydb.citygml.importer.reader.FeatureReadException;
import org.citydb.citygml.importer.reader.FeatureReader;
import org.citydb.citygml.importer.reader.FeatureReaderFactory;
import org.citydb.config.Config;
import org.citydb.config.project.importer.XMLValidation;
import org.citydb.config.project.query.filter.counter.CounterFilter;
import org.citydb.log.Logger;
import org.citygml4j.builder.jaxb.CityGMLBuilder;
import org.citygml4j.builder.jaxb.CityGMLBuilderException;
import org.citygml4j.model.module.Module;
import org.citygml4j.model.module.Modules;
import org.citygml4j.model.module.citygml.CityGMLModuleType;
import org.citygml4j.xml.io.CityGMLInputFactory;
import org.citygml4j.xml.io.reader.CityGMLInputFilter;
import org.citygml4j.xml.io.reader.FeatureReadMode;
import org.citygml4j.xml.io.writer.CityGMLWriteException;

import javax.xml.namespace.QName;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.List;

public class CityGMLReaderFactory implements FeatureReaderFactory {
    private final Logger log = Logger.getInstance();

    private CityGMLInputFactory factory;
    private CityGMLInputFilter typeFilter;
    private CounterFilter counterFilter;
    private ValidationErrorHandler validationHandler;
    private Config config;

    @Override
    public void initializeContext(CityGMLFilter filter, CityGMLBuilder cityGMLBuilder, Config config) throws FeatureReadException {
        this.config = config;

        // prepare CityGML input factory
        try {
            factory = cityGMLBuilder.createCityGMLInputFactory();
            factory.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_COLLECTION_MEMBER);
            factory.setProperty(CityGMLInputFactory.FAIL_ON_MISSING_ADE_SCHEMA, false);
            factory.setProperty(CityGMLInputFactory.PARSE_SCHEMA, false);
            factory.setProperty(CityGMLInputFactory.SPLIT_AT_FEATURE_PROPERTY, new QName("generalizesTo"));
            factory.setProperty(CityGMLInputFactory.EXCLUDE_FROM_SPLITTING, new QName("CityModel"));
        } catch (CityGMLBuilderException e) {
            throw new FeatureReadException("Failed to initialize CityGML input factory.", e);
        }

        // prepare XML validation
        XMLValidation xmlValidation = config.getProject().getImporter().getXMLValidation();
        if (xmlValidation.isSetUseXMLValidation()) {
            log.info("Applying XML validation to CityGML input features.");

            factory.setProperty(CityGMLInputFactory.USE_VALIDATION, true);
            factory.setProperty(CityGMLInputFactory.PARSE_SCHEMA, true);

            validationHandler = new ValidationErrorHandler();
            validationHandler.setReportAllErrors(!xmlValidation.isSetReportOneErrorPerFeature());
            factory.setValidationEventHandler(validationHandler);
        }

        // build XSLT transformer chain
        if (config.getProject().getImporter().getXSLTransformation().isEnabled()
                && config.getProject().getImporter().getXSLTransformation().isSetStylesheets()) {
            try {
                log.info("Applying XSL transformations to CityGML input features.");

                List<String> stylesheets = config.getProject().getImporter().getXSLTransformation().getStylesheets();
                SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
                Templates[] templates = new Templates[stylesheets.size()];

                for (int i = 0; i < stylesheets.size(); i++) {
                    Templates template = factory.newTemplates(new StreamSource(new File(stylesheets.get(i))));
                    templates[i] = template;
                }

                this.factory.setTransformationTemplates(templates);
            } catch (CityGMLWriteException | TransformerConfigurationException e) {
                throw new FeatureReadException("Failed to configure the XSL transformation.", e);
            }
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
        return new CityGMLReader(typeFilter, counterFilter, validationHandler, factory, config);
    }
}
