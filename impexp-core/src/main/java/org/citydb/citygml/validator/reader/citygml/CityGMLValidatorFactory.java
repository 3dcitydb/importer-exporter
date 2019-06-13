/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.gis.bgu.tum.de/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
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

package org.citydb.citygml.validator.reader.citygml;

import org.citydb.citygml.validator.ValidationException;
import org.citydb.citygml.validator.reader.Validator;
import org.citydb.citygml.validator.reader.ValidatorFactory;
import org.citydb.config.Config;
import org.citygml4j.xml.schema.SchemaHandler;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

public class CityGMLValidatorFactory implements ValidatorFactory {
    private javax.xml.validation.Validator validator;
    private ValidationErrorHandler validationHandler;

    @Override
    public void initializeContext(Config config) throws ValidationException {
        try {
            SchemaHandler schemaHandler = SchemaHandler.newInstance();
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(schemaHandler.getSchemaSources());
            validator = schema.newValidator();
        } catch (SAXException e) {
            throw new ValidationException("Failed to create CityGML schema context.", e);
        }

        validationHandler = new ValidationErrorHandler();
        validationHandler.setReportAllErrors(!config.getProject().getImporter().getXMLValidation().isSetReportOneErrorPerFeature());
    }

    @Override
    public Validator createValidator() throws ValidationException {
        return new CityGMLValidator(validator, validationHandler);
    }
}
