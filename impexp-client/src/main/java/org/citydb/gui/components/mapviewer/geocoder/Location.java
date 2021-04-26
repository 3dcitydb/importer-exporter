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
package org.citydb.gui.components.mapviewer.geocoder;

import org.jdesktop.swingx.mapviewer.GeoPosition;

import java.util.HashMap;
import java.util.Map;

public class Location {
	private LocationType locationType = LocationType.UNKNOWN;
	private String formattedAddress;
	private GeoPosition position;
	private ViewPort viewPort;
	private Map<String, Object> attributes;

	public Location() {
		attributes = new HashMap<>();
	}
	
	public LocationType getLocationType() {
		return locationType;
	}

	public void setLocationType(LocationType type) {
		this.locationType = type;
	}

	public String getFormattedAddress() {
		return formattedAddress;
	}
	
	public void setFormattedAddress(String formattedAddress) {
		this.formattedAddress = formattedAddress;
	}
	
	public GeoPosition getPosition() {
		return position;
	}
	
	public void setPosition(GeoPosition position) {
		this.position = position;
	}
	
	public void setViewPort(GeoPosition southWest, GeoPosition northEast) {
		viewPort = new ViewPort(southWest, northEast);
	}

	public ViewPort getViewPort() {
		return viewPort;
	}

	public void addAttribute(String name, Object value) {
		attributes.put(name, value);
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}

	public Object getAttribute(String name) {
		return attributes.get(name);
	}

	public <T> T getAttribute(String name, Class<T> type) {
		Object attribute = attributes.get(name);
		if (type.isInstance(attribute))
			return type.cast(attribute);

		return null;
	}
	
	@Override
	public String toString() {
		return formattedAddress;
	}
	
}
