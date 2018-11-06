package org.citydb.gui.components.mapviewer.geocoder.service;

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
