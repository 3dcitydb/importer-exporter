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
package org.citydb.gui.components.mapviewer.geocoder;

import java.util.ArrayList;
import java.util.List;

import org.jdesktop.swingx.mapviewer.GeoPosition;

public class Location {
	private List<ResultType> resultTypes;
	private LocationType locationType = LocationType.UNKNOWN;
	private String formattedAddress;
	private GeoPosition position;
	private ViewPort viewPort;

	public Location() {
		resultTypes = new ArrayList<ResultType>();
	}
	
	public void addResultType(ResultType type) {
		resultTypes.add(type);
	}
	
	public List<ResultType> getResultTypes() {
		return resultTypes;
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
	
	@Override
	public String toString() {
		return formattedAddress;
	}
	
}
