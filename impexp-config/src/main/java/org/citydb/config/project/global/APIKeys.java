package org.citydb.config.project.global;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="APIKeysType", propOrder={
        "googleGeocoding",
        "googleMapsElevation"
})
public class APIKeys {
    private String googleGeocoding = "";
    private String googleMapsElevation = "";

    public boolean isSetGoogleGeocoding() {
        return googleGeocoding != null && !googleGeocoding.trim().isEmpty();
    }

    public String getGoogleGeocoding() {
        return googleGeocoding;
    }

    public void setGoogleGeocoding(String googleGeocoding) {
        this.googleGeocoding = googleGeocoding;
    }

    public boolean isSetGoogleMapsElevation() {
        return googleMapsElevation != null && !googleMapsElevation.trim().isEmpty();
    }

    public String getGoogleMapsElevation() {
        return googleMapsElevation;
    }

    public void setGoogleMapsElevation(String googleMapsElevation) {
        this.googleMapsElevation = googleMapsElevation;
    }
}
