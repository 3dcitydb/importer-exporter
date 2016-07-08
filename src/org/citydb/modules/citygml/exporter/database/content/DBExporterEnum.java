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
package org.citydb.modules.citygml.exporter.database.content;

public enum DBExporterEnum {
	SURFACE_GEOMETRY,
	IMPLICIT_GEOMETRY,
	CITYOBJECT,
	CITYOBJECT_GENERICATTRIB,
	BUILDING,
	ROOM,
	BUILDING_FURNITURE,
	BUILDING_INSTALLATION,
	THEMATIC_SURFACE,
	BRIDGE,
	BRIDGE_THEMATIC_SURFACE,
	BRIDGE_CONSTR_ELEMENT,
	BRIDGE_INSTALLATION,
	BRIDGE_ROOM,
	BRIDGE_FURNITURE,
	TUNNEL,
	TUNNEL_THEMATIC_SURFACE,
	TUNNEL_INSTALLATION,
	TUNNEL_HOLLOW_SPACE,
	TUNNEL_FURNITURE,
	CITY_FURNITURE,
	LAND_USE,
	WATERBODY,
	PLANT_COVER,
	TRANSPORTATION_COMPLEX,
	SOLITARY_VEGETAT_OBJECT,
	RELIEF_FEATURE,
	LOCAL_APPEARANCE,
	GLOBAL_APPEARANCE,
	LOCAL_APPEARANCE_TEXTUREPARAM,
	GLOBAL_APPEARANCE_TEXTUREPARAM,
	GENERIC_CITYOBJECT,
	CITYOBJECTGROUP,
	GENERALIZATION,
	OTHER_GEOMETRY,
}
