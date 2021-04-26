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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class NamespaceAdapter extends XmlAdapter<NamespaceAdapter.NamespaceList, LinkedHashMap<String, Namespace>> {

    public static class NamespaceList {
        @XmlElement(name = "namespace")
        private List<Namespace> namespaces;
    }

    @Override
    public LinkedHashMap<String, Namespace> unmarshal(NamespaceList v) {
        LinkedHashMap<String, Namespace> namespaces = null;

        if (v != null && v.namespaces != null && !v.namespaces.isEmpty()) {
            namespaces = new LinkedHashMap<>();
            for (Namespace namespace : v.namespaces) {
                if (namespace.isSetURI())
                    namespaces.put(namespace.getURI(), namespace);
            }
        }

        return namespaces;
    }

    @Override
    public NamespaceList marshal(LinkedHashMap<String, Namespace> v) {
        NamespaceList list = null;

        if (v != null && !v.isEmpty()) {
            list = new NamespaceList();
            list.namespaces = new ArrayList<>(v.values());
        }

        return list;
    }
}