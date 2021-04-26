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
package org.citydb.config.project.plugin;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PluginConfigListAdapter extends XmlAdapter<PluginConfigListAdapter.PluginConfigList, Map<Class<? extends PluginConfig>, PluginConfig>> {

    public static class PluginConfigList {
        private List<PluginConfigItem> plugin;
    }

    @Override
    public Map<Class<? extends PluginConfig>, PluginConfig> unmarshal(PluginConfigList v) throws Exception {
        Map<Class<? extends PluginConfig>, PluginConfig> map = new HashMap<>();

        if (v != null && v.plugin != null) {
            for (PluginConfigItem item : v.plugin)
                if (item.getConfig() != null && !item.getConfig().getClass().equals(PluginConfig.class))
                    map.put(item.getConfig().getClass(), item.getConfig());
        }

        return map;
    }

    @Override
    public PluginConfigList marshal(Map<Class<? extends PluginConfig>, PluginConfig> v) throws Exception {
        PluginConfigList list = new PluginConfigList();

        if (v != null) {
            list.plugin = new ArrayList<>();
            for (Entry<Class<? extends PluginConfig>, PluginConfig> entry : v.entrySet()) {
                PluginConfigItem item = new PluginConfigItem();
                item.setConfig(entry.getValue());
                list.plugin.add(item);
            }
        }

        return list;
    }

}
