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
package org.citydb.config.project.global;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProxyConfigAdapter extends XmlAdapter<ProxyConfigAdapter.ProxyConfigList, Map<ProxyType, ProxyConfig>> {

    public static class ProxyConfigList {
        @XmlElement(name = "proxy")
        private List<ProxyConfig> proxies;
    }

    @Override
    public Map<ProxyType, ProxyConfig> unmarshal(ProxyConfigList v) throws Exception {
        Map<ProxyType, ProxyConfig> map = new HashMap<>();

        if (v != null && v.proxies != null) {
            for (ProxyConfig proxy : v.proxies)
                map.put(proxy.getType(), proxy);
        }

        for (ProxyType type : ProxyType.values())
            if (!map.containsKey(type))
                map.put(type, new ProxyConfig(type));

        return map;
    }

    @Override
    public ProxyConfigList marshal(Map<ProxyType, ProxyConfig> v) throws Exception {
        ProxyConfigList list = new ProxyConfigList();

        if (v != null)
            list.proxies = new ArrayList<>(v.values());

        return list;
    }

}
