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

package org.citydb.config.project.plugin;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluginListAdapter extends XmlAdapter<PluginListAdapter.PluginList, Map<String, Boolean>> {

    public static class PluginList {
        private List<PluginState> plugin;
    }

    @Override
    public Map<String, Boolean> unmarshal(PluginList plugins) throws Exception {
        Map<String, Boolean> map = new HashMap<>();

        if (plugins != null && plugins.plugin != null && !plugins.plugin.isEmpty()) {
            for (PluginState pluginState : plugins.plugin) {
                if (pluginState != null) {
                    map.put(pluginState.getPluginClass(), pluginState.isEnabled());
                }
            }
        }

        return map;
    }

    @Override
    public PluginList marshal(Map<String, Boolean> plugins) throws Exception {
        PluginList list = null;

        if (plugins != null && !plugins.isEmpty()) {
            list = new PluginList();
            list.plugin = new ArrayList<>();
            for (Map.Entry<String, Boolean> entry : plugins.entrySet()) {
                if (entry.getValue() != null) {
                    PluginState pluginState = new PluginState();
                    pluginState.setPluginClass(entry.getKey());
                    pluginState.setEnabled(entry.getValue());
                    list.plugin.add(pluginState);
                }
            }
        }

        return list;
    }
}
