/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

package org.citydb.core.operation.validator.reader.citygml;

import org.citydb.config.Config;
import org.citydb.config.project.global.LogLevel;
import org.citydb.util.log.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class ValidationErrorHandler implements ErrorHandler {
    private final Logger log = Logger.getInstance();
    private final Config config;

    private long validationErrors;
    private boolean isReportAllErrors;
    private boolean isAborted;
    private boolean hasFatalErrors;
    private String location;

    ValidationErrorHandler(Config config) {
        this.config = config;
        reset();
    }

    long getValidationErrors() {
        return validationErrors;
    }

    boolean isAborted() {
        return isAborted;
    }

    public void setAborted(boolean isAborted) {
        this.isAborted = isAborted;
    }

    public boolean hasFatalErrors() {
        return hasFatalErrors;
    }

    void reset() {
        validationErrors = 0;
        isReportAllErrors = !config.getImportConfig().getCityGMLOptions().getXMLValidation().isSetReportOneErrorPerFeature();
        isAborted = false;
        hasFatalErrors = false;
        location = null;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        report(e, "Warning", LogLevel.WARN);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        report(e, "Invalid content", LogLevel.ERROR);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        isReportAllErrors = false;
        hasFatalErrors = true;
        report(e, "Invalid content", LogLevel.ERROR);
    }

    private void report(SAXParseException e, String prefix, LogLevel level) throws SAXException {
        if (!isAborted) {
            String location = e.getLineNumber() + ", " + e.getColumnNumber();
            if (!location.equals(this.location)) {
                this.location = location;
                validationErrors++;

                log.log(level, prefix + " at " + '[' + location + "]: " + e.getMessage());

                if (!isReportAllErrors) {
                    isAborted = true;
                    throw new SAXException(e.getException());
                }
            }
        }
    }
}
