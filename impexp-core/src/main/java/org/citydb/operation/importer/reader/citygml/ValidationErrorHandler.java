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

package org.citydb.operation.importer.reader.citygml;

import org.citydb.config.project.global.LogLevel;
import org.citydb.log.Logger;

import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

class ValidationErrorHandler implements ValidationEventHandler {
    private final Logger log = Logger.getInstance();
    private long validationErrors;
    private boolean reportAllErrors;

    void setReportAllErrors(boolean reportAllErrors) {
        this.reportAllErrors = reportAllErrors;
    }

    long getValidationErrors() {
        return validationErrors;
    }

    void reset() {
        validationErrors = 0;
    }

    @Override
    public boolean handleEvent(ValidationEvent event) {
        StringBuilder msg = new StringBuilder();
        LogLevel type;

        switch (event.getSeverity()) {
            case ValidationEvent.FATAL_ERROR:
            case ValidationEvent.ERROR:
                msg.append("Invalid content");
                type = LogLevel.ERROR;
                break;
            case ValidationEvent.WARNING:
                msg.append("Warning");
                type = LogLevel.WARN;
                break;
            default:
                return reportAllErrors;
        }

        msg.append(": ").append(event.getMessage());
        log.log(type, msg.toString());

        validationErrors++;
        return reportAllErrors;
    }
}
