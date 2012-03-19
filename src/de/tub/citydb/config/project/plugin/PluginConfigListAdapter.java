/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
 * Institute for Geodesy and Geoinformation Science
 * Technische Universitaet Berlin, Germany
 * http://www.gis.tu-berlin.de/
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program. If not, see 
 * <http://www.gnu.org/licenses/>.
 * 
 * The development of the 3D City Database Importer/Exporter has 
 * been financially supported by the following cooperation partners:
 * 
 * Business Location Center, Berlin <http://www.businesslocationcenter.de/>
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * Berlin Senate of Business, Technology and Women <http://www.berlin.de/sen/wtf/>
 */
package de.tub.citydb.config.project.plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import de.tub.citydb.api.plugin.extension.config.PluginConfig;

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
