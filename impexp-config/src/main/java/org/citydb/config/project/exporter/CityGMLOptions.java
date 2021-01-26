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

package org.citydb.config.project.exporter;

import org.citydb.config.project.common.XSLTransformation;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@XmlType(name = "CityGMLExportOptionsType", propOrder = {})
public class CityGMLOptions {
    private Boolean writeProductHeader;
    @XmlJavaTypeAdapter(NamespaceAdapter.class)
    private LinkedHashMap<String, Namespace> namespaces;
    private XLink xlink;
    private XSLTransformation xslTransformation;

    public CityGMLOptions() {
        xlink = new XLink();
        xslTransformation = new XSLTransformation();
    }

    public boolean isWriteProductHeader() {
        return writeProductHeader != null ? writeProductHeader : true;
    }

    public void setWriteProductHeader(Boolean writeProductHeader) {
        this.writeProductHeader = writeProductHeader;
    }

    public boolean isSetNamespaces() {
        return namespaces != null && !namespaces.isEmpty();
    }

    public Namespace getNamespace(String uri) {
        return namespaces != null ? namespaces.get(uri) : null;
    }

    public Map<String, Namespace> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(List<Namespace> namespaces) {
        if (this.namespaces == null) {
            this.namespaces = new LinkedHashMap<>();
        }

        namespaces.stream()
                .filter(Namespace::isSetURI)
                .forEach(v -> this.namespaces.put(v.getURI(), v));
    }

    public XLink getXlink() {
        return xlink;
    }

    public void setXlink(XLink xlink) {
        if (xlink != null) {
            this.xlink = xlink;
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
