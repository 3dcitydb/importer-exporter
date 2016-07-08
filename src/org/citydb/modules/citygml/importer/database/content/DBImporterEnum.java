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
package org.citydb.modules.citygml.importer.database.content;

import java.util.LinkedList;
import java.util.List;

public enum DBImporterEnum {
	CITYOBJECT(),
	SURFACE_GEOMETRY(CITYOBJECT),
	IMPLICIT_GEOMETRY(CITYOBJECT, SURFACE_GEOMETRY),
	CITYOBJECT_GENERICATTRIB(CITYOBJECT, SURFACE_GEOMETRY),
	EXTERNAL_REFERENCE(CITYOBJECT),
	ADDRESS(),
	BUILDING(CITYOBJECT, SURFACE_GEOMETRY),
	ROOM(CITYOBJECT, BUILDING, SURFACE_GEOMETRY),
	BUILDING_FURNITURE(CITYOBJECT, ROOM, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	BUILDING_INSTALLATION(CITYOBJECT, BUILDING, ROOM, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	THEMATIC_SURFACE(CITYOBJECT, BUILDING, ROOM, BUILDING_INSTALLATION, SURFACE_GEOMETRY),
	OPENING(CITYOBJECT, ADDRESS, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	OPENING_TO_THEM_SURFACE(OPENING, THEMATIC_SURFACE),
	ADDRESS_TO_BUILDING(ADDRESS, BUILDING),
	BRIDGE(CITYOBJECT, SURFACE_GEOMETRY),
	BRIDGE_CONSTR_ELEMENT(CITYOBJECT, BRIDGE, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	BRIDGE_ROOM(CITYOBJECT, BRIDGE, SURFACE_GEOMETRY),
	BRIDGE_FURNITURE(CITYOBJECT, BRIDGE_ROOM, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	BRIDGE_INSTALLATION(CITYOBJECT, BRIDGE, BRIDGE_ROOM, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	BRIDGE_THEMATIC_SURFACE(CITYOBJECT, BRIDGE, BRIDGE_ROOM, BRIDGE_INSTALLATION, BRIDGE_CONSTR_ELEMENT, SURFACE_GEOMETRY),
	BRIDGE_OPENING(CITYOBJECT, ADDRESS, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	BRIDGE_OPEN_TO_THEM_SRF(BRIDGE_OPENING, BRIDGE_THEMATIC_SURFACE),
	ADDRESS_TO_BRIDGE(ADDRESS, BRIDGE),
	TUNNEL(CITYOBJECT, SURFACE_GEOMETRY),
	TUNNEL_HOLLOW_SPACE(CITYOBJECT, TUNNEL, SURFACE_GEOMETRY),
	TUNNEL_FURNITURE(CITYOBJECT, TUNNEL_HOLLOW_SPACE, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	TUNNEL_INSTALLATION(CITYOBJECT, TUNNEL, TUNNEL_HOLLOW_SPACE, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	TUNNEL_THEMATIC_SURFACE(CITYOBJECT, TUNNEL, TUNNEL_HOLLOW_SPACE, TUNNEL_INSTALLATION, SURFACE_GEOMETRY),
	TUNNEL_OPENING(CITYOBJECT, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	TUNNEL_OPEN_TO_THEM_SRF(TUNNEL_OPENING, TUNNEL_THEMATIC_SURFACE),
	TRANSPORTATION_COMPLEX(CITYOBJECT, SURFACE_GEOMETRY),
	TRAFFIC_AREA(CITYOBJECT, TRANSPORTATION_COMPLEX, SURFACE_GEOMETRY),
	CITY_FURNITURE(CITYOBJECT, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	LAND_USE(CITYOBJECT, SURFACE_GEOMETRY),
	WATERBODY(CITYOBJECT, SURFACE_GEOMETRY),
	WATERBOUNDARY_SURFACE(CITYOBJECT, SURFACE_GEOMETRY),
	WATERBOD_TO_WATERBND_SRF(WATERBODY, WATERBOUNDARY_SURFACE),
	PLANT_COVER(CITYOBJECT, SURFACE_GEOMETRY),
	SOLITARY_VEGETAT_OBJECT(CITYOBJECT, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	RELIEF_FEATURE(CITYOBJECT),
	RELIEF_COMPONENT(CITYOBJECT, SURFACE_GEOMETRY),
	RELIEF_FEAT_TO_REL_COMP(RELIEF_FEATURE, RELIEF_COMPONENT),
	GENERIC_CITYOBJECT(CITYOBJECT, SURFACE_GEOMETRY, IMPLICIT_GEOMETRY),
	CITYOBJECTGROUP(CITYOBJECT, SURFACE_GEOMETRY),
	DEPRECATED_MATERIAL_MODEL(),
	APPEARANCE(CITYOBJECT, DEPRECATED_MATERIAL_MODEL),
	TEX_IMAGE(),
	SURFACE_DATA(TEX_IMAGE),
	TEXTURE_PARAM(SURFACE_DATA, SURFACE_GEOMETRY),
	APPEAR_TO_SURFACE_DATA(APPEARANCE, SURFACE_DATA),
	OTHER_GEOMETRY();

	private DBImporterEnum[] dependencies;
	public static List<DBImporterEnum> EXECUTION_PLAN = getExecutionPlan();

	private DBImporterEnum(DBImporterEnum... dependencies) {
		this.dependencies = dependencies;
	}

	public static List<DBImporterEnum> getExecutionPlan() {
		Integer[] weights = new Integer[values().length];
		for (DBImporterEnum type : values())		
			weightDependencies(type, weights);

		return getExecutionPlan(weights);
	}

	public static List<DBImporterEnum> getExecutionPlan(DBImporterEnum type) {
		Integer[] weights = new Integer[values().length];
		weightDependencies(type, weights);

		return getExecutionPlan(weights);
	}

	private static List<DBImporterEnum> getExecutionPlan(Integer[] weights) {
		LinkedList<DBImporterEnum> executionPlan = new LinkedList<DBImporterEnum>();

		int i, j;
		for (i = 0; i < values().length; i++) {
			if (weights[i] == null)
				continue;

			j = 0;
			for (DBImporterEnum item : executionPlan) {
				if (weights[i] >= weights[item.ordinal()])
					break;

				j++;
			}

			executionPlan.add(j, DBImporterEnum.values()[i]);
		}

		return executionPlan;
	}

	private static void weightDependencies(DBImporterEnum type, Integer[] weights) {
		if (weights[type.ordinal()] == null)
			weights[type.ordinal()] = 0;

		for (DBImporterEnum dependence : type.dependencies) {
			if (dependence != null) {
				if (weights[dependence.ordinal()] == null)
					weights[dependence.ordinal()] = 0;

				weights[dependence.ordinal()] += weights[type.ordinal()] + 1;
				weightDependencies(dependence, weights);
			}
		}
	}
}
