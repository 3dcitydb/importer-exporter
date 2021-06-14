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

package org.citydb.operation.validator.reader.cityjson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import org.citydb.event.Event;
import org.citydb.event.EventDispatcher;
import org.citydb.event.EventHandler;
import org.citydb.event.global.EventType;
import org.citydb.file.InputFile;
import org.citydb.log.Logger;
import org.citydb.operation.validator.ValidationException;
import org.citydb.operation.validator.reader.Validator;
import org.citydb.registry.ObjectRegistry;
import org.citygml4j.cityjson.CityJSON;
import org.citygml4j.cityjson.CityJSONTypeAdapterFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CityJSONValidator implements Validator, EventHandler {
    private final Logger log = Logger.getInstance();
    private final EventDispatcher eventDispatcher;

    private InputStream inputStream;
    private volatile boolean isAborted;
    private boolean hasErrors;

    CityJSONValidator() {
        eventDispatcher = ObjectRegistry.getInstance().getEventDispatcher();
        eventDispatcher.addEventHandler(EventType.INTERRUPT, this);
    }

    @Override
    public long getValidationErrors() {
        return !isAborted && hasErrors ? 1 : 0;
    }

    @Override
    public void validate(InputFile inputFile) throws ValidationException {
        try {
            inputStream = inputFile.openStream();

            // we do not really validate the file against the CityJSON schema
            // but only check whether Gson can parse it...
            try (JsonReader reader = new JsonReader(new InputStreamReader(inputStream))) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapterFactory(new CityJSONTypeAdapterFactory())
                        .create();
                gson.fromJson(reader, CityJSON.class);
            }
        } catch (JsonParseException e) {
            if (!isAborted) {
                log.error("Invalid content: " + e.getMessage());
                hasErrors = true;
            }
        } catch (IOException e) {
            throw new ValidationException("Failed to validate CityJSON input file.", e);
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
        isAborted = true;

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                //
            }
        }
    }
}
