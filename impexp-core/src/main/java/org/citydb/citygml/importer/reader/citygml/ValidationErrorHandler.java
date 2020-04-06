package org.citydb.citygml.importer.reader.citygml;

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
