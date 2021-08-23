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

package org.citydb.gui.map.geocoder.service;

import java.util.ArrayList;
import java.util.List;

public class GeocodingServiceException extends Exception {
    private final List<String> messages = new ArrayList<>();

    public GeocodingServiceException(String message) {
        this(message, null);
    }

    public GeocodingServiceException(Throwable cause) {
        this(null, cause);
    }

    public GeocodingServiceException(String message, Throwable cause) {
        super(message, cause);

        if (message != null && !message.trim().isEmpty())
            messages.add(message);

        while (cause != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage != null)
                messages.add(cause.getClass().getTypeName() + ": " + causeMessage);

            cause = cause.getCause();
        }
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    public List<String> getMessages() {
        return messages;
    }
}
