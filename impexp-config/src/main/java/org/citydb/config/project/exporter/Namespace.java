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

package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

@XmlType(name = "NamespaceType", propOrder = {
        "prefix",
        "schemaLocation"
})
public class Namespace {
    @XmlAttribute(required = true)
    private String uri;
    @XmlAttribute
    private NamespaceMode mode;
    private String prefix;
    private String schemaLocation;

    public boolean isSetURI() {
        return uri != null;
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public NamespaceMode getMode() {
        return mode != null ? mode : NamespaceMode.AUTOMATIC;
    }

    public void setMode(NamespaceMode mode) {
        this.mode = mode;
    }

    public boolean isSetPrefix() {
        return prefix != null;
    }

    public String getPrefix() {
        return prefix != null ? prefix.trim() : null;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isSetSchemaLocation() {
        return schemaLocation != null;
    }

    public String getSchemaLocation() {
        return schemaLocation != null ? schemaLocation.trim() : null;
    }

    public void setSchemaLocation(String schemaLocation) {
        this.schemaLocation = schemaLocation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, prefix, schemaLocation, mode);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Namespace)) {
            return false;
        }

        return uri.equals(((Namespace) obj).getURI());
    }
}
