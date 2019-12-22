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
package org.citydb.config.project.ade;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ADEExtensionAdapter extends XmlAdapter<ADEExtensionAdapter.ADEExtensionList, Map<String, ADEExtension>> {

    public static class ADEExtensionList {
        @XmlElement(name = "adeExtension")
        private List<ADEExtension> adeExtensions;
    }

    @Override
    public Map<String, ADEExtension> unmarshal(ADEExtensionList v) {
        Map<String, ADEExtension> adeExtensions = null;

        if (v != null && v.adeExtensions != null && !v.adeExtensions.isEmpty()) {
            adeExtensions = new HashMap<>();
            for (ADEExtension adeExtension : v.adeExtensions) {
                if (adeExtension.isSetExtensionId())
                    adeExtensions.put(adeExtension.getExtensionId(), adeExtension);
            }
        }

        return adeExtensions;
    }

    @Override
    public ADEExtensionList marshal(Map<String, ADEExtension> v) {
        ADEExtensionList list = null;

        if (v != null && !v.isEmpty()) {
            list = new ADEExtensionList();
            list.adeExtensions = new ArrayList<>(v.values());
        }

        return list;
    }
}