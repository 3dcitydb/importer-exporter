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
package org.citydb.database;


public enum TableEnum {
	UNDEFINED,
	ADDRESS,
	ADDRESS_TO_BUILDING,
	APPEARANCE,
	SURFACE_DATA,
	TEX_IMAGE,
	SURFACE_GEOMETRY,
	IMPLICIT_GEOMETRY,
	CITYOBJECT,
	CITYOBJECT_GENERICATTRIB,
	EXTERNAL_REFERENCE,
	BUILDING,
	ROOM,
	BUILDING_FURNITURE,
	BUILDING_INSTALLATION,
	THEMATIC_SURFACE,
	OPENING,
	OPENING_TO_THEM_SURFACE,
	BRIDGE,
	BRIDGE_CONSTR_ELEMENT,
	BRIDGE_INSTALLATION,
	BRIDGE_THEMATIC_SURFACE,
	BRIDGE_OPENING,
	BRIDGE_ROOM,
	BRIDGE_FURNITURE,
	TUNNEL,
	TUNNEL_THEMATIC_SURFACE,
	TUNNEL_OPENING,
	TUNNEL_INSTALLATION,
	TUNNEL_HOLLOW_SPACE,
	TUNNEL_FURNITURE,
	WATERBODY,
	WATERBOUNDARY_SURFACE,
	WATERBOD_TO_WATERBND_SRF,
	PLANT_COVER,
	SOLITARY_VEGETAT_OBJECT,
	TRANSPORTATION_COMPLEX,
	TRAFFIC_AREA,
	CITY_FURNITURE,
	LAND_USE,
	RELIEF_FEATURE,
	RELIEF_COMPONENT,
	TIN_RELIEF,
	GENERIC_CITYOBJECT,
	CITYOBJECTGROUP;

	public static TableEnum fromInt(int i) {
		for (TableEnum c : TableEnum.values()) {
			if (c.ordinal() == i) {
				return c;
			}
		}

		return UNDEFINED;
	}
}
