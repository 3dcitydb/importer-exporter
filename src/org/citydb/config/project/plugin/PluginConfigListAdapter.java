/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2016
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
package org.citydb.config.project.plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.citydb.api.plugin.extension.config.PluginConfig;

public class PluginConfigListAdapter extends XmlAdapter<PluginConfigList, HashMap<Class<? extends PluginConfig>, PluginConfig>> {

	@Override
	public HashMap<Class<? extends PluginConfig>, PluginConfig> unmarshal(PluginConfigList v) throws Exception {
		HashMap<Class<? extends PluginConfig>, PluginConfig> map = new HashMap<Class<? extends PluginConfig>, PluginConfig>();

		if (v != null) {
			for (PluginConfigItem item : v.getItems())
				if (item.getConfig() != null && !item.getConfig().getClass().equals(PluginConfig.class))
					map.put((Class<? extends PluginConfig>)item.getConfig().getClass(), item.getConfig());
		}

		return map;
	}

	@Override
	public PluginConfigList marshal(HashMap<Class<? extends PluginConfig>, PluginConfig> v) throws Exception {
		PluginConfigList list = new PluginConfigList();

		if (v != null) {
			Iterator<Entry<Class<? extends PluginConfig>, PluginConfig>> iter = v.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Class<? extends PluginConfig>, PluginConfig> entry = iter.next();
				PluginConfigItem item = new PluginConfigItem();
				item.setConfig(entry.getValue());
				list.addItem(item);
			}
		}

		return list;
	}

}
