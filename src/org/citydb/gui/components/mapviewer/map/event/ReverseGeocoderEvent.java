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
package org.citydb.gui.components.mapviewer.map.event;

import org.citydb.api.event.Event;
import org.citydb.gui.components.mapviewer.geocoder.GeocoderResponse;
import org.citydb.gui.components.mapviewer.geocoder.Location;

public class ReverseGeocoderEvent extends Event {
	private final ReverseGeocoderStatus status;
	private final Location location;
	private final GeocoderResponse response;
	
	public enum ReverseGeocoderStatus {
		SEARCHING,
		RESULT,
		ERROR
	}	
	
	public ReverseGeocoderEvent(Object source) {
		super(MapEvents.REVERSE_GEOCODER, GLOBAL_CHANNEL, source);
		this.status = ReverseGeocoderStatus.SEARCHING;
		location = null;
		response = null;
	}
	
	public ReverseGeocoderEvent(Location location, Object source) {
		super(MapEvents.REVERSE_GEOCODER, GLOBAL_CHANNEL, source);
		this.status = ReverseGeocoderStatus.RESULT;
		this.location = location;
		response = null;
	}
	
	public ReverseGeocoderEvent(GeocoderResponse response, Object source) {
		super(MapEvents.REVERSE_GEOCODER, GLOBAL_CHANNEL, source);
		this.status = ReverseGeocoderStatus.ERROR;
		location = null;
		this.response = response;
	}

	public ReverseGeocoderStatus getStatus() {
		return status;
	}

	public Location getLocation() {
		return location;
	}

	public GeocoderResponse getResponse() {
		return response;
	}
	
}
