package org.citydb.config.project.importer;

import org.citydb.config.project.common.XSLTransformation;

import javax.xml.bind.annotation.XmlType;

@XmlType(name = "CityGMLImportOptionsType", propOrder = {
        "address",
        "xmlValidation",
        "xslTransformation"
})
public class CityGMLOptions {
    private ImportAddress address;
    private XMLValidation xmlValidation;
    private XSLTransformation xslTransformation;

    public CityGMLOptions() {
        address = new ImportAddress();
        xmlValidation = new XMLValidation();
        xslTransformation = new XSLTransformation();
    }

    public ImportAddress getAddress() {
        return address;
    }

    public void setAddress(ImportAddress address) {
        if (address != null) {
            this.address = address;
        }
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
