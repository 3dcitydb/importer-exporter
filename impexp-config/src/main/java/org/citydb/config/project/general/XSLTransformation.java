package org.citydb.config.project.general;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlType(name="XSLTransformationType", propOrder={
        "stylesheets"
})
public class XSLTransformation {
    @XmlAttribute(required=true)
    private boolean isEnabled = false;
    @XmlElement(name="stylesheet")
    private List<String> stylesheets;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isSetStylesheets() {
        return stylesheets != null && !stylesheets.isEmpty();
    }

    public List<String> getStylesheets() {
        return stylesheets;
    }

    public void addStylesheet(String stylesheet) {
        if (stylesheets == null)
            stylesheets = new ArrayList<>();

        stylesheets.add(stylesheet);
    }

    public void setStylesheets(List<String> stylesheets) {
        this.stylesheets = stylesheets;
    }
}
