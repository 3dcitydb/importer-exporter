package org.citydb.config.project.importer;

import org.citydb.config.project.common.XSLTransformation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "CityGMLImportOptionsType", propOrder = {
        "importXalAddress",
        "xmlValidation",
        "xslTransformation"
})
public class CityGMLOptions {
    @XmlElement(defaultValue = "true")
    private boolean importXalAddress = true;
    private XMLValidation xmlValidation;
    private XSLTransformation xslTransformation;

    public CityGMLOptions() {
        xmlValidation = new XMLValidation();
        xslTransformation = new XSLTransformation();
    }

    public boolean isImportXalAddress() {
        return importXalAddress;
    }

    public void setImportXalAddress(boolean importXalAddress) {
        this.importXalAddress = importXalAddress;
    }

    public XMLValidation getXMLValidation() {
        return xmlValidation;
    }

    public void setXMLValidation(XMLValidation xmlValidation) {
        if (xmlValidation != null) {
            this.xmlValidation = xmlValidation;
        }
    }

    public XSLTransformation getXSLTransformation() {
        return xslTransformation;
    }

    public void setXSLTransformation(XSLTransformation xslTransformation) {
        if (xslTransformation != null) {
            this.xslTransformation = xslTransformation;
        }
    }
}
