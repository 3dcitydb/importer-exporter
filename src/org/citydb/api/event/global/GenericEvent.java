/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2016,
 * Chair of Geoinformatics,
 * Technische Universitaet Muenchen, Germany
 * http://www.gis.bgu.tum.de/
 * 
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 * 
 * virtualcitySYSTEMS GmbH, Berlin <http://www.virtualcitysystems.de/>
 * M.O.S.S. Computer Grafik Systeme GmbH, Muenchen <http://www.moss.de/>
 * 
 * The 3D City Database Importer/Exporter program is free software:
 * you can redistribute it and/or modify it under the terms of the
 * GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 */
package org.citydb.api.event.global;

import java.util.HashMap;
import java.util.Map;

import org.citydb.api.event.Event;

public final class GenericEvent extends Event {
	private final String id;
	private HashMap<String, Object> properties;

	public GenericEvent(String id, Map<String, Object> properties, Object channel, Object source) {
		super(GlobalEvents.GENERIC_EVENT, channel, source);
		this.id = id;

		if (properties != null)
			this.properties = new HashMap<String, Object>(properties);
	}

	public GenericEvent(String id, Map<String, Object> properties, Object source) {
		this(id, properties, GLOBAL_CHANNEL, source);
	}
	
	public GenericEvent(String id, Object channel, Object source) {
		this(id, null, channel, source);
	}
	
	public GenericEvent(String id, Object source) {
		this(id, null, GLOBAL_CHANNEL, source);
	}

	public String getId() {
		return id;
	}

	public boolean hasProperties() {
		return !properties.isEmpty();
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public Object getProperty(String key) {
		return hasProperties() ? properties.get(key) : null;
	}
}
