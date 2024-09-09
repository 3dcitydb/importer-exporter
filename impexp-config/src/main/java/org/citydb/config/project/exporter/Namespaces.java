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

package org.citydb.config.project.exporter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.*;

@XmlType(name = "NamespacesType", propOrder = {})
public class Namespaces {
    @XmlAttribute(required = true)
    private boolean isEnabled;
    @XmlAttribute
    private boolean skipOthers;
    @XmlElement(name = "namespace")
    private LinkedHashSet<Namespace> namespaces;

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isSkipOthers() {
        return skipOthers;
    }

    public void setSkipOthers(boolean skipOthers) {
        this.skipOthers = skipOthers;
    }

    public boolean hasNamespaces() {
        return namespaces != null && !namespaces.isEmpty();
    }

    public Map<String, Namespace> getNamespaces() {
        if (hasNamespaces()) {
            Map<String, Namespace> result = new LinkedHashMap<>();
            namespaces.forEach(namespace -> result.put(namespace.getURI(), namespace));
            return result;
        } else {
            return Collections.emptyMap();
        }
    }

    public void setNamespaces(Collection<Namespace> namespaces) {
        this.namespaces = namespaces != null ?
                new LinkedHashSet<>(namespaces) :
                null;
    }
}
