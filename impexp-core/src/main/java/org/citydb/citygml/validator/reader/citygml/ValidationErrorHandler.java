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

import org.citydb.config.Config;
import org.citydb.config.project.global.LogLevel;
import org.citydb.log.Logger;
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

    ValidationErrorHandler(Config config) {
        this.config = config;
        reset();
    }

    void setReportAllErrors(boolean reportAllErrors) {
        this.isReportAllErrors = reportAllErrors;
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
        isReportAllErrors = !config.getProject().getImportConfig().getXMLValidation().isSetReportOneErrorPerFeature();
        isAborted = false;
        hasFatalErrors = false;
    }

    @Override
    public void warning(SAXParseException e) throws SAXException {
        write(e, "Warning", LogLevel.WARN);
    }

    @Override
    public void error(SAXParseException e) throws SAXException {
        write(e, "Invalid content", LogLevel.ERROR);
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        isReportAllErrors = false;
        hasFatalErrors = true;
        write(e, "Invalid content", LogLevel.ERROR);
    }

    private void write(SAXParseException e, String prefix, LogLevel level) throws SAXException {
        if (!isAborted) {
            log.log(level, prefix + " at " + '[' + e.getLineNumber() + ',' + e.getColumnNumber() + "]: " + e.getMessage());
            validationErrors++;
            if (!isReportAllErrors) {
                isAborted = true;
                throw new SAXException(e.getException());
            }
        }
    }
}
