/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * (C) 2013 - 2015,
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
package org.citydb.gui.components.mapviewer.geocoder;

public enum ResultType {
	STREET_ADDRESS("street_address"),
	ROUTE("route"),
	INTERSECTION("intersection"),
	POLITICAL("political"),
	COUNTRY("country"),
	ADMINISTRATIVE_AREA_LEVEL_1("administrative_area_level_1"),
	ADMINISTRATIVE_AREA_LEVEL_2("administrative_area_level_2"),
	ADMINISTRATIVE_AREA_LEVEL_3("administrative_area_level_3"),
	COLLOQUIAL_AREA("colloquial_area"),
	LOCALITY("locality"),
	SUBLOCALITY("sublocality"),
	NEIGHBORHOOD("neighborhood"),
	PREMISE("premise"),
	SUBPREMISE("subpremise"),
	POSTAL_CODE("postal_code"),
	NATURAL_FEATURE("natural_feature"),
	AIRPORT("airport"),
	PARK("park"),
	POINT_OF_INTEREST("point_of_interest"),
	UNKNOWN("unknown");
	
	private final String value;
	
	ResultType(String value) {
		this.value = value;
	}
	
	public static ResultType fromValue(String v) {
		for (ResultType c: ResultType.values()) {
			if (c.value.equalsIgnoreCase(v)) {
				return c;
			}
		}

		return UNKNOWN;
	}
	
	public String toString() {
		return value;
	}
}
