package org.citydb.config.project.global;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="APIKeysType", propOrder={
        "googleGeocoding",
        "googleElevation"
})
public class APIKeys {
    private String googleGeocoding = "";
    private String googleElevation = "";

    public boolean isSetGoogleGeocoding() {
        return googleGeocoding != null && !googleGeocoding.trim().isEmpty();
    }

    public String getGoogleGeocoding() {
        return googleGeocoding;
    }

    public void setGoogleGeocoding(String googleGeocoding) {
        this.googleGeocoding = googleGeocoding;
    }

    public boolean isSetGoogleElevation() {
        return googleElevation != null && !googleElevation.trim().isEmpty();
    }

    public String getGoogleElevation() {
        return googleElevation;
    }

    public void setGoogleElevation(String googleElevation) {
        this.googleElevation = googleElevation;
    }
}
