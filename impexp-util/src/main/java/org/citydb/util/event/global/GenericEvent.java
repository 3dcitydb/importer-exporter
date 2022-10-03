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
package org.citydb.util.event.global;

import org.citydb.util.event.Event;

import java.util.HashMap;
import java.util.Map;

public final class GenericEvent extends Event {
	private final String id;
	private final Map<String, Object> properties;

	public GenericEvent(String id, Map<String, Object> properties, Object channel, String label) {
		super(EventType.GENERIC_EVENT, channel, label);
		this.id = id;
		this.properties = properties != null ?
				new HashMap<>(properties) :
				new HashMap<>();
	}

	public GenericEvent(String id, Map<String, Object> properties, String label) {
		this(id, properties, GLOBAL_CHANNEL, label);
	}

	public GenericEvent(String id, String label) {
		this(id, null, GLOBAL_CHANNEL, label);
	}

	public String getId() {
		return id;
	}

	public boolean hasProperties() {
		return !properties.isEmpty();
	}
	
	public boolean isSetProperty(String key) {
		return properties.containsKey(key);
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}
	
	public Object setProperty(String key, Object property) {
		return properties.put(key, property);
	}
}
