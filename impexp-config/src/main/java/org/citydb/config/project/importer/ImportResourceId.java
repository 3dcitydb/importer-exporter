/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "ImportResourceIdType", propOrder = {
        "uuidMode",
        "idPrefix",
        "keepIdAsExternalReference",
        "codeSpaceMode",
        "codeSpace"
})
public class ImportResourceId {
    @XmlElement(required = true)
    private UUIDMode uuidMode = UUIDMode.COMPLEMENT;
    @XmlElement(defaultValue = "ID_")
    private String idPrefix = "ID_";
    @XmlElement(defaultValue = "true")
    private Boolean keepIdAsExternalReference = true;
    @XmlElement(required = true)
    private CodeSpaceMode codeSpaceMode = CodeSpaceMode.NONE;
    @XmlElement(defaultValue = "ID")
    private String codeSpace = "ID";

    public ImportResourceId() {
    }

    public boolean isUUIDModeReplace() {
        return uuidMode == UUIDMode.REPLACE;
    }

    public boolean isUUIDModeComplement() {
        return uuidMode == UUIDMode.COMPLEMENT;
    }

    public UUIDMode getUuidMode() {
        return uuidMode;
    }

    public void setUuidMode(UUIDMode uuidMode) {
        this.uuidMode = uuidMode;
    }

    public String getIdPrefix() {
        return idPrefix;
    }

    public void setIdPrefix(String idPrefix) {
        this.idPrefix = idPrefix;
    }

    public boolean isSetKeepIdAsExternalReference() {
        return keepIdAsExternalReference != null ? keepIdAsExternalReference : false;
    }

    public Boolean getKeepIdAsExternalReference() {
        return keepIdAsExternalReference;
    }

    public void setKeepIdAsExternalReference(Boolean keepIdAsExternalReference) {
        this.keepIdAsExternalReference = keepIdAsExternalReference;
    }

    public boolean isSetNoneCodeSpaceMode() {
        return codeSpaceMode == CodeSpaceMode.NONE;
    }

    public boolean isSetRelativeCodeSpaceMode() {
        return codeSpaceMode == CodeSpaceMode.RELATIVE;
    }

    public boolean isSetAbsoluteCodeSpaceMode() {
        return codeSpaceMode == CodeSpaceMode.ABSOLUTE;
    }

    public boolean isSetUserCodeSpaceMode() {
        return codeSpaceMode == CodeSpaceMode.USER;
    }

    public CodeSpaceMode getCodeSpaceMode() {
        return codeSpaceMode;
    }

    public void setCodeSpaceMode(CodeSpaceMode codeSpaceMode) {
        this.codeSpaceMode = codeSpaceMode;
    }

    public String getCodeSpace() {
        return codeSpace;
    }

    public void setCodeSpace(String codeSpace) {
        this.codeSpace = codeSpace;
    }

}
