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
package org.citydb.config.project.database;

public class DatabaseConfigurationException extends Exception {
    private final Reason reason;

    public DatabaseConfigurationException(Reason reason, Throwable cause) {
        super(reason.toString(), cause);
        this.reason = reason;
    }

    public DatabaseConfigurationException(Reason reason) {
        this(reason, null);
    }

    public DatabaseConfigurationException(String message, Throwable cause) {
        super(message, cause);
        reason = Reason.OTHER;
    }

    public DatabaseConfigurationException(String message) {
        this(message, null);
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        MISSING_HOSTNAME("Missing server hostname."),
        MISSING_USERNAME("Missing username."),
        MISSING_DB_NAME("Missing database name."),
        MISSING_PORT("Missing server port."),
        OTHER("Other configuration exception.");

        private final String message;

        Reason(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }
}
