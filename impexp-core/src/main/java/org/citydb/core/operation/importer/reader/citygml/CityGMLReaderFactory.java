/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citydb.core.operation.importer.reader.citygml;

import org.citydb.config.Config;
import org.citydb.config.project.importer.CityGMLOptions;
import org.citydb.core.log.Logger;
import org.citydb.core.operation.importer.filter.CityGMLFilter;
import org.citydb.core.operation.importer.filter.selection.counter.CounterFilter;
import org.citydb.core.operation.importer.reader.FeatureReadException;
import org.citydb.core.operation.importer.reader.FeatureReader;
import org.citydb.core.operation.importer.reader.FeatureReaderFactory;
import org.citydb.core.registry.ObjectRegistry;
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
    public void initializeContext(CityGMLFilter filter, Config config) throws FeatureReadException {
        this.config = config;

        // prepare CityGML input factory
        try {
            factory = ObjectRegistry.getInstance().getCityGMLBuilder().createCityGMLInputFactory();
            factory.setProperty(CityGMLInputFactory.FEATURE_READ_MODE, FeatureReadMode.SPLIT_PER_COLLECTION_MEMBER);
            factory.setProperty(CityGMLInputFactory.FAIL_ON_MISSING_ADE_SCHEMA, false);
            factory.setProperty(CityGMLInputFactory.PARSE_SCHEMA, false);
            factory.setProperty(CityGMLInputFactory.SPLIT_AT_FEATURE_PROPERTY, new QName("generalizesTo"));
            factory.setProperty(CityGMLInputFactory.EXCLUDE_FROM_SPLITTING, new QName("CityModel"));
        } catch (CityGMLBuilderException e) {
            throw new FeatureReadException("Failed to initialize CityGML input factory.", e);
        }

        CityGMLOptions cityGMLOptions = config.getImportConfig().getCityGMLOptions();

        // prepare XML validation
        if (cityGMLOptions.getXMLValidation().isSetUseXMLValidation()) {
            log.info("Applying XML validation to CityGML input features.");

            factory.setProperty(CityGMLInputFactory.USE_VALIDATION, true);
            factory.setProperty(CityGMLInputFactory.PARSE_SCHEMA, true);

            validationHandler = new ValidationErrorHandler();
            validationHandler.setReportAllErrors(!cityGMLOptions.getXMLValidation().isSetReportOneErrorPerFeature());
            factory.setValidationEventHandler(validationHandler);
        }

        // build XSLT transformer chain
        if (cityGMLOptions.getXSLTransformation().isEnabled()
                && cityGMLOptions.getXSLTransformation().isSetStylesheets()) {
            try {
                log.info("Applying XSL transformations to CityGML input features.");

                List<String> stylesheets = cityGMLOptions.getXSLTransformation().getStylesheets();
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
                return config.getImportConfig().getAppearances().isSetImportAppearance();
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
