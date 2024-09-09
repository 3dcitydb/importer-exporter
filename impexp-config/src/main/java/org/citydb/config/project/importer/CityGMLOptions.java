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

package org.citydb.config.project.importer;

import org.citydb.config.project.common.XSLTransformation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "CityGMLImportOptionsType", propOrder = {})
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
