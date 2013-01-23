/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2013
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
package de.tub.citydb.gui.components.mapviewer.geocoder;

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
