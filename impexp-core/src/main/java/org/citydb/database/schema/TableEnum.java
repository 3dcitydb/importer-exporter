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
package org.citydb.database.schema;

public enum TableEnum {
	UNDEFINED,
	ADDRESS,
	ADDRESS_TO_BUILDING,
	ADDRESS_TO_BRIDGE,
	APPEARANCE,
	SURFACE_DATA,
	APPEAR_TO_SURFACE_DATA,
	TEX_IMAGE,
	TEXTUREPARAM,
	SURFACE_GEOMETRY,
	IMPLICIT_GEOMETRY,
	CITYOBJECT,
	GENERALIZATION,
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
	BRIDGE_OPEN_TO_THEM_SRF,
	TUNNEL,
	TUNNEL_THEMATIC_SURFACE,
	TUNNEL_OPENING,
	TUNNEL_INSTALLATION,
	TUNNEL_HOLLOW_SPACE,
	TUNNEL_FURNITURE,
	TUNNEL_OPEN_TO_THEM_SRF,
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
	RASTER_RELIEF,
	BREAKLINE_RELIEF,
	MASSPOINT_RELIEF,
	RELIEF_FEAT_TO_REL_COMP,
	GENERIC_CITYOBJECT,
	CITYOBJECTGROUP,
	GROUP_TO_CITYOBJECT;

	public static TableEnum fromOrdinal(int i) {
		for (TableEnum table : TableEnum.values()) {
			if (table.ordinal() == i) {
				return table;
			}
		}

		return UNDEFINED;
	}
	
	public static TableEnum fromTableName(String name) {
		name = name.toLowerCase();
		
		for (TableEnum table : TableEnum.values()) {
			if (table.getName().equals(name)) {
				return table;
			}
		}

		return UNDEFINED;
	}
	
	public String getName() {
		return super.toString().toLowerCase();
	}

	@Override
	public String toString() {
		return getName();
	}
	
}
