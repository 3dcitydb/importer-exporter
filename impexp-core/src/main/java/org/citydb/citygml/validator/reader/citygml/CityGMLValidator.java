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
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.file.InputFile;
import org.citydb.registry.ObjectRegistry;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;

public class CityGMLValidator implements Validator, EventHandler {
    private final javax.xml.validation.Validator validator;
    private final ValidationErrorHandler validationHandler;
    private final EventDispatcher eventDispatcher;

    private InputStream inputStream;

    CityGMLValidator(javax.xml.validation.Validator validator, ValidationErrorHandler validationHandler) {
        this.validator = validator;
        this.validationHandler = validationHandler;

        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
        eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
    }

    @Override
    public long getValidationErrors() {
        return validationHandler.getValidationErrors();
    }

    @Override
    public void validate(InputFile inputFile) throws ValidationException {
        try {
            validator.reset();
            validationHandler.reset();
            validator.setErrorHandler(validationHandler);

            inputStream = inputFile.openStream();
            validator.validate(new StreamSource(inputStream));
        } catch (IOException | SAXException e) {
            if (!validationHandler.isAborted())
                throw new ValidationException("Failed to validate CityGML input file.", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

    @Override
    public void close() throws ValidationException {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                //
            }
        }

        eventDispatcher.removeEventHandler(this);
    }

    @Override
    public void handleEvent(Event event) throws Exception {
        validationHandler.setAborted(true);

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                //
            }
        }
    }
}
