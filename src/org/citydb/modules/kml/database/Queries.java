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
package org.citydb.modules.kml.database;

import java.util.HashMap;

import org.citydb.api.database.DatabaseType;
import org.citydb.api.log.LogLevel;
import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.Lod0FootprintMode;
import org.citydb.database.adapter.AbstractSQLAdapter;
import org.citydb.log.Logger;
import org.citydb.modules.citygml.importer.database.content.DBSequencerEnum;

public class Queries {

	// ----------------------------------------------------------------------
	// 	GENERIC PURPOSE QUERIES
	// ----------------------------------------------------------------------

	public static final String GET_IDS(DatabaseType type) {
		StringBuilder query = new StringBuilder()
		.append("SELECT co.id, co.gmlid, co.objectclass_id, co.envelope FROM CITYOBJECT co WHERE ");
		
		switch (type) {
		case ORACLE:
			query.append("SDO_ANYINTERACT(co.envelope, ?) = 'TRUE'");
			break;
		case POSTGIS:
			query.append("co.envelope && ?");
			break;
		}		
		
		return query.toString();
	}

	public static final String GET_EXTRUDED_HEIGHT(DatabaseType type) {
		switch (type) {
		case ORACLE:
			return "SELECT " + // "b.measured_height, " +
			"SDO_GEOM.SDO_MAX_MBR_ORDINATE(co.envelope, 3) - SDO_GEOM.SDO_MIN_MBR_ORDINATE(co.envelope, 3) AS envelope_measured_height " +
			"FROM CITYOBJECT co " + // ", BUILDING b " +
			"WHERE co.id = ?"; // + " AND b.building_root_id = co.id";
		case POSTGIS:
			return "SELECT " + // "b.measured_height, " +
			"ST_ZMax(Box3D(co.envelope)) - ST_ZMin(Box3D(co.envelope)) AS envelope_measured_height " +
			"FROM CITYOBJECT co " + // ", BUILDING b " +
			"WHERE co.id = ?"; // + " AND b.building_root_id = co.id";
		default:
			return null;
		}
	}

	public static final String GET_STRVAL_GENERICATTRIB_FROM_ID =
			"SELECT coga.strval " +
					"FROM CITYOBJECT_GENERICATTRIB coga " + 
					"WHERE coga.cityobject_id = ? AND coga.attrname = ? ";

	public static final String GET_ID_FROM_GMLID =
			"SELECT id FROM CITYOBJECT WHERE gmlid = ?";

	public static final String GET_ID_AND_OBJECTCLASS_FROM_GMLID =
			"SELECT id, objectclass_id FROM CITYOBJECT WHERE gmlid = ?";

	public static final String GET_GMLID_AND_OBJECTCLASS_FROM_ID =
			"SELECT gmlid, objectclass_id FROM CITYOBJECT WHERE id = ?";

	public static final String INSERT_GE_ZOFFSET(AbstractSQLAdapter sqlAdapter) {
		return "INSERT INTO CITYOBJECT_GENERICATTRIB (ID, ATTRNAME, DATATYPE, STRVAL, CITYOBJECT_ID) " +
				"VALUES (" + sqlAdapter.getNextSequenceValue(DBSequencerEnum.CITYOBJECT_GENERICATTRIB_ID_SEQ) + ", ?, 1, ?, ?)";
	}

	// ----------------------------------------------------------------------
	// 	BUILDING QUERIES
	// ----------------------------------------------------------------------

	public static final String BUILDING_PARTS_FROM_BUILDING =
			"SELECT id FROM BUILDING WHERE building_root_id = ?";

	private static final String BUILDING_PART_FOOTPRINT_LOD4 =
			"SELECT sg.geometry " +
					"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts " +
					"WHERE " +
					"ts.building_id = ? " +
					"AND ts.objectclass_id = '35' " +
					"AND sg.root_id = ts.lod4_multi_surface_id " +
					"AND sg.geometry IS NOT NULL "; 


	private static final String BUILDING_PART_COLLADA_LOD4_ROOT_IDS =
			"SELECT geom.gid FROM (" + 
					// Building
					"SELECT ts.lod4_multi_surface_id as gid " + 					
					"FROM THEMATIC_SURFACE ts " + 
					"WHERE " +  
					"ts.building_id = ? " +
					"AND ts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT lod4_solid_id AS gid  FROM BUILDING " +
					"WHERE " +
						"id = ? AND lod4_solid_id IS NOT NULL " +
						"AND id NOT IN " +
						"(SELECT building_id FROM THEMATIC_SURFACE " +
							"WHERE building_id = ? " +
							"AND lod4_multi_surface_id IS NOT NULL " +
						") " +
					"UNION " +					 
					"SELECT lod4_multi_surface_id AS gid  FROM BUILDING " +
					"WHERE " +
						"id = ? AND lod4_multi_surface_id IS NOT NULL " +
						"AND id NOT IN " +
						"(SELECT building_id FROM THEMATIC_SURFACE " +
							"WHERE building_id = ? " +
							"AND lod4_multi_surface_id IS NOT NULL " +
						") " +
					"UNION " + 
					// Room
					"SELECT ts.lod4_multi_surface_id as gid " + 
					"FROM ROOM r, THEMATIC_SURFACE ts " + 
					"WHERE " +  
					"r.building_id = ? " +
					"AND ts.room_id = r.id " +
					"AND ts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
			        "SELECT r.lod4_solid_id as gid " + 
			        "FROM ROOM r LEFT JOIN THEMATIC_SURFACE ts ON ts.room_id = r.id " + 
			        "WHERE " +  
	  			    "r.building_id = ? " +
				    "AND r.lod4_solid_id IS NOT NULL " +
	  			    "AND ts.lod4_multi_surface_id IS NULL " + 
				    "UNION " + 
			        "SELECT r.lod4_multi_surface_id as gid " + 
			        "FROM ROOM r LEFT JOIN THEMATIC_SURFACE ts ON ts.room_id = r.id " + 
			        "WHERE " +  
	  			    "r.building_id = ? " +
				    "AND r.lod4_multi_surface_id IS NOT NULL " +
	  			    "AND ts.lod4_multi_surface_id IS NULL " +
				    "UNION " + 
	  			    // Building Furniture
					"SELECT bf.lod4_brep_id as gid " + 
					"FROM ROOM r, BUILDING_FURNITURE bf " + 
					"WHERE " +  
					"r.building_id = ? " +
					"AND bf.room_id = r.id " +
					"AND bf.lod4_brep_id IS NOT NULL " +
					"UNION " + 			
					// Building  Installation
					"SELECT ts.lod4_multi_surface_id as gid " + 
					"FROM BUILDING_INSTALLATION bi, THEMATIC_SURFACE ts " + 
					"WHERE bi.building_id = ? " +  
					"AND ts.building_installation_id = bi.id " +
					"AND ts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT bi.lod4_brep_id as gid " + 
					"FROM BUILDING_INSTALLATION bi LEFT JOIN THEMATIC_SURFACE ts ON ts.building_installation_id = bi.id " +
					"WHERE " +  
					"bi.building_id = ? " +
					"AND bi.lod4_brep_id IS NOT NULL " +
					"AND ts.lod4_multi_surface_id IS NULL " + 
					"UNION " + 
					// Opening
					"SELECT o.lod4_multi_surface_id as gid " + 
					"FROM THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE o2ts, OPENING o " + 
					"WHERE " +  
					"ts.building_id = ? " +
					"AND ts.lod4_multi_surface_id IS NOT NULL " +
					"AND o2ts.thematic_surface_id = ts.id " +
					"AND o.id = o2ts.opening_id) geom";


	private static final String BUILDING_PART_GEOMETRY_LOD4 =
			"SELECT sg.geometry, ts.objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"LEFT JOIN THEMATIC_SURFACE ts ON ts.lod4_multi_surface_id = sg.root_id " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + BUILDING_PART_COLLADA_LOD4_ROOT_IDS + ")";

	private static final String BUILDING_PART_FOOTPRINT_LOD3 =
			"SELECT sg.geometry " +
					"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts " +
					"WHERE " +
					"ts.building_id = ? " +
					"AND ts.objectclass_id = '35' " +
					"AND sg.root_id = ts.lod3_multi_surface_id " +
					"AND sg.geometry IS NOT NULL "; 

	private static final String BUILDING_PART_COLLADA_LOD3_ROOT_IDS =
			"SELECT geom.gid FROM (" + 
					// Building
					"SELECT ts.lod3_multi_surface_id as gid " + 
					"FROM THEMATIC_SURFACE ts " + 
					"WHERE " +  
					"ts.building_id = ? " +
					"AND ts.lod3_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT lod3_solid_id AS gid FROM BUILDING " +
					"WHERE " +
						"id = ? AND lod3_solid_id IS NOT NULL " +
						"AND id NOT IN " +
						"(SELECT building_id FROM THEMATIC_SURFACE " +
							"WHERE building_id = ? " +
							"AND lod3_multi_surface_id IS NOT NULL " +
						") " +
					"UNION " +					 
					"SELECT lod3_multi_surface_id AS gid  FROM BUILDING " +
					"WHERE " +
						"id = ? AND lod3_multi_surface_id IS NOT NULL " +
						"AND id NOT IN " +
						"(SELECT building_id FROM THEMATIC_SURFACE " +
							"WHERE building_id = ? " +
							"AND lod3_multi_surface_id IS NOT NULL " +
						") " +
					"UNION " + 
					// Building Installation
					"SELECT ts.lod3_multi_surface_id as gid " + 
					"FROM BUILDING_INSTALLATION bi, THEMATIC_SURFACE ts " + 
					"WHERE bi.building_id = ? " +  
					"AND ts.building_installation_id = bi.id " +
					"AND ts.lod3_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT bi.lod3_brep_id as gid " + 
					"FROM BUILDING_INSTALLATION bi LEFT JOIN THEMATIC_SURFACE ts ON ts.building_installation_id = bi.id " +
					"WHERE " +  
					"bi.building_id = ? " +
					"AND bi.lod3_brep_id IS NOT NULL " +
					"AND ts.lod3_multi_surface_id IS NULL " + 
					"UNION " + 
					// Opening
					"SELECT o.lod3_multi_surface_id as gid " + 
					"FROM THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE o2ts, OPENING o " + 
					"WHERE " +  
					"ts.building_id = ? " +
					"AND ts.lod3_multi_surface_id IS NOT NULL " +
					"AND o2ts.thematic_surface_id = ts.id " +
					"AND o.id = o2ts.opening_id) geom";

	
	private static final String BUILDING_PART_GEOMETRY_LOD3 =
			"SELECT sg.geometry, ts.objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"LEFT JOIN THEMATIC_SURFACE ts ON ts.lod3_multi_surface_id = sg.root_id " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + BUILDING_PART_COLLADA_LOD3_ROOT_IDS	+ ")";
	

	private static final String BUILDING_PART_FOOTPRINT_LOD2 =
			"SELECT sg.geometry " +
					"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts " +
					"WHERE " +
					"ts.building_id = ? " +
					"AND ts.objectclass_id = '35' " +
					"AND sg.root_id = ts.lod2_multi_surface_id " +
					"AND sg.geometry IS NOT NULL " +
					"ORDER BY ts.building_id";
	
	
	private static final String BUILDING_PART_COLLADA_LOD2_ROOT_IDS =
			"SELECT geom.gid FROM ( " + 
					// Building
					"SELECT ts.lod2_multi_surface_id as gid " + 
					"FROM THEMATIC_SURFACE ts " + 
					"WHERE " +  
					"ts.building_id = ? " +
					"AND ts.lod2_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT lod2_solid_id AS gid FROM BUILDING " +
					"WHERE " +
						"id = ? AND lod2_solid_id IS NOT NULL " +
						"AND id NOT IN " +
						"(SELECT building_id FROM THEMATIC_SURFACE " +
							"WHERE building_id = ? " +
							"AND lod2_multi_surface_id IS NOT NULL " +
						") " +
					"UNION " +					 
					"SELECT lod2_multi_surface_id AS gid FROM BUILDING " +
					"WHERE " +
						"id = ? AND lod2_multi_surface_id IS NOT NULL " +
						"AND id NOT IN " +
						"(SELECT building_id FROM THEMATIC_SURFACE " +
							"WHERE building_id = ? " +
							"AND lod2_multi_surface_id IS NOT NULL " +
						") " +				
					"UNION " +			
					// Building Installation	
					"SELECT ts.lod2_multi_surface_id as gid " + 
					"FROM BUILDING_INSTALLATION bi, THEMATIC_SURFACE ts " + 
					"WHERE bi.building_id = ? " +  
					"AND ts.building_installation_id = bi.id " +
					"AND ts.lod2_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT bi.lod2_brep_id as gid " + 
					"FROM BUILDING_INSTALLATION bi LEFT JOIN THEMATIC_SURFACE ts ON ts.building_installation_id = bi.id " +
					"WHERE " +  
					"bi.building_id = ? " +
					"AND bi.lod2_brep_id IS NOT NULL " +
					"AND ts.lod2_multi_surface_id IS NULL) geom";
	
	
	private static final String BUILDING_PART_GEOMETRY_LOD2 =
			"SELECT sg.geometry, ts.objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"LEFT JOIN THEMATIC_SURFACE ts ON ts.lod2_multi_surface_id = sg.root_id " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + BUILDING_PART_COLLADA_LOD2_ROOT_IDS	+ ")";

	
	private static final String BUILDING_PART_FOOTPRINT_LOD1(DatabaseType type) {		
		switch (type) {
		case ORACLE:
			return BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1(type).replace("<TOLERANCE>", "0.001")
					.replace("<2D_SRID>", "(SELECT SRID FROM DATABASE_SRS)")
					.replace("<LoD>", "1")
					.replace("<GROUP_BY_1>", "256")
					.replace("<GROUP_BY_2>", "64")
					.replace("<GROUP_BY_3>", "16");
		case POSTGIS:		
			return BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1(type).replace("<TOLERANCE>", "0.001")
					.replace("<LoD>", "1");			
		default:
			return null;
		}
	}	
	
	private static final String BUILDING_PART_COLLADA_LOD1_ROOT_IDS =
			"SELECT geom.gid FROM (SELECT b.lod1_multi_surface_id as gid " +			
					"FROM BUILDING b " +
					"WHERE " +
					"b.id = ? " +
					"AND b.lod1_multi_surface_id IS NOT NULL " + 
					"UNION " + 
					"SELECT b.lod1_solid_id as gid " +			
					"FROM BUILDING b " +
					"WHERE " +
					"b.id = ? " +
					"AND b.lod1_solid_id IS NOT NULL) geom"; 
	
	
	private static final String BUILDING_PART_GEOMETRY_LOD1 =
			"SELECT sg.geometry, NULL as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + BUILDING_PART_COLLADA_LOD1_ROOT_IDS	+ ")";
	
	
	private static final String COLLADA_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID_0 =
			"SELECT sg.geometry, sg.id, sg.parent_id, sg.root_id, sd.tex_image_id, co.objectclass_id, " +
					"sd.x3d_shininess, sd.x3d_transparency, sd.x3d_ambient_intensity, sd.x3d_specular_color, sd.x3d_diffuse_color, sd.x3d_emissive_color, sd.x3d_is_smooth, " +
					"ti.tex_image_uri, tp.texture_coordinates, a.theme " +
					"FROM SURFACE_GEOMETRY sg " +
					"LEFT JOIN TEXTUREPARAM tp ON tp.surface_geometry_id = sg.id " + 
					"LEFT JOIN SURFACE_DATA sd ON sd.id = tp.surface_data_id " +
					"LEFT JOIN TEX_IMAGE ti ON ti.id = sd.tex_image_id " +
					"LEFT JOIN APPEAR_TO_SURFACE_DATA a2sd ON a2sd.surface_data_id = sd.id " +
					"LEFT JOIN APPEARANCE a ON a2sd.appearance_id = a.id " +
					"LEFT JOIN CITYOBJECT co ON sg.cityobject_id = co.id " +
					"WHERE " +
					"sg.root_id = ? "; 
	
	private static final String COLLADA_IMPLICIT_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID_0 =
			"SELECT sg.implicit_geometry, sg.id, sg.parent_id, sg.root_id, sd.tex_image_id, " +
					"sd.x3d_shininess, sd.x3d_transparency, sd.x3d_ambient_intensity, sd.x3d_specular_color, sd.x3d_diffuse_color, sd.x3d_emissive_color, sd.x3d_is_smooth, " +
					"ti.tex_image_uri, tp.texture_coordinates, a.theme " +
					"FROM SURFACE_GEOMETRY sg " +
					"LEFT JOIN TEXTUREPARAM tp ON tp.surface_geometry_id = sg.id " + 
					"LEFT JOIN SURFACE_DATA sd ON sd.id = tp.surface_data_id " +
					"LEFT JOIN TEX_IMAGE ti ON ti.id = sd.tex_image_id " +
					"LEFT JOIN APPEAR_TO_SURFACE_DATA a2sd ON a2sd.surface_data_id = sd.id " +
					"LEFT JOIN APPEARANCE a ON a2sd.appearance_id = a.id " +
					"WHERE " +
					"sg.root_id = ? "; 
	
	public static final String[] COLLADA_IMPLICIT_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID = new String[] {
		COLLADA_IMPLICIT_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID_0 + "AND sg.implicit_geometry IS NULL ORDER BY sg.id", // parents
		COLLADA_IMPLICIT_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID_0 + "AND sg.implicit_geometry IS NOT NULL" // elementary surfaces
	};
	
	public static final String[] COLLADA_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID = new String[] {
			COLLADA_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID_0 + "AND sg.geometry IS NULL ORDER BY sg.id", // parents
			COLLADA_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID_0 + "AND sg.geometry IS NOT NULL" // elementary surfaces
	};

	private static final String BUILDING_PART_GEOMETRY_HIGHLIGHTING_LOD1 =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg, BUILDING b " +
					"WHERE " +
					"b.id = ? " +
					"AND (sg.root_id = b.lod1_solid_id " +
						"OR sg.root_id = b.lod1_multi_surface_id) " +
					"AND sg.geometry IS NOT NULL ";

	private static final String BUILDING_PART_GEOMETRY_HIGHLIGHTING_LOD2 =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + BUILDING_PART_COLLADA_LOD2_ROOT_IDS	+ ")";

	private static final String BUILDING_PART_GEOMETRY_HIGHLIGHTING_LOD3 =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + BUILDING_PART_COLLADA_LOD3_ROOT_IDS + ")";

	private static final String BUILDING_PART_GEOMETRY_HIGHLIGHTING_LOD4 =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" +
					"SELECT geom.gid FROM (SELECT ts.lod4_multi_surface_id as gid " + 
					"FROM THEMATIC_SURFACE ts " + 
					"WHERE " +  
					"ts.building_id = ? " +
					"AND ts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT o.lod4_multi_surface_id as gid " + 
					"FROM THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE o2ts, OPENING o " + 
					"WHERE " +  
					"ts.building_id = ? " +
					"AND ts.lod4_multi_surface_id IS NOT NULL " +
					"AND o2ts.thematic_surface_id = ts.id " +
					"AND o.id = o2ts.opening_id " +
					"UNION " + 
					"SELECT b.lod4_solid_id as gid " + 
					"FROM BUILDING b LEFT JOIN THEMATIC_SURFACE ts ON ts.building_id = b.id " + 
					"WHERE " +  
					"b.id = ? " +
					"AND b.lod4_solid_id IS NOT NULL " +
					"AND ts.lod4_multi_surface_id IS NULL " +
					"UNION " +						
					"SELECT b.lod4_multi_surface_id as gid " + 
					"FROM BUILDING b LEFT JOIN THEMATIC_SURFACE ts ON ts.building_id = b.id " + 
					"WHERE " +  
					"b.id = ? " +
					"AND b.lod4_multi_surface_id IS NOT NULL " +
					"AND ts.lod4_multi_surface_id IS NULL) geom)";


	private static final String BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD2_OR_HIGHER(DatabaseType type) {
		switch (type) {
		case ORACLE:
			return "SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (" +
						"SELECT * FROM (" +
						"SELECT * FROM (" +
				    	"SELECT citydb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
				    	"FROM SURFACE_GEOMETRY sg " +
				    	"WHERE " +
				    	"sg.root_id IN( " +
				    	"SELECT geom.gid FROM (SELECT b.lod<LoD>_multi_surface_id as gid " +
				    	"FROM BUILDING b " +
				    	"WHERE "+
				    	"b.id = ? " +
				    	"AND b.lod<LoD>_multi_surface_id IS NOT NULL " +
				    	"UNION " +
				    	"SELECT b.lod<LoD>_solid_id as gid " +
				    	"FROM BUILDING b " +
				    	"WHERE "+
				    	"b.id = ? " +
				    	"AND b.lod<LoD>_solid_id IS NOT NULL " +				    	
				    	"UNION " +
				    	"SELECT ts.lod<LoD>_multi_surface_id as gid " +
				    	"FROM THEMATIC_SURFACE ts " +
				    	"WHERE "+
				    	"ts.building_id = ? " +
				    	"AND ts.lod<LoD>_multi_surface_id IS NOT NULL) geom "+
				    	") " +
				    	"AND sg.geometry IS NOT NULL" +

						") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'" +
						") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>" +

						") " +
						"GROUP BY mod(rownum, <GROUP_BY_1>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_2>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_3>) " +
						")";
		case POSTGIS:
			return "SELECT ST_Union(get_valid_area.simple_geom) " +
			"FROM (" +
			"SELECT * FROM (" +
			"SELECT * FROM (" +
				        "SELECT ST_Force2D(sg.geometry) AS simple_geom " +
				        "FROM SURFACE_GEOMETRY sg " +
				        "WHERE " +
				        "sg.root_id IN( " +
				        "SELECT b.lod<LoD>_multi_surface_id " +
				        "FROM BUILDING b " +
				        "WHERE b.id = ? " +
				        "AND b.lod<LoD>_multi_surface_id IS NOT NULL " +
				        "UNION " +
				        "SELECT b.lod<LoD>_solid_id " +
				        "FROM BUILDING b " +
				        "WHERE b.id = ? " +
				        "AND b.lod<LoD>_solid_id IS NOT NULL " +
				        "UNION " +
				        "SELECT ts.lod<LoD>_multi_surface_id " +
				        "FROM THEMATIC_SURFACE ts " +
				        "WHERE ts.building_id = ? " +
				        "AND ts.lod<LoD>_multi_surface_id IS NOT NULL "+
				        ") " +
				        "AND sg.geometry IS NOT NULL) AS get_geoms " +
				    	"WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms " +
				    	// ST_Area for WGS84 only works correctly if the geometry is a geography data type
				    	"WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area";
		default:
			return null;
		}
	}

	private static final String BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1(DatabaseType type) {
		switch (type) {
		case ORACLE:
			return "SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (" +
						"SELECT * FROM (" +
						"SELECT * FROM (" +
				    	"SELECT citydb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
				    	"FROM SURFACE_GEOMETRY sg " +
				    	"WHERE " +
				    	"sg.root_id IN( " +				    	
				    	"SELECT geom.gid FROM (SELECT b.lod<LoD>_multi_surface_id as gid " +
				    	"FROM BUILDING b " +
				    	"WHERE "+
				    	"b.id = ? " +
				    	"AND b.lod<LoD>_multi_surface_id IS NOT NULL " +
				    	"UNION " +
				    	"SELECT b.lod<LoD>_solid_id as gid " +
				    	"FROM BUILDING b " +
				    	"WHERE "+
				    	"b.id = ? " +
				    	"AND b.lod<LoD>_solid_id IS NOT NULL) geom "+
				    	") " +
				    	"AND sg.geometry IS NOT NULL" +
						") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'" +
						") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>" +
						") " +
						"GROUP BY mod(rownum, <GROUP_BY_1>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_2>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_3>) " +
						")";
		case POSTGIS:
			return "SELECT ST_Union(get_valid_area.simple_geom) " +
			"FROM (" +
			"SELECT * FROM (" +
			"SELECT * FROM (" +
				        "SELECT ST_Force2D(sg.geometry) AS simple_geom " +
				        "FROM SURFACE_GEOMETRY sg " +
				        "WHERE " +
				        "sg.root_id IN( " +
				        "SELECT b.lod<LoD>_multi_surface_id " +
				        "FROM BUILDING b " +
				        "WHERE b.id = ? " +
				        "AND b.lod<LoD>_multi_surface_id IS NOT NULL " +
				        "UNION " +				        
				        "SELECT b.lod<LoD>_solid_id " +
				        "FROM BUILDING b " +
				        "WHERE b.id = ? " +
				        "AND b.lod<LoD>_solid_id IS NOT NULL " +				        
				        ") " +
				        "AND sg.geometry IS NOT NULL) AS get_geoms " +
				    	"WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms " +
				    	// ST_Area for WGS84 only works correctly if the geometry is a geography data type
				    	"WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area";
		default:
			return null;
		}
	}
	
	private static final String BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD0(DatabaseType type) {
		switch (type) {
		case ORACLE:
			return "SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (" +
						"SELECT * FROM (" +
						"SELECT * FROM (" +
				    	"SELECT sg.geometry AS simple_geom " +
				    	"FROM SURFACE_GEOMETRY sg " +
				    	"WHERE " +
				    	"sg.root_id IN( " +
				    	"SELECT geom.gid FROM (SELECT b.lod0_footprint_id as gid " +
				    	"FROM BUILDING b " +
				    	"WHERE "+
				    	"b.id = ? " +
				    	"AND b.lod0_footprint_id IS NOT NULL " +
				    	"UNION " +
				    	"SELECT b.lod0_roofprint_id as gid " +
				    	"FROM BUILDING b " +
				    	"WHERE "+
				    	"b.id = ? " +
				    	"AND b.lod0_roofprint_id IS NOT NULL) geom "+
				    	") " +
				    	"AND sg.geometry IS NOT NULL" +
						") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'" +
						") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>" +
						") " +
						"GROUP BY mod(rownum, <GROUP_BY_1>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_2>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_3>) " +
						")";
		case POSTGIS:
			return "SELECT ST_Union(get_valid_area.simple_geom) " +
			"FROM (" +
			"SELECT * FROM (" +
			"SELECT * FROM (" +
				        "SELECT sg.geometry AS simple_geom " +
				        "FROM SURFACE_GEOMETRY sg " +
				        "WHERE " +
				        "sg.root_id IN( " +
				        "SELECT b.lod0_footprint_id " +
				        "FROM BUILDING b " +
				        "WHERE b.id = ? " +
				        "AND b.lod0_footprint_id IS NOT NULL " +
				        "UNION " +				        
				        "SELECT b.lod0_roofprint_id " +
				        "FROM BUILDING b " +
				        "WHERE b.id = ? " +
				        "AND b.lod0_roofprint_id IS NOT NULL " +				        
				        ") " +
				        "AND sg.geometry IS NOT NULL) AS get_geoms " +
				    	"WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms " +
				    	// ST_Area for WGS84 only works correctly if the geometry is a geography data type
				    	"WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area";
		default:
			return null;
		}
	}
	public static String getBuildingPartAggregateGeometries (double tolerance,
			int srid2D,
			int lodToExportFrom,
			double groupBy1,
			double groupBy2,
			double groupBy3,
			DatabaseType type) {
		if (lodToExportFrom > 1) {
			return BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD2_OR_HIGHER(type).replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<2D_SRID>", String.valueOf(srid2D))
					.replace("<LoD>", String.valueOf(lodToExportFrom))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));
		}
		else if (lodToExportFrom == 1){
			return BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1(type).replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<2D_SRID>", String.valueOf(srid2D))
					.replace("<LoD>", String.valueOf(lodToExportFrom))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));			
		}
		else {
			return BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD0(type).replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));					
		}
	}



	private static final HashMap<Integer, String> buildingPartQueriesLod4 = new HashMap<Integer, String>();
	static {
		buildingPartQueriesLod4.put(DisplayForm.FOOTPRINT, BUILDING_PART_FOOTPRINT_LOD4);
		buildingPartQueriesLod4.put(DisplayForm.EXTRUDED, BUILDING_PART_FOOTPRINT_LOD4);
		buildingPartQueriesLod4.put(DisplayForm.GEOMETRY, BUILDING_PART_GEOMETRY_LOD4);
		buildingPartQueriesLod4.put(DisplayForm.COLLADA, BUILDING_PART_COLLADA_LOD4_ROOT_IDS);
	}

	private static final HashMap<Integer, String> buildingPartQueriesLod3 = new HashMap<Integer, String>();
	static {
		buildingPartQueriesLod3.put(DisplayForm.FOOTPRINT, BUILDING_PART_FOOTPRINT_LOD3);
		buildingPartQueriesLod3.put(DisplayForm.EXTRUDED, BUILDING_PART_FOOTPRINT_LOD3);
		buildingPartQueriesLod3.put(DisplayForm.GEOMETRY, BUILDING_PART_GEOMETRY_LOD3);
		buildingPartQueriesLod3.put(DisplayForm.COLLADA, BUILDING_PART_COLLADA_LOD3_ROOT_IDS);
	}

	private static final HashMap<Integer, String> buildingPartQueriesLod2 = new HashMap<Integer, String>();
	static {
		buildingPartQueriesLod2.put(DisplayForm.FOOTPRINT, BUILDING_PART_FOOTPRINT_LOD2);
		buildingPartQueriesLod2.put(DisplayForm.EXTRUDED, BUILDING_PART_FOOTPRINT_LOD2);
		buildingPartQueriesLod2.put(DisplayForm.GEOMETRY, BUILDING_PART_GEOMETRY_LOD2);
		buildingPartQueriesLod2.put(DisplayForm.COLLADA, BUILDING_PART_COLLADA_LOD2_ROOT_IDS);
	}

	private static final HashMap<Integer, String> buildingPartQueriesLod1 = new HashMap<Integer, String>();
	static {
		buildingPartQueriesLod1.put(DisplayForm.GEOMETRY, BUILDING_PART_GEOMETRY_LOD1);
		buildingPartQueriesLod1.put(DisplayForm.COLLADA, BUILDING_PART_COLLADA_LOD1_ROOT_IDS);
	}
	
	private static final String BUILDING_PART_FOOTPRINT_LOD0 = 
			"SELECT sg.geometry " +
					"FROM SURFACE_GEOMETRY sg, BUILDING b " +
					"WHERE " +
					"b.id = ? " +
					"AND sg.root_id = b.lod0_footprint_id " +
					"AND sg.geometry IS NOT NULL ";
	
	private static final String BUILDING_PART_ROOFPRINT_LOD0 = 
			"SELECT sg.geometry " +
					"FROM SURFACE_GEOMETRY sg, BUILDING b " +
					"WHERE " +
					"b.id = ? " +
					"AND sg.root_id = b.lod0_roofprint_id " +
					"AND sg.geometry IS NOT NULL ";

	public static String getBuildingPartQuery (int lodToExportFrom, DisplayForm displayForm, Lod0FootprintMode lod0FootprintMode, DatabaseType type) {
		String query = null;
		switch (lodToExportFrom) {
		case 0: 
			if (lod0FootprintMode == Lod0FootprintMode.FOOTPRINT) {
				query = BUILDING_PART_FOOTPRINT_LOD0;
			}
			else if (lod0FootprintMode == Lod0FootprintMode.ROOFPRINT || lod0FootprintMode == Lod0FootprintMode.ROOFPRINT_PRIOR_FOOTPRINT) {
				query = BUILDING_PART_ROOFPRINT_LOD0;
			}			
			break;
		case 1:
			if (displayForm.getForm() == DisplayForm.FOOTPRINT || displayForm.getForm() == DisplayForm.EXTRUDED){
				query = BUILDING_PART_FOOTPRINT_LOD1(type);	
			}
			else 
				query = buildingPartQueriesLod1.get(displayForm.getForm());
			break;
		case 2:
			query = buildingPartQueriesLod2.get(displayForm.getForm());
			break;
		case 3:
			query = buildingPartQueriesLod3.get(displayForm.getForm());
			break;
		case 4:
			query = buildingPartQueriesLod4.get(displayForm.getForm());
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No BuildingPart query found for LoD" + lodToExportFrom);
		}
		return query;
	}

	public static String getBuildingPartHighlightingQuery (int lodToExportFrom) {
		String query = null;
		switch (lodToExportFrom) {
		case 1:
			query = BUILDING_PART_GEOMETRY_HIGHLIGHTING_LOD1;
			break;
		case 2:
			query = BUILDING_PART_GEOMETRY_HIGHLIGHTING_LOD2;
			break;
		case 3:
			query = BUILDING_PART_GEOMETRY_HIGHLIGHTING_LOD3;
			break;
		case 4:
			query = BUILDING_PART_GEOMETRY_HIGHLIGHTING_LOD4;
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No BuildingPart highlighting query found for LoD" + lodToExportFrom);
		}

		//	    	Logger.getInstance().log(LogLevelType.DEBUG, query);
		return query;
	}


	// ----------------------------------------------------------------------
	// 	Bridge QUERIES
	// ----------------------------------------------------------------------

	public static final String BRIDGE_PARTS_FROM_BRIDGE =
			"SELECT id FROM BRIDGE WHERE bridge_root_id = ?";

	private static final String BRIDGE_PART_FOOTPRINT_LOD4 =
			"SELECT sg.geometry " +
					"FROM SURFACE_GEOMETRY sg, BRIDGE_THEMATIC_SURFACE bts " +
					"WHERE " +
					"bts.bridge_id = ? " +
					"AND bts.objectclass_id = '73' " +  // BridgeGroundSurface
					"AND sg.root_id = bts.lod4_multi_surface_id " +
					"AND sg.geometry IS NOT NULL "; 

	
	private static final String BRIDGE_PART_COLLADA_LOD4_ROOT_IDS =
			"SELECT geom.gid FROM (" + 
					// bridge
					"SELECT bts.lod4_multi_surface_id as gid " + 					
					"FROM BRIDGE_THEMATIC_SURFACE bts " + 
					"WHERE " +  
					"bts.bridge_id = ? " +
					"AND bts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT b.lod4_solid_id as gid " + 
					"FROM BRIDGE b LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_id = b.id " + 
					"WHERE " +  
					"b.id = ? " +
					"AND b.lod4_solid_id IS NOT NULL " +
					"AND bts.lod4_multi_surface_id IS NULL " +
					"UNION " +					 
					"SELECT b.lod4_multi_surface_id as gid " + 
					"FROM BRIDGE b LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_id = b.id " + 
					"WHERE " +  
					"b.id = ? " +
					"AND b.lod4_multi_surface_id IS NOT NULL " +
					"AND bts.lod4_multi_surface_id IS NULL "	+	
					"UNION " + 
					// Room
					"SELECT bts.lod4_multi_surface_id as gid " + 
					"FROM BRIDGE_ROOM br, BRIDGE_THEMATIC_SURFACE bts " + 
					"WHERE " +  
					"br.bridge_id = ? " +
					"AND bts.bridge_room_id = br.id " +
					"AND bts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
			        "SELECT br.lod4_solid_id as gid " + 
			        "FROM BRIDGE_ROOM br LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_room_id = br.id " + 
			        "WHERE " +  
	  			    "br.bridge_id = ? " +
				    "AND br.lod4_solid_id IS NOT NULL " +
	  			    "AND bts.lod4_multi_surface_id IS NULL " + 
				    "UNION " + 
			        "SELECT br.lod4_multi_surface_id as gid " + 
			        "FROM BRIDGE_ROOM br LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_room_id = br.id " + 
			        "WHERE " +  
	  			    "br.bridge_id = ? " +
				    "AND br.lod4_multi_surface_id IS NOT NULL " +
	  			    "AND bts.lod4_multi_surface_id IS NULL " + 
				    "UNION " + 
	  			    // Bridge Furniture
					"SELECT bf.lod4_brep_id as gid " + 
					"FROM BRIDGE_ROOM br, BRIDGE_FURNITURE bf " + 
					"WHERE " +  
					"br.bridge_id = ? " +
					"AND bf.bridge_room_id = br.id " +
					"AND bf.lod4_brep_id IS NOT NULL " +
					"UNION " + 			
					// Bridge  Installation
					"SELECT bts.lod4_multi_surface_id as gid " + 
					"FROM BRIDGE_INSTALLATION bi, BRIDGE_THEMATIC_SURFACE bts " + 
					"WHERE bi.bridge_id = ? " +  
					"AND bts.bridge_installation_id = bi.id " +
					"AND bts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT bi.lod4_brep_id as gid " + 
					"FROM BRIDGE_INSTALLATION bi LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_installation_id = bi.id " +
					"WHERE " +  
					"bi.bridge_id = ? " +
					"AND bi.lod4_brep_id IS NOT NULL " +
					"AND bts.lod4_multi_surface_id IS NULL " + 
					"UNION " + 
					// Bridge Construction Element
					"SELECT bts.lod4_multi_surface_id as gid " + 
					"FROM BRIDGE_CONSTR_ELEMENT bce, BRIDGE_THEMATIC_SURFACE bts " + 
					"WHERE bce.bridge_id = ? " +  
					"AND bts.bridge_constr_element_id = bce.id " +
					"AND bts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT bce.lod4_brep_id as gid " + 
					"FROM BRIDGE_CONSTR_ELEMENT bce LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_constr_element_id = bce.id " +
					"WHERE " +  
					"bce.bridge_id = ? " +
					"AND bce.lod4_brep_id IS NOT NULL " +
					"AND bts.lod4_multi_surface_id IS NULL " + 
					"UNION " + 
					// Opening
					"SELECT bo.lod4_multi_surface_id as gid " + 
					"FROM BRIDGE_THEMATIC_SURFACE bts, BRIDGE_OPEN_TO_THEM_SRF botts, BRIDGE_OPENING bo " + 
					"WHERE " +  
					"bts.bridge_id = ? " +
					"AND bts.lod4_multi_surface_id IS NOT NULL " +
					"AND botts.bridge_thematic_surface_id = bts.id " +
					"AND bo.id = botts.bridge_opening_id) geom";


	private static final String BRIDGE_PART_GEOMETRY_LOD4 =
			"SELECT sg.geometry, bts.objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.lod4_multi_surface_id = sg.root_id " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + BRIDGE_PART_COLLADA_LOD4_ROOT_IDS + ")";

	private static final String BRIDGE_PART_FOOTPRINT_LOD3 =
			"SELECT sg.geometry " +
					"FROM SURFACE_GEOMETRY sg, BRIDGE_THEMATIC_SURFACE bts " +
					"WHERE " +
					"bts.bridge_id = ? " +
					"AND bts.objectclass_id = '73' " +
					"AND sg.root_id = bts.lod3_multi_surface_id " +
					"AND sg.geometry IS NOT NULL "; 

	private static final String BRIDGE_PART_COLLADA_LOD3_ROOT_IDS =
			"SELECT geom.gid FROM (" + 
					// Bridge
					"SELECT bts.lod3_multi_surface_id as gid " + 
					"FROM BRIDGE_THEMATIC_SURFACE bts " + 
					"WHERE " +  
					"bts.bridge_id = ? " +
					"AND bts.lod3_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT b.lod3_solid_id as gid " + 
					"FROM BRIDGE b LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_id = b.id " + 
					"WHERE " +  
					"b.id = ? " +
					"AND b.lod3_solid_id IS NOT NULL " +
					"AND bts.lod3_multi_surface_id IS NULL " +
					"UNION " + 
					"SELECT b.lod3_multi_surface_id as gid " + 
					"FROM BRIDGE b LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_id = b.id " + 
					"WHERE " +  
					"b.id = ? " +
					"AND b.lod3_multi_surface_id IS NOT NULL " +
					"AND bts.lod3_multi_surface_id IS NULL " + 
					"UNION " + 
					// Bridge Installation
					"SELECT bts.lod3_multi_surface_id as gid " + 
					"FROM BRIDGE_INSTALLATION bi, BRIDGE_THEMATIC_SURFACE bts " + 
					"WHERE bi.bridge_id = ? " +  
					"AND bts.bridge_installation_id = bi.id " +
					"AND bts.lod3_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT bi.lod3_brep_id as gid " + 
					"FROM BRIDGE_INSTALLATION bi LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_installation_id = bi.id " +
					"WHERE " +  
					"bi.bridge_id = ? " +
					"AND bi.lod3_brep_id IS NOT NULL " +
					"AND bts.lod3_multi_surface_id IS NULL " + 
					"UNION " + 
					// Bridge Construction Element
					"SELECT bts.lod3_multi_surface_id as gid " + 
					"FROM BRIDGE_CONSTR_ELEMENT bce, BRIDGE_THEMATIC_SURFACE bts " + 
					"WHERE bce.bridge_id = ? " +  
					"AND bts.bridge_constr_element_id = bce.id " +
					"AND bts.lod3_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT bce.lod3_brep_id as gid " + 
					"FROM BRIDGE_CONSTR_ELEMENT bce LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_constr_element_id = bce.id " +
					"WHERE " +  
					"bce.bridge_id = ? " +
					"AND bce.lod3_brep_id IS NOT NULL " +
					"AND bts.lod3_multi_surface_id IS NULL " + 
					"UNION " + 
					// Bridge Opening
					"SELECT bo.lod3_multi_surface_id as gid " + 
					"FROM BRIDGE_THEMATIC_SURFACE bts, BRIDGE_OPEN_TO_THEM_SRF botts, BRIDGE_OPENING bo " + 
					"WHERE " +  
					"bts.bridge_id = ? " +
					"AND bts.lod3_multi_surface_id IS NOT NULL " +
					"AND botts.bridge_thematic_surface_id = bts.id " +
					"AND bo.id = botts.bridge_opening_id) geom";

	
	private static final String BRIDGE_PART_GEOMETRY_LOD3 =
			"SELECT sg.geometry, bts.objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.lod3_multi_surface_id = sg.root_id " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + BRIDGE_PART_COLLADA_LOD3_ROOT_IDS	+ ")";
	

	private static final String BRIDGE_PART_FOOTPRINT_LOD2 =
			"SELECT sg.geometry " +
					"FROM SURFACE_GEOMETRY sg, BRIDGE_THEMATIC_SURFACE bts " +
					"WHERE " +
					"bts.bridge_id = ? " +
					"AND bts.objectclass_id = '73' " +
					"AND sg.root_id = bts.lod2_multi_surface_id " +
					"AND sg.geometry IS NOT NULL " +
					"ORDER BY bts.bridge_id";
	
	
	private static final String BRIDGE_PART_COLLADA_LOD2_ROOT_IDS =
			"SELECT geom.gid FROM ( " + 
					// Bridge
					"SELECT bts.lod2_multi_surface_id as gid " + 
					"FROM BRIDGE_THEMATIC_SURFACE bts " + 
					"WHERE " +  
					"bts.bridge_id = ? " +
					"AND bts.lod2_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT b.lod2_multi_surface_id as gid " + 
					"FROM BRIDGE b LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_id = b.id " + 
					"WHERE " +  
					"b.id = ? " +
					"AND b.lod2_multi_surface_id IS NOT NULL " +
					"AND bts.lod2_multi_surface_id IS NULL " +
					"UNION " + 
					"SELECT b.lod2_solid_id as gid " + 
					"FROM BRIDGE b LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_id = b.id " + 
					"WHERE " +  
					"b.id = ? " +
					"AND b.lod2_solid_id IS NOT NULL " +
					"AND bts.lod2_multi_surface_id IS NULL " +					
					"UNION " +			
					// Bridge Construction Element
					"SELECT bts.lod2_multi_surface_id as gid " + 
					"FROM BRIDGE_CONSTR_ELEMENT bce, BRIDGE_THEMATIC_SURFACE bts " + 
					"WHERE bce.bridge_id = ? " +  
					"AND bts.bridge_constr_element_id = bce.id " +
					"AND bts.lod2_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT bce.lod2_brep_id as gid " + 
					"FROM BRIDGE_CONSTR_ELEMENT bce LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_constr_element_id = bce.id " +
					"WHERE " +  
					"bce.bridge_id = ? " +
					"AND bce.lod2_brep_id IS NOT NULL " +
					"AND bts.lod2_multi_surface_id IS NULL " + 
					"UNION " + 
					// Bridge Installation	
					"SELECT bts.lod2_multi_surface_id as gid " + 
					"FROM BRIDGE_INSTALLATION bi, BRIDGE_THEMATIC_SURFACE bts " + 
					"WHERE bi.bridge_id = ? " +  
					"AND bts.bridge_installation_id = bi.id " +
					"AND bts.lod2_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT bi.lod2_brep_id as gid " + 
					"FROM BRIDGE_INSTALLATION bi LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_installation_id = bi.id " +
					"WHERE " +  
					"bi.bridge_id = ? " +
					"AND bi.lod2_brep_id IS NOT NULL " +
					"AND bts.lod2_multi_surface_id IS NULL) geom";
	
	
	private static final String BRIDGE_PART_GEOMETRY_LOD2 =
			"SELECT sg.geometry, bts.objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.lod2_multi_surface_id = sg.root_id " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + BRIDGE_PART_COLLADA_LOD2_ROOT_IDS	+ ")";

	
	private static final String BRIDGE_PART_FOOTPRINT_LOD1(DatabaseType type) {		
		switch (type) {
		case ORACLE:
			return BRIDGE_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1(type).replace("<TOLERANCE>", "0.001")
					.replace("<2D_SRID>", "(SELECT SRID FROM DATABASE_SRS)")
					.replace("<LoD>", "1")
					.replace("<GROUP_BY_1>", "256")
					.replace("<GROUP_BY_2>", "64")
					.replace("<GROUP_BY_3>", "16");
		case POSTGIS:		
			return BRIDGE_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1(type).replace("<TOLERANCE>", "0.001")
					.replace("<LoD>", "1");			
		default:
			return null;
		}
	}	
	
	private static final String BRIDGE_PART_COLLADA_LOD1_ROOT_IDS =
			"SELECT geom.gid FROM (SELECT b.lod1_multi_surface_id as gid " +			
					"FROM BRIDGE b " +
					"WHERE " +
					"b.id = ? " +
					"AND b.lod1_multi_surface_id IS NOT NULL " + 
					"UNION " + 
					"SELECT b.lod1_solid_id as gid " +			
					"FROM BRIDGE b " +
					"WHERE " +
					"b.id = ? " +
					"AND b.lod1_solid_id IS NOT NULL) geom"; 
	
	
	private static final String BRIDGE_PART_GEOMETRY_LOD1 =
			"SELECT sg.geometry, NULL as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + BRIDGE_PART_COLLADA_LOD1_ROOT_IDS	+ ")";

	private static final String BRIDGE_PART_GEOMETRY_HIGHLIGHTING_LOD1 =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg, BRIDGE b " +
					"WHERE " +
					"b.id = ? " +
					"AND (sg.root_id = b.lod1_solid_id " +
						"OR sg.root_id = b.lod1_multi_surface_id) " +
					"AND sg.geometry IS NOT NULL ";

	private static final String BRIDGE_PART_GEOMETRY_HIGHLIGHTING_LOD2 =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + BRIDGE_PART_COLLADA_LOD2_ROOT_IDS	+ ")";

	private static final String BRIDGE_PART_GEOMETRY_HIGHLIGHTING_LOD3 =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + BRIDGE_PART_COLLADA_LOD3_ROOT_IDS + ")";

	private static final String BRIDGE_PART_GEOMETRY_HIGHLIGHTING_LOD4 =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" +
					"SELECT geom.gid FROM (SELECT bts.lod4_multi_surface_id as gid " + 
					"FROM BRIDGE_THEMATIC_SURFACE bts " + 
					"WHERE " +  
					"bts.bridge_id = ? " +
					"AND bts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT bo.lod4_multi_surface_id as gid " + 
					"FROM BRIDGE_THEMATIC_SURFACE bts, BRIDGE_OPEN_TO_THEM_SRF botts, BRIDGE_OPENING bo " + 
					"WHERE " +  
					"bts.bridge_id = ? " +
					"AND bts.lod4_multi_surface_id IS NOT NULL " +
					"AND botts.bridge_thematic_surface_id = bts.id " +
					"AND bo.id = botts.bridge_opening_id " +
					"UNION " + 
					"SELECT b.lod4_solid_id as gid " + 
					"FROM bridge b LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_id = b.id " + 
					"WHERE " +  
					"b.id = ? " +
					"AND b.lod4_solid_id IS NOT NULL " +
					"AND bts.lod4_multi_surface_id IS NULL " +
					"UNION " +						
					"SELECT b.lod4_multi_surface_id as gid " + 
					"FROM BRIDGE b LEFT JOIN BRIDGE_THEMATIC_SURFACE bts ON bts.bridge_id = b.id " + 
					"WHERE " +  
					"b.id = ? " +
					"AND b.lod4_multi_surface_id IS NOT NULL " +
					"AND bts.lod4_multi_surface_id IS NULL) geom)";


	private static final String BRIDGE_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD2_OR_HIGHER(DatabaseType type) {
		switch (type) {
		case ORACLE:
			return "SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (" +
						"SELECT * FROM (" +
						"SELECT * FROM (" +
				    	"SELECT citydb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
				    	"FROM SURFACE_GEOMETRY sg " +
				    	"WHERE " +
				    	"sg.root_id IN( " +
				    	"SELECT geom.gid FROM (SELECT b.lod<LoD>_multi_surface_id as gid " +
				    	"FROM BRIDGE b " +
				    	"WHERE "+
				    	"b.id = ? " +
				    	"AND b.lod<LoD>_multi_surface_id IS NOT NULL " +
				    	"UNION " +
				    	"SELECT b.lod<LoD>_solid_id as gid " +
				    	"FROM BRIDGE b " +
				    	"WHERE "+
				    	"b.id = ? " +
				    	"AND b.lod<LoD>_solid_id IS NOT NULL " +				    	
				    	"UNION " +
				    	"SELECT bts.lod<LoD>_multi_surface_id as gid " +
				    	"FROM BRIDGE_THEMATIC_SURFACE bts " +
				    	"WHERE "+
				    	"bts.bridge_id = ? " +
				    	"AND bts.lod<LoD>_multi_surface_id IS NOT NULL) geom "+
				    	") " +
				    	"AND sg.geometry IS NOT NULL" +

						") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'" +
						") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>" +

						") " +
						"GROUP BY mod(rownum, <GROUP_BY_1>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_2>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_3>) " +
						")";
		case POSTGIS:
			return "SELECT ST_Union(get_valid_area.simple_geom) " +
			"FROM (" +
			"SELECT * FROM (" +
			"SELECT * FROM (" +
				        "SELECT ST_Force2D(sg.geometry) AS simple_geom " +
				        "FROM SURFACE_GEOMETRY sg " +
				        "WHERE " +
				        "sg.root_id IN( " +
				        "SELECT b.lod<LoD>_multi_surface_id " +
				        "FROM BRIDGE b " +
				        "WHERE b.id = ? " +
				        "AND b.lod<LoD>_multi_surface_id IS NOT NULL " +
				        "UNION " +
				        "SELECT b.lod<LoD>_solid_id " +
				        "FROM BRIDGE b " +
				        "WHERE b.id = ? " +
				        "AND b.lod<LoD>_solid_id IS NOT NULL " +
				        "UNION " +
				        "SELECT bts.lod<LoD>_multi_surface_id " +
				        "FROM BRIDGE_THEMATIC_SURFACE bts " +
				        "WHERE bts.bridge_id = ? " +
				        "AND bts.lod<LoD>_multi_surface_id IS NOT NULL "+
				        ") " +
				        "AND sg.geometry IS NOT NULL) AS get_geoms " +
				    	"WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms " +
				    	// ST_Area for WGS84 only works correctly if the geometry is a geography data type
				    	"WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area";
		default:
			return null;
		}
	}

	private static final String BRIDGE_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1(DatabaseType type) {
		switch (type) {
		case ORACLE:
			return "SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (" +
						"SELECT * FROM (" +
						"SELECT * FROM (" +
				    	"SELECT citydb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
				    	"FROM SURFACE_GEOMETRY sg " +
				    	"WHERE " +
				    	"sg.root_id IN( " +				    	
				    	"SELECT geom.gid FROM (SELECT b.lod<LoD>_multi_surface_id as gid " +
				    	"FROM BRIDGE b " +
				    	"WHERE "+
				    	"b.id = ? " +
				    	"AND b.lod<LoD>_multi_surface_id IS NOT NULL " +
				    	"UNION " +
				    	"SELECT b.lod<LoD>_solid_id as gid " +
				    	"FROM BRIDGE b " +
				    	"WHERE "+
				    	"b.id = ? " +
				    	"AND b.lod<LoD>_solid_id IS NOT NULL) geom "+
				    	") " +
				    	"AND sg.geometry IS NOT NULL" +
						") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'" +
						") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>" +
						") " +
						"GROUP BY mod(rownum, <GROUP_BY_1>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_2>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_3>) " +
						")";
		case POSTGIS:
			return "SELECT ST_Union(get_valid_area.simple_geom) " +
			"FROM (" +
			"SELECT * FROM (" +
			"SELECT * FROM (" +
				        "SELECT ST_Force2D(sg.geometry) AS simple_geom " +
				        "FROM SURFACE_GEOMETRY sg " +
				        "WHERE " +
				        "sg.root_id IN( " +
				        "SELECT b.lod<LoD>_multi_surface_id " +
				        "FROM BRIDGE b " +
				        "WHERE b.id = ? " +
				        "AND b.lod<LoD>_multi_surface_id IS NOT NULL " +
				        "UNION " +				        
				        "SELECT b.lod<LoD>_solid_id " +
				        "FROM BRIDGE b " +
				        "WHERE b.id = ? " +
				        "AND b.lod<LoD>_solid_id IS NOT NULL " +				        
				        ") " +
				        "AND sg.geometry IS NOT NULL) AS get_geoms " +
				    	"WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms " +
				    	// ST_Area for WGS84 only works correctly if the geometry is a geography data type
				    	"WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area";
		default:
			return null;
		}
	}
	
	public static String getBridgePartAggregateGeometries (double tolerance,
			int srid2D,
			int lodToExportFrom,
			double groupBy1,
			double groupBy2,
			double groupBy3,
			DatabaseType type) {
		if (lodToExportFrom > 1) {
			return BRIDGE_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD2_OR_HIGHER(type).replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<2D_SRID>", String.valueOf(srid2D))
					.replace("<LoD>", String.valueOf(lodToExportFrom))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));
		}
		else {
			return BRIDGE_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1(type).replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<2D_SRID>", String.valueOf(srid2D))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));					
		}
	}



	private static final HashMap<Integer, String> bridgePartQueriesLod4 = new HashMap<Integer, String>();
	static {
		bridgePartQueriesLod4.put(DisplayForm.FOOTPRINT, BRIDGE_PART_FOOTPRINT_LOD4);
		bridgePartQueriesLod4.put(DisplayForm.EXTRUDED, BRIDGE_PART_FOOTPRINT_LOD4);
		bridgePartQueriesLod4.put(DisplayForm.GEOMETRY, BRIDGE_PART_GEOMETRY_LOD4);
		bridgePartQueriesLod4.put(DisplayForm.COLLADA, BRIDGE_PART_COLLADA_LOD4_ROOT_IDS);
	}

	private static final HashMap<Integer, String> bridgePartQueriesLod3 = new HashMap<Integer, String>();
	static {
		bridgePartQueriesLod3.put(DisplayForm.FOOTPRINT, BRIDGE_PART_FOOTPRINT_LOD3);
		bridgePartQueriesLod3.put(DisplayForm.EXTRUDED, BRIDGE_PART_FOOTPRINT_LOD3);
		bridgePartQueriesLod3.put(DisplayForm.GEOMETRY, BRIDGE_PART_GEOMETRY_LOD3);
		bridgePartQueriesLod3.put(DisplayForm.COLLADA, BRIDGE_PART_COLLADA_LOD3_ROOT_IDS);
	}

	private static final HashMap<Integer, String> bridgePartQueriesLod2 = new HashMap<Integer, String>();
	static {
		bridgePartQueriesLod2.put(DisplayForm.FOOTPRINT, BRIDGE_PART_FOOTPRINT_LOD2);
		bridgePartQueriesLod2.put(DisplayForm.EXTRUDED, BRIDGE_PART_FOOTPRINT_LOD2);
		bridgePartQueriesLod2.put(DisplayForm.GEOMETRY, BRIDGE_PART_GEOMETRY_LOD2);
		bridgePartQueriesLod2.put(DisplayForm.COLLADA, BRIDGE_PART_COLLADA_LOD2_ROOT_IDS);
	}

	private static final HashMap<Integer, String> bridgePartQueriesLod1 = new HashMap<Integer, String>();
	static {
		bridgePartQueriesLod1.put(DisplayForm.GEOMETRY, BRIDGE_PART_GEOMETRY_LOD1);
		bridgePartQueriesLod1.put(DisplayForm.COLLADA, BRIDGE_PART_COLLADA_LOD1_ROOT_IDS);
	}
	
	public static String getBridgePartQuery (int lodToExportFrom, DisplayForm displayForm, DatabaseType type) {
		String query = null;
		switch (lodToExportFrom) {
		case 1:
			if (displayForm.getForm() == DisplayForm.FOOTPRINT || displayForm.getForm() == DisplayForm.EXTRUDED){
				query = BRIDGE_PART_FOOTPRINT_LOD1(type);		
			}
			else 
				query = bridgePartQueriesLod1.get(displayForm.getForm());
			break;
		case 2:
			query = bridgePartQueriesLod2.get(displayForm.getForm());
			break;
		case 3:
			query = bridgePartQueriesLod3.get(displayForm.getForm());
			break;
		case 4:
			query = bridgePartQueriesLod4.get(displayForm.getForm());
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No BuildingPart query found for LoD" + lodToExportFrom);
		}
		return query;
	}

	public static String getBridgePartHighlightingQuery (int lodToExportFrom) {
		String query = null;
		switch (lodToExportFrom) {
		case 1:
			query = BRIDGE_PART_GEOMETRY_HIGHLIGHTING_LOD1;
			break;
		case 2:
			query = BRIDGE_PART_GEOMETRY_HIGHLIGHTING_LOD2;
			break;
		case 3:
			query = BRIDGE_PART_GEOMETRY_HIGHLIGHTING_LOD3;
			break;
		case 4:
			query = BRIDGE_PART_GEOMETRY_HIGHLIGHTING_LOD4;
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No BridgePart highlighting query found for LoD" + lodToExportFrom);
		}
		return query;
	}
	// ----------------------------------------------------------------------
	// 	Tunnel QUERIES
	// ----------------------------------------------------------------------

	public static final String TUNNEL_PARTS_FROM_TUNNEL =
			"SELECT id FROM TUNNEL WHERE tunnel_root_id = ?";

	private static final String TUNNEL_PART_FOOTPRINT_LOD4 =
			"SELECT sg.geometry " +
					"FROM SURFACE_GEOMETRY sg, TUNNEL_THEMATIC_SURFACE tts " +
					"WHERE " +
					"tts.tunnel_id = ? " +
					"AND tts.objectclass_id = '94' " +  // TunnelGroundSurface
					"AND sg.root_id = tts.lod4_multi_surface_id " +
					"AND sg.geometry IS NOT NULL "; 

	
	private static final String TUNNEL_PART_COLLADA_LOD4_ROOT_IDS =
			"SELECT geom.gid FROM (" + 
					// tunnel
					"SELECT tts.lod4_multi_surface_id as gid " + 					
					"FROM TUNNEL_THEMATIC_SURFACE tts " + 
					"WHERE " +  
					"tts.tunnel_id = ? " +
					"AND tts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT t.lod4_solid_id as gid " + 
					"FROM TUNNEL t LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_id = t.id " + 
					"WHERE " +  
					"t.id = ? " +
					"AND t.lod4_solid_id IS NOT NULL " +
					"AND tts.lod4_multi_surface_id IS NULL " +
					"UNION " +					 
					"SELECT t.lod4_multi_surface_id as gid " + 
					"FROM TUNNEL t LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_id = t.id " + 
					"WHERE " +  
					"t.id = ? " +
					"AND t.lod4_multi_surface_id IS NOT NULL " +
					"AND tts.lod4_multi_surface_id IS NULL "	+	
					"UNION " + 
					// Tunnel Hollow Space
					"SELECT tts.lod4_multi_surface_id as gid " + 
					"FROM TUNNEL_HOLLOW_SPACE ths, TUNNEL_THEMATIC_SURFACE tts " + 
					"WHERE " +  
					"ths.tunnel_id = ? " +
					"AND tts.tunnel_hollow_space_id = ths.id " +
					"AND tts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
			        "SELECT ths.lod4_solid_id as gid " + 
			        "FROM TUNNEL_HOLLOW_SPACE ths LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_hollow_space_id = ths.id " + 
			        "WHERE " +  
	  			    "ths.tunnel_id = ? " +
				    "AND ths.lod4_solid_id IS NOT NULL " +
	  			    "AND tts.lod4_multi_surface_id IS NULL " + 
				    "UNION " + 
			        "SELECT ths.lod4_multi_surface_id as gid " + 
			        "FROM TUNNEL_HOLLOW_SPACE ths LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_hollow_space_id = ths.id " + 
			        "WHERE " +  
	  			    "ths.tunnel_id = ? " +
				    "AND ths.lod4_multi_surface_id IS NOT NULL " +
	  			    "AND tts.lod4_multi_surface_id IS NULL " + 
				    "UNION " + 
	  			    // Tunnel Furniture
					"SELECT tf.lod4_brep_id as gid " + 
					"FROM TUNNEL_HOLLOW_SPACE ths, TUNNEL_FURNITURE tf " + 
					"WHERE " +  
					"ths.tunnel_id = ? " +
					"AND tf.tunnel_hollow_space_id = ths.id " +
					"AND tf.lod4_brep_id IS NOT NULL " +
					"UNION " + 			
					// Tunnel  Installation
					"SELECT tts.lod4_multi_surface_id as gid " + 
					"FROM TUNNEL_INSTALLATION ti, TUNNEL_THEMATIC_SURFACE tts " + 
					"WHERE ti.tunnel_id = ? " +  
					"AND tts.tunnel_installation_id = ti.id " +
					"AND tts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT ti.lod4_brep_id as gid " + 
					"FROM TUNNEL_INSTALLATION ti LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_installation_id = ti.id " +
					"WHERE " +  
					"ti.tunnel_id = ? " +
					"AND ti.lod4_brep_id IS NOT NULL " +
					"AND tts.lod4_multi_surface_id IS NULL " + 
					"UNION " + 
					// Opening
					"SELECT ot.lod4_multi_surface_id as gid " + 
					"FROM TUNNEL_THEMATIC_SURFACE tts, TUNNEL_OPEN_TO_THEM_SRF totts, TUNNEL_OPENING ot " +  // "to" is conflict with the SQL keyword
					"WHERE " +  
					"tts.tunnel_id = ? " +
					"AND tts.lod4_multi_surface_id IS NOT NULL " +
					"AND totts.tunnel_thematic_surface_id = tts.id " +
					"AND ot.id = totts.tunnel_opening_id) geom";


	private static final String TUNNEL_PART_GEOMETRY_LOD4 =
			"SELECT sg.geometry, tts.objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.lod4_multi_surface_id = sg.root_id " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + TUNNEL_PART_COLLADA_LOD4_ROOT_IDS + ")";

	private static final String TUNNEL_PART_FOOTPRINT_LOD3 =
			"SELECT sg.geometry " +
					"FROM SURFACE_GEOMETRY sg, TUNNEL_THEMATIC_SURFACE tts " +
					"WHERE " +
					"tts.tunnel_id = ? " +
					"AND tts.objectclass_id = '94' " +
					"AND sg.root_id = tts.lod3_multi_surface_id " +
					"AND sg.geometry IS NOT NULL "; 

	private static final String TUNNEL_PART_COLLADA_LOD3_ROOT_IDS =
			"SELECT geom.gid FROM (" + 
					// Tunnel
					"SELECT tts.lod3_multi_surface_id as gid " + 
					"FROM TUNNEL_THEMATIC_SURFACE tts " + 
					"WHERE " +  
					"tts.tunnel_id = ? " +
					"AND tts.lod3_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT t.lod3_solid_id as gid " + 
					"FROM TUNNEL t LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_id = t.id " + 
					"WHERE " +  
					"t.id = ? " +
					"AND t.lod3_solid_id IS NOT NULL " +
					"AND tts.lod3_multi_surface_id IS NULL " +
					"UNION " + 
					"SELECT t.lod3_multi_surface_id as gid " + 
					"FROM TUNNEL t LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_id = t.id " + 
					"WHERE " +  
					"t.id = ? " +
					"AND t.lod3_multi_surface_id IS NOT NULL " +
					"AND tts.lod3_multi_surface_id IS NULL " + 
					"UNION " + 
					// Tunnel Installation
					"SELECT tts.lod3_multi_surface_id as gid " + 
					"FROM TUNNEL_INSTALLATION ti, TUNNEL_THEMATIC_SURFACE tts " + 
					"WHERE ti.tunnel_id = ? " +  
					"AND tts.tunnel_installation_id = ti.id " +
					"AND tts.lod3_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT ti.lod3_brep_id as gid " + 
					"FROM TUNNEL_INSTALLATION ti LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_installation_id = ti.id " +
					"WHERE " +  
					"ti.tunnel_id = ? " +
					"AND ti.lod3_brep_id IS NOT NULL " +
					"AND tts.lod3_multi_surface_id IS NULL " + 
					"UNION " + 
					// Tunnel Opening
					"SELECT ot.lod3_multi_surface_id as gid " + 
					"FROM TUNNEL_THEMATIC_SURFACE tts, TUNNEL_OPEN_TO_THEM_SRF totts, TUNNEL_OPENING ot " + 
					"WHERE " +  
					"tts.tunnel_id = ? " +
					"AND tts.lod3_multi_surface_id IS NOT NULL " +
					"AND totts.tunnel_thematic_surface_id = tts.id " +
					"AND ot.id = totts.tunnel_opening_id) geom";

	
	private static final String TUNNEL_PART_GEOMETRY_LOD3 =
			"SELECT sg.geometry, tts.objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.lod3_multi_surface_id = sg.root_id " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + TUNNEL_PART_COLLADA_LOD3_ROOT_IDS	+ ")";
	

	private static final String TUNNEL_PART_FOOTPRINT_LOD2 =
			"SELECT sg.geometry " +
					"FROM SURFACE_GEOMETRY sg, TUNNEL_THEMATIC_SURFACE tts " +
					"WHERE " +
					"tts.tunnel_id = ? " +
					"AND tts.objectclass_id = '94' " +
					"AND sg.root_id = tts.lod2_multi_surface_id " +
					"AND sg.geometry IS NOT NULL " +
					"ORDER BY tts.tunnel_id";
	
	
	private static final String TUNNEL_PART_COLLADA_LOD2_ROOT_IDS =
			"SELECT geom.gid FROM ( " + 
					// Tunnel
					"SELECT tts.lod2_multi_surface_id as gid " + 
					"FROM TUNNEL_THEMATIC_SURFACE tts " + 
					"WHERE " +  
					"tts.tunnel_id = ? " +
					"AND tts.lod2_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT t.lod2_multi_surface_id as gid " + 
					"FROM TUNNEL t LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_id = t.id " + 
					"WHERE " +  
					"t.id = ? " +
					"AND t.lod2_multi_surface_id IS NOT NULL " +
					"AND tts.lod2_multi_surface_id IS NULL " +
					"UNION " + 
					"SELECT t.lod2_solid_id as gid " + 
					"FROM TUNNEL t LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_id = t.id " + 
					"WHERE " +  
					"t.id = ? " +
					"AND t.lod2_solid_id IS NOT NULL " +
					"AND tts.lod2_multi_surface_id IS NULL " +					
					"UNION " +			
					// Tunnel Installation	
					"SELECT tts.lod2_multi_surface_id as gid " + 
					"FROM TUNNEL_INSTALLATION ti, TUNNEL_THEMATIC_SURFACE tts " + 
					"WHERE ti.tunnel_id = ? " +  
					"AND tts.tunnel_installation_id = ti.id " +
					"AND tts.lod2_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT ti.lod2_brep_id as gid " + 
					"FROM TUNNEL_INSTALLATION ti LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_installation_id = ti.id " +
					"WHERE " +  
					"ti.tunnel_id = ? " +
					"AND ti.lod2_brep_id IS NOT NULL " +
					"AND tts.lod2_multi_surface_id IS NULL) geom";
	
	
	private static final String TUNNEL_PART_GEOMETRY_LOD2 =
			"SELECT sg.geometry, tts.objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.lod2_multi_surface_id = sg.root_id " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + TUNNEL_PART_COLLADA_LOD2_ROOT_IDS	+ ")";

	
	private static final String TUNNEL_PART_FOOTPRINT_LOD1(DatabaseType type) {		
		switch (type) {
		case ORACLE:
			return TUNNEL_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1(type).replace("<TOLERANCE>", "0.001")
					.replace("<2D_SRID>", "(SELECT SRID FROM DATABASE_SRS)")
					.replace("<LoD>", "1")
					.replace("<GROUP_BY_1>", "256")
					.replace("<GROUP_BY_2>", "64")
					.replace("<GROUP_BY_3>", "16");
		case POSTGIS:		
			return TUNNEL_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1(type).replace("<TOLERANCE>", "0.001")
					.replace("<LoD>", "1");			
		default:
			return null;
		}
	}	
	
	private static final String TUNNEL_PART_COLLADA_LOD1_ROOT_IDS =
			"SELECT geom.gid FROM (SELECT t.lod1_multi_surface_id as gid " +			
					"FROM TUNNEL t " +
					"WHERE " +
					"t.id = ? " +
					"AND t.lod1_multi_surface_id IS NOT NULL " + 
					"UNION " + 
					"SELECT t.lod1_solid_id as gid " +			
					"FROM TUNNEL t " +
					"WHERE " +
					"t.id = ? " +
					"AND t.lod1_solid_id IS NOT NULL) geom"; 
	
	
	private static final String TUNNEL_PART_GEOMETRY_LOD1 =
			"SELECT sg.geometry, NULL as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + TUNNEL_PART_COLLADA_LOD1_ROOT_IDS	+ ")";

	private static final String TUNNEL_PART_GEOMETRY_HIGHLIGHTING_LOD1 =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg, TUNNEL t " +
					"WHERE " +
					"t.id = ? " +
					"AND (sg.root_id = t.lod1_solid_id " +
						"OR sg.root_id = t.lod1_multi_surface_id) " +
					"AND sg.geometry IS NOT NULL ";

	private static final String TUNNEL_PART_GEOMETRY_HIGHLIGHTING_LOD2 =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + TUNNEL_PART_COLLADA_LOD2_ROOT_IDS	+ ")";

	private static final String TUNNEL_PART_GEOMETRY_HIGHLIGHTING_LOD3 =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" + TUNNEL_PART_COLLADA_LOD3_ROOT_IDS + ")";

	private static final String TUNNEL_PART_GEOMETRY_HIGHLIGHTING_LOD4 =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE " +
					"sg.geometry IS NOT NULL " +
					"AND sg.root_id IN (" +
					"SELECT geom.gid FROM (SELECT tts.lod4_multi_surface_id as gid " + 
					"FROM TUNNEL_THEMATIC_SURFACE tts " + 
					"WHERE " +  
					"tts.tunnel_id = ? " +
					"AND tts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT to.lod4_multi_surface_id as gid " + 
					"FROM TUNNEL_THEMATIC_SURFACE tts, TUNNEL_OPEN_TO_THEM_SURF totts, TUNNEL_OPENING to " + 
					"WHERE " +  
					"tts.tunnel_id = ? " +
					"AND tts.lod4_multi_surface_id IS NOT NULL " +
					"AND totts.tunnel_thematic_surface_id = tts.id " +
					"AND to.id = totts.tunnel_opening_id " +
					"UNION " + 
					"SELECT t.lod4_solid_id as gid " + 
					"FROM tunnel t LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_id = t.id " + 
					"WHERE " +  
					"t.id = ? " +
					"AND t.lod4_solid_id IS NOT NULL " +
					"AND tts.lod4_multi_surface_id IS NULL " +
					"UNION " +						
					"SELECT t.lod4_multi_surface_id as gid " + 
					"FROM TUNNEL t LEFT JOIN TUNNEL_THEMATIC_SURFACE tts ON tts.tunnel_id = t.id " + 
					"WHERE " +  
					"t.id = ? " +
					"AND t.lod4_multi_surface_id IS NOT NULL " +
					"AND tts.lod4_multi_surface_id IS NULL) geom)";


	private static final String TUNNEL_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD2_OR_HIGHER(DatabaseType type) {
		switch (type) {
		case ORACLE:
			return "SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (" +
						"SELECT * FROM (" +
						"SELECT * FROM (" +
				    	"SELECT citydb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
				    	"FROM SURFACE_GEOMETRY sg " +
				    	"WHERE " +
				    	"sg.root_id IN( " +
				    	"SELECT geom.gid FROM (SELECT t.lod<LoD>_multi_surface_id as gid " +
				    	"FROM TUNNEL t " +
				    	"WHERE "+
				    	"t.id = ? " +
				    	"AND t.lod<LoD>_multi_surface_id IS NOT NULL " +
				    	"UNION " +
				    	"SELECT t.lod<LoD>_solid_id as gid " +
				    	"FROM TUNNEL t " +
				    	"WHERE "+
				    	"t.id = ? " +
				    	"AND t.lod<LoD>_solid_id IS NOT NULL " +				    	
				    	"UNION " +
				    	"SELECT tts.lod<LoD>_multi_surface_id as gid " +
				    	"FROM TUNNEL_THEMATIC_SURFACE tts " +
				    	"WHERE "+
				    	"tts.tunnel_id = ? " +
				    	"AND tts.lod<LoD>_multi_surface_id IS NOT NULL) geom "+
				    	") " +
				    	"AND sg.geometry IS NOT NULL" +

						") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'" +
						") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>" +

						") " +
						"GROUP BY mod(rownum, <GROUP_BY_1>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_2>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_3>) " +
						")";
		case POSTGIS:
			return "SELECT ST_Union(get_valid_area.simple_geom) " +
			"FROM (" +
			"SELECT * FROM (" +
			"SELECT * FROM (" +
				        "SELECT ST_Force2D(sg.geometry) AS simple_geom " +
				        "FROM SURFACE_GEOMETRY sg " +
				        "WHERE " +
				        "sg.root_id IN( " +
				        "SELECT t.lod<LoD>_multi_surface_id " +
				        "FROM TUNNEL t " +
				        "WHERE t.id = ? " +
				        "AND t.lod<LoD>_multi_surface_id IS NOT NULL " +
				        "UNION " +
				        "SELECT t.lod<LoD>_solid_id " +
				        "FROM TUNNEL t " +
				        "WHERE t.id = ? " +
				        "AND t.lod<LoD>_solid_id IS NOT NULL " +
				        "UNION " +
				        "SELECT tts.lod<LoD>_multi_surface_id " +
				        "FROM TUNNEL_THEMATIC_SURFACE tts " +
				        "WHERE tts.tunnel_id = ? " +
				        "AND tts.lod<LoD>_multi_surface_id IS NOT NULL "+
				        ") " +
				        "AND sg.geometry IS NOT NULL) AS get_geoms " +
				    	"WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms " +
				    	// ST_Area for WGS84 only works correctly if the geometry is a geography data type
				    	"WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area";
		default:
			return null;
		}
	}

	private static final String TUNNEL_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1(DatabaseType type) {
		switch (type) {
		case ORACLE:
			return "SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (" +
						"SELECT * FROM (" +
						"SELECT * FROM (" +
				    	"SELECT citydb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
				    	"FROM SURFACE_GEOMETRY sg " +
				    	"WHERE " +
				    	"sg.root_id IN( " +				    	
				    	"SELECT geom.gid FROM (SELECT t.lod<LoD>_multi_surface_id as gid " +
				    	"FROM TUNNEL t  " +
				    	"WHERE "+
				    	"t.id = ? " +
				    	"AND t.lod<LoD>_multi_surface_id IS NOT NULL " +
				    	"UNION " +
				    	"SELECT t.lod<LoD>_solid_id as gid " +
				    	"FROM TUNNEL t " +
				    	"WHERE "+
				    	"t.id = ? " +
				    	"AND t.lod<LoD>_solid_id IS NOT NULL) geom "+
				    	") " +
				    	"AND sg.geometry IS NOT NULL" +
						") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'" +
						") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>" +
						") " +
						"GROUP BY mod(rownum, <GROUP_BY_1>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_2>) " +
						") " +
						"GROUP BY mod (rownum, <GROUP_BY_3>) " +
						")";
		case POSTGIS:
			return "SELECT ST_Union(get_valid_area.simple_geom) " +
			"FROM (" +
			"SELECT * FROM (" +
			"SELECT * FROM (" +
				        "SELECT ST_Force2D(sg.geometry) AS simple_geom " +
				        "FROM SURFACE_GEOMETRY sg " +
				        "WHERE " +
				        "sg.root_id IN( " +
				        "SELECT t.lod<LoD>_multi_surface_id " +
				        "FROM TUNNEL t " +
				        "WHERE t.id = ? " +
				        "AND t.lod<LoD>_multi_surface_id IS NOT NULL " +
				        "UNION " +				        
				        "SELECT t.lod<LoD>_solid_id " +
				        "FROM TUNNEL t " +
				        "WHERE t.id = ? " +
				        "AND t.lod<LoD>_solid_id IS NOT NULL " +				        
				        ") " +
				        "AND sg.geometry IS NOT NULL) AS get_geoms " +
				    	"WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms " +
				    	// ST_Area for WGS84 only works correctly if the geometry is a geography data type
				    	"WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area";
		default:
			return null;
		}
	}
	

	public static String getTunnelPartAggregateGeometries (double tolerance,
			int srid2D,
			int lodToExportFrom,
			double groupBy1,
			double groupBy2,
			double groupBy3,
			DatabaseType type) {
		if (lodToExportFrom > 1) {
			return TUNNEL_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD2_OR_HIGHER(type).replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<2D_SRID>", String.valueOf(srid2D))
					.replace("<LoD>", String.valueOf(lodToExportFrom))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));
		}
		else {
			return TUNNEL_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1(type).replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<2D_SRID>", String.valueOf(srid2D))
					.replace("<LoD>", String.valueOf(lodToExportFrom))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));					
		}
	}



	private static final HashMap<Integer, String> tunnelPartQueriesLod4 = new HashMap<Integer, String>();
	static {
		tunnelPartQueriesLod4.put(DisplayForm.FOOTPRINT, TUNNEL_PART_FOOTPRINT_LOD4);
		tunnelPartQueriesLod4.put(DisplayForm.EXTRUDED, TUNNEL_PART_FOOTPRINT_LOD4);
		tunnelPartQueriesLod4.put(DisplayForm.GEOMETRY, TUNNEL_PART_GEOMETRY_LOD4);
		tunnelPartQueriesLod4.put(DisplayForm.COLLADA, TUNNEL_PART_COLLADA_LOD4_ROOT_IDS);
	}

	private static final HashMap<Integer, String> tunnelPartQueriesLod3 = new HashMap<Integer, String>();
	static {
		tunnelPartQueriesLod3.put(DisplayForm.FOOTPRINT, TUNNEL_PART_FOOTPRINT_LOD3);
		tunnelPartQueriesLod3.put(DisplayForm.EXTRUDED, TUNNEL_PART_FOOTPRINT_LOD3);
		tunnelPartQueriesLod3.put(DisplayForm.GEOMETRY, TUNNEL_PART_GEOMETRY_LOD3);
		tunnelPartQueriesLod3.put(DisplayForm.COLLADA, TUNNEL_PART_COLLADA_LOD3_ROOT_IDS);
	}

	private static final HashMap<Integer, String> tunnelPartQueriesLod2 = new HashMap<Integer, String>();
	static {
		tunnelPartQueriesLod2.put(DisplayForm.FOOTPRINT, TUNNEL_PART_FOOTPRINT_LOD2);
		tunnelPartQueriesLod2.put(DisplayForm.EXTRUDED, TUNNEL_PART_FOOTPRINT_LOD2);
		tunnelPartQueriesLod2.put(DisplayForm.GEOMETRY, TUNNEL_PART_GEOMETRY_LOD2);
		tunnelPartQueriesLod2.put(DisplayForm.COLLADA, TUNNEL_PART_COLLADA_LOD2_ROOT_IDS);
	}

	private static final HashMap<Integer, String> tunnelPartQueriesLod1 = new HashMap<Integer, String>();
	static {
		tunnelPartQueriesLod1.put(DisplayForm.GEOMETRY, TUNNEL_PART_GEOMETRY_LOD1);
		tunnelPartQueriesLod1.put(DisplayForm.COLLADA, TUNNEL_PART_COLLADA_LOD1_ROOT_IDS);
	}

	public static String getTunnelPartQuery (int lodToExportFrom, DisplayForm displayForm, DatabaseType type) {
		String query = null;
		switch (lodToExportFrom) {
		case 1:
			if (displayForm.getForm() == DisplayForm.FOOTPRINT || displayForm.getForm() == DisplayForm.EXTRUDED){
				query = TUNNEL_PART_FOOTPRINT_LOD1(type);		
			}
			else 
				query = tunnelPartQueriesLod1.get(displayForm.getForm());
			break;
		case 2:
			query = tunnelPartQueriesLod2.get(displayForm.getForm());
			break;
		case 3:
			query = tunnelPartQueriesLod3.get(displayForm.getForm());
			break;
		case 4:
			query = tunnelPartQueriesLod4.get(displayForm.getForm());
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No TunnelPart query found for LoD" + lodToExportFrom);
		}
		return query;
	}

	public static String getTunnelPartHighlightingQuery (int lodToExportFrom) {
		String query = null;
		switch (lodToExportFrom) {
		case 1:
			query = TUNNEL_PART_GEOMETRY_HIGHLIGHTING_LOD1;
			break;
		case 2:
			query = TUNNEL_PART_GEOMETRY_HIGHLIGHTING_LOD2;
			break;
		case 3:
			query = TUNNEL_PART_GEOMETRY_HIGHLIGHTING_LOD3;
			break;
		case 4:
			query = TUNNEL_PART_GEOMETRY_HIGHLIGHTING_LOD4;
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No TunnelPart highlighting query found for LoD" + lodToExportFrom);
		}
		return query;
	}
	
	// ----------------------------------------------------------------------
	// CITY OBJECT GROUP QUERIES
	// ----------------------------------------------------------------------

	public static final String CITYOBJECTGROUP_FOOTPRINT =
			"SELECT sg.geometry " +
					"FROM SURFACE_GEOMETRY sg, CITYOBJECTGROUP cog " +
					"WHERE " +
					"cog.id = ? " +
					"AND sg.root_id = cog.brep_id " +
					"AND sg.geometry IS NOT NULL ";

	public static final String CITYOBJECTGROUP_MEMBERS = 
			"SELECT co.id, co.gmlid, co.envelope, co.objectclass_id " + 
					"FROM CITYOBJECT co " +
					"WHERE co.ID IN (SELECT g2co.cityobject_id "+  
					"FROM GROUP_TO_CITYOBJECT g2co "+ 
					"WHERE g2co.cityobjectgroup_id = ?)";

	public static final String CITYOBJECTGROUP_MEMBERS_IN_BBOX(DatabaseType type) {
		StringBuilder query = new StringBuilder()
		.append("SELECT co.id, co.gmlid, co.objectclass_id, co.envelope ")
		.append("FROM CITYOBJECT co ")
		.append("WHERE co.ID IN (SELECT g2co.cityobject_id ")
		.append("FROM GROUP_TO_CITYOBJECT g2co ")
		.append("WHERE g2co.cityobjectgroup_id = ?) ")
		.append("AND ");
				
		switch (type) {
		case ORACLE:
			query.append("SDO_ANYINTERACT(co.envelope, ?) = 'TRUE'");
			break;
		case POSTGIS:
			query.append("co.envelope && ?");
			break;
		}	
		
		return query.toString();
	}

	// ----------------------------------------------------------------------
	// SOLITARY VEGETATION OBJECT QUERIES
	// ----------------------------------------------------------------------

	private static final String SOLITARY_VEGETATION_OBJECT_BASIS_DATA =
			"SELECT ig.relative_brep_id, svo.lod<LoD>_implicit_ref_point, " +
					"svo.lod<LoD>_implicit_transformation, svo.lod<LoD>_brep_id " +
					"FROM SOLITARY_VEGETAT_OBJECT svo " + 
					"LEFT JOIN IMPLICIT_GEOMETRY ig ON ig.id = svo.lod<LoD>_implicit_rep_id " + 
					"WHERE svo.id = ?";

	public static String getSolitaryVegetationObjectBasisData (int lodToExportFrom) {
		return SOLITARY_VEGETATION_OBJECT_BASIS_DATA.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	private static final String SOLITARY_VEGETATION_OBJECT_FOOTPRINT_EXTRUDED_GEOMETRY =
			"SELECT sg.geometry, '7' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id = ? " + 
					"AND sg.geometry IS NOT NULL";
	
	private static final String SOLITARY_VEGETATION_OBJECT_FOOTPRINT_EXTRUDED_IMPLICIT_GEOMETRY =
			"SELECT sg.implicit_geometry, '7' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id = ? " + 
					"AND sg.implicit_geometry IS NOT NULL";

	private static final String SOLITARY_VEGETATION_OBJECT_COLLADA_ROOT_IDS(AbstractSQLAdapter sqlAdapter) {
		String query = "SELECT ? "; // dummy
		if (sqlAdapter.requiresPseudoTableInSelect())
			query += " FROM " + sqlAdapter.getPseudoTableName();

		return query;
	}

	public static String getSolitaryVegetationObjectGeometryContents (DisplayForm displayForm, AbstractSQLAdapter sqlAdapter, Boolean isImplcitGeometry) {
		String query = null;
		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
		case DisplayForm.GEOMETRY:
			if (isImplcitGeometry)
				query = SOLITARY_VEGETATION_OBJECT_FOOTPRINT_EXTRUDED_IMPLICIT_GEOMETRY;
			else
				query = SOLITARY_VEGETATION_OBJECT_FOOTPRINT_EXTRUDED_GEOMETRY;			
			break;
		case DisplayForm.COLLADA:
			query = SOLITARY_VEGETATION_OBJECT_COLLADA_ROOT_IDS(sqlAdapter);
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No solitary vegetation object query found");
		}
		return query;
	}
	

	private static final String SOLITARY_VEGETATION_OBJECT_GEOMETRY_HIGHLIGHTING =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id IN (" +
					"SELECT geom.gid FROM (SELECT svo.lod<LoD>_brep_id as gid " +
					"FROM SOLITARY_VEGETAT_OBJECT svo " + 
					"WHERE svo.id = ?) geom) " +
					"AND sg.geometry IS NOT NULL";
	
	private static final String SOLITARY_VEGETATION_OBJECT_IMPLICIT_GEOMETRY_HIGHLIGHTING =
			"SELECT sg.implicit_geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id IN (" +
					"SELECT geom.gid FROM (SELECT ig.relative_brep_id as gid " + 
					"FROM SOLITARY_VEGETAT_OBJECT svo, IMPLICIT_GEOMETRY ig " + 
					"WHERE svo.id = ? " +
					"AND ig.id = svo.lod<LoD>_implicit_rep_id) geom)" +
					"AND sg.implicit_geometry IS NOT NULL";

	public static String getSolitaryVegetationObjectHighlightingQuery (int lodToExportFrom, Boolean isImplicitGeometry) {
		if (isImplicitGeometry)
			return SOLITARY_VEGETATION_OBJECT_IMPLICIT_GEOMETRY_HIGHLIGHTING.replace("<LoD>", String.valueOf(lodToExportFrom));
		return SOLITARY_VEGETATION_OBJECT_GEOMETRY_HIGHLIGHTING.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	// ----------------------------------------------------------------------
	// PLANT COVER QUERIES
	// ----------------------------------------------------------------------

	private static final String PLANT_COVER_FOOTPRINT_EXTRUDED_GEOMETRY =
			"SELECT sg.geometry, '8' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id IN (" +
					"SELECT geom.gid FROM (SELECT pc.lod<LoD>_multi_solid_id as gid " +
					"FROM PLANT_COVER pc " +
					"WHERE pc.id = ? " +
					"UNION " +
					"SELECT pc.lod<LoD>_multi_surface_id as gid " +
					"FROM PLANT_COVER pc " +
					"WHERE pc.id = ? " +
					") geom) AND sg.geometry IS NOT NULL";
	
	private static final String PLANT_COVER_COLLADA_ROOT_IDS =
			"SELECT geom.gid FROM (SELECT pc.lod<LoD>_multi_surface_id as gid " +			
					"FROM PLANT_COVER pc " +
					"WHERE " +
					"pc.id = ? " +
					"AND pc.lod<LoD>_multi_surface_id IS NOT NULL " + 
					"UNION " + 
					"SELECT pc.lod<LoD>_multi_solid_id as gid " +			
					"FROM PLANT_COVER pc " +
					"WHERE " +
					"pc.id = ? " +
					"AND pc.lod<LoD>_multi_solid_id IS NOT NULL) geom";	

	public static String getPlantCoverQuery (int lodToExportFrom, DisplayForm displayForm) {
		String query = null;
		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
		case DisplayForm.GEOMETRY:
			query = PLANT_COVER_FOOTPRINT_EXTRUDED_GEOMETRY;
			break;
		case DisplayForm.COLLADA:
			query = PLANT_COVER_COLLADA_ROOT_IDS;
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No plant cover object query found");
		}
		return query.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	public static String getPlantCoverHighlightingQuery (int lodToExportFrom) {
		return PLANT_COVER_FOOTPRINT_EXTRUDED_GEOMETRY.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	// ----------------------------------------------------------------------
	// GENERIC CITY OBJECT QUERIES
	// ----------------------------------------------------------------------

	private static final String GENERIC_CITYOBJECT_BASIS_DATA =
			"SELECT ig.relative_brep_id, gco.lod<LoD>_implicit_ref_point, " +
					"gco.lod<LoD>_implicit_transformation, gco.lod<LoD>_brep_id " +
					"FROM GENERIC_CITYOBJECT gco " +
					"LEFT JOIN IMPLICIT_GEOMETRY ig ON ig.id = gco.lod<LoD>_implicit_rep_id " + 
					"WHERE gco.id = ?";

	public static String getGenericCityObjectBasisData (int lodToExportFrom) {
		return GENERIC_CITYOBJECT_BASIS_DATA.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	private static final String GENERIC_CITYOBJECT_FOOTPRINT_EXTRUDED_GEOMETRY =
			"SELECT sg.geometry, '5' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id = ? " + 
					"AND sg.geometry IS NOT NULL";

	private static final String GENERIC_CITYOBJECT_FOOTPRINT_EXTRUDED_IMPLICIT_GEOMETRY =
			"SELECT sg.implicit_geometry, '5' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id = ? " + 
					"AND sg.implicit_geometry IS NOT NULL";
	
	private static final String GENERIC_CITYOBJECT_COLLADA_ROOT_IDS(AbstractSQLAdapter sqlAdapter) {
		String query = "SELECT ? "; // dummy
		if (sqlAdapter.requiresPseudoTableInSelect())
			query += " FROM " + sqlAdapter.getPseudoTableName();

		return query;
	}

	public static String getGenericCityObjectGeometryContents (DisplayForm displayForm, AbstractSQLAdapter sqlAdapter, Boolean isImplcitGeometry) {
		String query = null;
		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
		case DisplayForm.GEOMETRY:
			if (isImplcitGeometry)
				query = GENERIC_CITYOBJECT_FOOTPRINT_EXTRUDED_IMPLICIT_GEOMETRY;
			else
				query = GENERIC_CITYOBJECT_FOOTPRINT_EXTRUDED_GEOMETRY;		
			break;
		case DisplayForm.COLLADA:
			query = GENERIC_CITYOBJECT_COLLADA_ROOT_IDS(sqlAdapter);
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No GenericCityObject query found");
		}
		return query;
	}

	private static final String GENERIC_CITYOBJECT_GEOMETRY_HIGHLIGHTING =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id IN (" +
					"SELECT geom.gid FROM (SELECT gco.lod<LoD>_brep_id as gid " +
					"FROM GENERIC_CITYOBJECT gco " + 
					"WHERE gco.id = ?) geom) " +
					"AND sg.geometry IS NOT NULL";
	
	
	private static final String GENERIC_CITYOBJECT_IMPLICIT_GEOMETRY_HIGHLIGHTING =			
			"SELECT sg.implicit_geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id IN (" +
					"SELECT geom.gid FROM (SELECT ig.relative_brep_id as gid " + 
					"FROM GENERIC_CITYOBJECT gco, IMPLICIT_GEOMETRY ig " + 
					"WHERE gco.id = ? " +
					"AND ig.id = gco.lod<LoD>_implicit_rep_id) geom) " +
					"AND sg.implicit_geometry IS NOT NULL";

	
	public static String getGenericCityObjectHighlightingQuery (int lodToExportFrom, Boolean isImplicitGeometry) {
		if (isImplicitGeometry)
			return GENERIC_CITYOBJECT_IMPLICIT_GEOMETRY_HIGHLIGHTING.replace("<LoD>", String.valueOf(lodToExportFrom));
		return GENERIC_CITYOBJECT_GEOMETRY_HIGHLIGHTING.replace("<LoD>", String.valueOf(lodToExportFrom));
		
	}
	
	private static final String GENERIC_CITYOBJECT_POINT_AND_CURVE =
			"SELECT gco.lod<LoD>_other_geom " +
			"FROM generic_cityobject gco " +
			"WHERE gco.id = ? " +
			"AND gco.lod<LoD>_other_geom IS NOT NULL";	
	
    public static String getGenericCityObjectPointAndCurveQuery (int lodToExportFrom) {
    	return GENERIC_CITYOBJECT_POINT_AND_CURVE.replace("<LoD>", String.valueOf(lodToExportFrom));
    }

	// ----------------------------------------------------------------------
	// CITY FURNITURE QUERIES
	// ----------------------------------------------------------------------

	private static final String CITY_FURNITURE_BASIS_DATA =
			"SELECT ig.relative_brep_id, cf.lod<LoD>_implicit_ref_point, " +
					"cf.lod<LoD>_implicit_transformation, cf.lod<LoD>_brep_id " +
					"FROM CITY_FURNITURE cf " + 
					"LEFT JOIN IMPLICIT_GEOMETRY ig ON ig.id = cf.lod<LoD>_implicit_rep_id " + 
					"WHERE cf.id = ?";

	public static String getCityFurnitureBasisData (int lodToExportFrom) {
		return CITY_FURNITURE_BASIS_DATA.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	private static final String CITY_FURNITURE_FOOTPRINT_EXTRUDED_GEOMETRY =
			"SELECT sg.geometry, '21' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id = ? " + 
					"AND sg.geometry IS NOT NULL";
	
	private static final String CITY_FURNITURE_FOOTPRINT_EXTRUDED_IMPLICIT_GEOMETRY =
			"SELECT sg.implicit_geometry, '21' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id = ? " + 
					"AND sg.implicit_geometry IS NOT NULL";

	private static final String CITY_FURNITURE_COLLADA_ROOT_IDS(AbstractSQLAdapter sqlAdapter) {
		String query = "SELECT ? "; // dummy
		if (sqlAdapter.requiresPseudoTableInSelect())
			query += " FROM " + sqlAdapter.getPseudoTableName();

		return query;
	}

	public static String getCityFurnitureGeometryContents (DisplayForm displayForm, AbstractSQLAdapter sqlAdapter, Boolean isImplcitGeometry) {
		String query = null;
		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
		case DisplayForm.GEOMETRY:
			if (isImplcitGeometry)
				query = CITY_FURNITURE_FOOTPRINT_EXTRUDED_IMPLICIT_GEOMETRY;
			else
				query = CITY_FURNITURE_FOOTPRINT_EXTRUDED_GEOMETRY;		
			break;
		case DisplayForm.COLLADA:
			query = CITY_FURNITURE_COLLADA_ROOT_IDS(sqlAdapter);
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No city furniture query found");
		}
		return query;
	}

	private static final String CITY_FURNITURE_GEOMETRY_HIGHLIGHTING =
			"SELECT sg.geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id IN (" +
					"SELECT geom.gid FROM (SELECT cf.lod<LoD>_brep_id as gid " +
					"FROM CITY_FURNITURE cf " + 
					"WHERE cf.id = ?) geom) " +
					"AND sg.geometry IS NOT NULL";
	
	
	private static final String CITY_FURNITURE_IMPLICIT_GEOMETRY_HIGHLIGHTING =
			"SELECT sg.implicit_geometry, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id IN (" +
					"SELECT geom.gid FROM (SELECT ig.relative_brep_id as gid " + 
					"FROM CITY_FURNITURE cf, IMPLICIT_GEOMETRY ig " + 
					"WHERE cf.id = ? " +
					"AND ig.id = cf.lod<LoD>_implicit_rep_id) geom) " +
					"AND sg.implicit_geometry IS NOT NULL";


	public static String getCityFurnitureHighlightingQuery (int lodToExportFrom, Boolean isImplicitGeometry) {
		if (isImplicitGeometry)
			return CITY_FURNITURE_IMPLICIT_GEOMETRY_HIGHLIGHTING.replace("<LoD>", String.valueOf(lodToExportFrom));
		return CITY_FURNITURE_GEOMETRY_HIGHLIGHTING.replace("<LoD>", String.valueOf(lodToExportFrom));
		
	}

	// ----------------------------------------------------------------------
	// WATER BODY QUERIES
	// ----------------------------------------------------------------------

/*	private static final String WATERBODY_LOD1_ROOT_IDS =
			"SELECT wb.lod<LoD>_solid_id " +
					"FROM WATERBODY wb " +
					"WHERE wb.id = ? " +
					"AND wb.lod<LoD>_solid_id IS NOT NULL";*/
	
	private static final String WATERBODY_LOD1_ROOT_IDS =
			"SELECT geom.gid FROM (" +
					"SELECT wb.lod<LoD>_solid_id as gid " +
					"FROM WATERBODY wb " +
					"WHERE wb.id = ? " +
					"AND wb.lod<LoD>_solid_id IS NOT NULL " +
					"UNION " +
					"SELECT wb.lod<LoD>_multi_surface_id as gid " + 
					"FROM WATERBODY wb " +
					"WHERE wb.id = ? " +
					"AND wb.lod<LoD>_multi_surface_id IS NOT NULL) geom";
	
	private static final String WATERBODY_ROOT_IDS =
			"SELECT geom.gid FROM (" +
					"SELECT wb.lod<LoD>_solid_id as gid " +
					"FROM WATERBODY wb " +
					"WHERE wb.id = ? " +
					"AND wb.lod<LoD>_solid_id IS NOT NULL " +
					"UNION " +
					"SELECT wbs.lod<LoD>_surface_id as gid " + // min lod value here is 2
					"FROM WATERBOD_TO_WATERBND_SRF wb2wbs, WATERBOUNDARY_SURFACE wbs " +
					"WHERE wb2wbs.waterbody_id = ? " +
					"AND wbs.id = wb2wbs.waterboundary_surface_id " +
					"AND wbs.lod<LoD>_surface_id IS NOT NULL) geom";

	private static final String WATERBODY_FOOTPRINT_EXTRUDED_GEOMETRY =
			"SELECT sg.geometry, '9' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id IN (" + WATERBODY_ROOT_IDS +
					") AND sg.geometry IS NOT NULL";

	private static final String WATERBODY_FOOTPRINT_EXTRUDED_GEOMETRY_LOD1 =
			"SELECT sg.geometry, '9' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id IN (" +
					"SELECT geom.gid FROM (SELECT wb.lod1_solid_id as gid " +
					"FROM WATERBODY wb " +
					"WHERE wb.id = ? " +
					"UNION " +
					"SELECT wb.lod1_multi_surface_id as gid " +
					"FROM WATERBODY wb " +
					"WHERE wb.id = ? " +
					") geom) AND sg.geometry IS NOT NULL";
/*					"UNION " +
					"SELECT wb.lod1_multi_curve, 'Water' as type, -1 " +
					"FROM WATERBODY wb " +
					"WHERE wb.id = ? " + 
					"AND wb.lod1_multi_curve IS NOT NULL";*/

	private static final String WATERBODY_FOOTPRINT_LOD0 =
			"SELECT sg.geometry, '9' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg, WATERBODY wb " +
					"WHERE wb.id = ? "+
					"AND sg.root_id = wb.lod0_multi_surface_id " +
					"AND sg.geometry IS NOT NULL " +
					"UNION " +
					"SELECT wb.lod0_multi_curve, 'Water' as type, -1 " +
					"FROM WATERBODY wb " +
					"WHERE wb.id = ? " + 
					"AND wb.lod0_multi_curve IS NOT NULL";

	public static String getWaterBodyQuery (int lodToExportFrom, DisplayForm displayForm) {
		String query = null;
		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
		case DisplayForm.GEOMETRY:
			if (lodToExportFrom > 1) {
				query = WATERBODY_FOOTPRINT_EXTRUDED_GEOMETRY;
			}
			else if (lodToExportFrom == 1) {
				query = WATERBODY_FOOTPRINT_EXTRUDED_GEOMETRY_LOD1;
			}
			else {
				query = WATERBODY_FOOTPRINT_LOD0;
			}
			break;

		case DisplayForm.COLLADA:
			if (lodToExportFrom == 1) {
				query = WATERBODY_LOD1_ROOT_IDS;
			}
			else {
				query = WATERBODY_ROOT_IDS;
			}
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No water body object query found");
		}
		return query.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	public static String getWaterBodyHighlightingQuery (int lodToExportFrom) {
		String query = null;
		if (lodToExportFrom > 1) {
			query = WATERBODY_FOOTPRINT_EXTRUDED_GEOMETRY;
		}
		else if (lodToExportFrom == 1) {
			query = WATERBODY_FOOTPRINT_EXTRUDED_GEOMETRY_LOD1;
		}
		else {
			query = WATERBODY_FOOTPRINT_LOD0;
		}
		return query.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	// ----------------------------------------------------------------------
	// LAND USE QUERIES
	// ----------------------------------------------------------------------

	private static final String LAND_USE_FOOTPRINT_EXTRUDED_GEOMETRY =
			"SELECT sg.geometry, '4' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg, LAND_USE lu " + 
					"WHERE lu.id = ? " +
					"AND sg.root_id = lu.lod<LoD>_multi_surface_id " + 
					"AND sg.geometry IS NOT NULL";

	private static final String LAND_USE_COLLADA_ROOT_IDS =
			"SELECT lu.lod<LoD>_multi_surface_id " +
					"FROM LAND_USE lu " +
					"WHERE lu.id = ? " +
					"AND lu.lod<LoD>_multi_surface_id IS NOT NULL";

	public static String getLandUseQuery (int lodToExportFrom, DisplayForm displayForm) {
		String query = null;
		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
		case DisplayForm.GEOMETRY:
			query = LAND_USE_FOOTPRINT_EXTRUDED_GEOMETRY;
			break;
		case DisplayForm.COLLADA:
			query = LAND_USE_COLLADA_ROOT_IDS;
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No land use object query found");
		}
		return query.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	public static String getLandUseHighlightingQuery (int lodToExportFrom) {
		return LAND_USE_FOOTPRINT_EXTRUDED_GEOMETRY.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	// ----------------------------------------------------------------------
	// TRANSPORTATION QUERIES
	// ----------------------------------------------------------------------

	private static final String TRANSPORTATION_COMPLEX_LOD1_ROOT_IDS =
			"SELECT tc.lod<LoD>_multi_surface_id " +
					"FROM TRANSPORTATION_COMPLEX tc " +
					"WHERE tc.id = ? " +
					"AND tc.lod<LoD>_multi_surface_id IS NOT NULL";

	private static final String TRANSPORTATION_COMPLEX_ROOT_IDS =
			"SELECT geom.gid FROM (SELECT tc.lod<LoD>_multi_surface_id as gid " +
					"FROM TRANSPORTATION_COMPLEX tc " +
					"WHERE tc.id = ? " +
					"AND tc.lod<LoD>_multi_surface_id IS NOT NULL" +
					" UNION " +
					"SELECT ta.lod<LoD>_multi_surface_id as gid " + // min lod value here is 2
					"FROM TRAFFIC_AREA ta " +
					"WHERE ta.transportation_complex_id = ? " +
					"AND ta.lod<LoD>_multi_surface_id IS NOT NULL) geom";

	private static final String TRANSPORTATION_COMPLEX_FOOTPRINT_EXTRUDED_GEOMETRY =
			"SELECT sg.geometry, '42' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id IN (" + TRANSPORTATION_COMPLEX_ROOT_IDS +
					") AND sg.geometry IS NOT NULL";

	private static final String TRANSPORTATION_COMPLEX_FOOTPRINT_EXTRUDED_GEOMETRY_LOD1 =
			"SELECT sg.geometry, '42' as objectclass_id, sg.id " +
					"FROM SURFACE_GEOMETRY sg " +
					"WHERE sg.root_id IN (" +
					"select tmp.id from (SELECT tc.lod1_multi_surface_id as id " +
					"FROM TRANSPORTATION_COMPLEX tc " +
					"WHERE tc.id = ? " +
					"AND tc.lod1_multi_surface_id IS NOT NULL" +
					") tmp ) AND sg.geometry IS NOT NULL";

	private static final String TRANSPORTATION_COMPLEX_FOOTPRINT_LOD0 =
			"SELECT tc.lod0_network " +
					"FROM TRANSPORTATION_COMPLEX tc " +
					"WHERE tc.id = ? " +
					"AND tc.lod0_network IS NOT NULL";

	public static String getTransportationQuery (int lodToExportFrom, DisplayForm displayForm) {
		String query = null;
		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
		case DisplayForm.GEOMETRY:
			switch (lodToExportFrom) {
			case 2:
			case 3:
			case 4:
				query = TRANSPORTATION_COMPLEX_FOOTPRINT_EXTRUDED_GEOMETRY;
				break;
			case 1:
				query = TRANSPORTATION_COMPLEX_FOOTPRINT_EXTRUDED_GEOMETRY_LOD1;
				break;
			case 0:
				query = TRANSPORTATION_COMPLEX_FOOTPRINT_LOD0;
				break;
			}
			break;

		case DisplayForm.COLLADA:
			if (lodToExportFrom == 1) {
				query = TRANSPORTATION_COMPLEX_LOD1_ROOT_IDS;
			}
			else {
				query = TRANSPORTATION_COMPLEX_ROOT_IDS;
			}
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No transportation object query found");
		}

		//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
		return query.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	public static String getTransportationHighlightingQuery (int lodToExportFrom) {
		String query = null;
		if (lodToExportFrom > 1) {
			query = TRANSPORTATION_COMPLEX_FOOTPRINT_EXTRUDED_GEOMETRY;
		}
		else if (lodToExportFrom == 1) {
			query = TRANSPORTATION_COMPLEX_FOOTPRINT_EXTRUDED_GEOMETRY_LOD1;
		}
		else {
			query = TRANSPORTATION_COMPLEX_FOOTPRINT_LOD0;
		}
		return query.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	// ----------------------------------------------------------------------
	// RELIEF QUERIES
	// ----------------------------------------------------------------------

	public static final int RELIEF_TIN_QUERY = 0;
	private static final String RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_TIN =
			"SELECT sg.geometry, '14' as objectclass_id, sg.id " +
					"FROM RELIEF_FEATURE rf, RELIEF_FEAT_TO_REL_COMP rf2rc, TIN_RELIEF tr, SURFACE_GEOMETRY sg " +
					"WHERE rf.id = ? " +
					"AND rf.lod = <LoD> " +
					"AND rf2rc.relief_feature_id = rf.id " +
					"AND tr.id = rf2rc.relief_component_id " +
					"AND sg.root_id = tr.surface_geometry_id " + 
					"AND sg.geometry IS NOT NULL";

	public static final int RELIEF_TIN_BREAK_LINES_QUERY = 1;
	private static final String RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_TIN_BREAK_LINES =
			"SELECT tr.break_lines, '14' as objectclass_id, -1 " +
					"FROM RELIEF_FEATURE rf, RELIEF_FEAT_TO_REL_COMP rf2rc, TIN_RELIEF tr " +
					"WHERE rf.id = ? " +
					"AND rf.lod = <LoD> " +
					"AND rf2rc.relief_feature_id = rf.id " +
					"AND tr.id = rf2rc.relief_component_id " +
					"AND tr.break_lines IS NOT NULL";

	public static final int RELIEF_TIN_STOP_LINES_QUERY = 2;
	private static final String RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_TIN_STOP_LINES =
			"SELECT tr.stop_lines, '14' as objectclass_id, -1 " +
					"FROM RELIEF_FEATURE rf, RELIEF_FEAT_TO_REL_COMP rf2rc, TIN_RELIEF tr " +
					"WHERE rf.id = ? " +
					"AND rf.lod = <LoD> " +
					"AND rf2rc.relief_feature_id = rf.id " +
					"AND tr.id = rf2rc.relief_component_id " +
					"AND tr.stop_lines IS NOT NULL";
	/*
	public static final int RELIEF_TIN_CONTROL_POINTS_QUERY = 2;
	private static final String RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_TIN_CONTROL_POINTS =
		"SELECT tr.control_points, 'Relief' as type, -1 " +
		"FROM RELIEF_FEATURE rf, RELIEF_FEAT_TO_REL_COMP rf2rc, TIN_RELIEF tr " +
		"WHERE rf.id = ? " +
			"AND rf.lod = <LoD> " +
			"AND rf2rc.relief_feature_id = rf.id " +
			"AND tr.id = rf2rc.relief_component_id";
	 */
	public static final int RELIEF_BREAK_BREAK_LINES_QUERY = 3;
	private static final String RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_BREAK_BREAK_LINES =
			"SELECT br.break_lines, '14' as objectclass_id, -1 " +
					"FROM RELIEF_FEATURE rf, RELIEF_FEAT_TO_REL_COMP rf2rc, BREAKLINE_RELIEF br " +
					"WHERE rf.id = ? " +
					"AND rf.lod = <LoD> " +
					"AND rf2rc.relief_feature_id = rf.id " +
					"AND br.id = rf2rc.relief_component_id " +
					"AND br.break_lines IS NOT NULL";

	public static final int RELIEF_BREAK_RIDGE_OR_VALLEY_LINES_QUERY = 4;
	private static final String RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_BREAK_RIDGE_OR_VALLEY_LINES =
			"SELECT br.ridge_or_valley_lines, '14' as objectclass_id, -1 " +
					"FROM RELIEF_FEATURE rf, RELIEF_FEAT_TO_REL_COMP rf2rc, BREAKLINE_RELIEF br " +
					"WHERE rf.id = ? " +
					"AND rf.lod = <LoD> " +
					"AND rf2rc.relief_feature_id = rf.id " +
					"AND br.id = rf2rc.relief_component_id " +
					"AND br.ridge_or_valley_lines IS NOT NULL";
	/*
	public static final int RELIEF_MASSPOINT_QUERY = 4;
	private static final String RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_MASSPOINT =
		"SELECT mr.relief_points, 'Relief' as type, -1 " +
		"FROM RELIEF_FEATURE rf, RELIEF_FEAT_TO_REL_COMP rf2rc, MASSPOINT_RELIEF mr " +
		"WHERE rf.id = ? " +
			"AND rf.lod = <LoD> " +
			"AND rf2rc.relief_feature_id = rf.id " +
			"AND mr.id = rf2rc.relief_component_id";
	 */
	private static final String RELIEF_COLLADA_ROOT_IDS =
			"SELECT tr.surface_geometry_id " +
					"FROM RELIEF_FEATURE rf, RELIEF_FEAT_TO_REL_COMP rf2rc, TIN_RELIEF tr " +
					"WHERE rf.id = ? " +
					"AND rf.lod = <LoD> " +
					"AND rf2rc.relief_feature_id = rf.id " +
					"AND tr.id = rf2rc.relief_component_id";

	public static String getReliefQuery (int lodToExportFrom, DisplayForm displayForm, int queryNumber) {
		String query = null;
		switch (queryNumber) {
		case RELIEF_TIN_QUERY:
			switch (displayForm.getForm()) {
			case DisplayForm.FOOTPRINT:
			case DisplayForm.EXTRUDED:
			case DisplayForm.GEOMETRY:
				query = RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_TIN;
				break;
			case DisplayForm.COLLADA:
				query = RELIEF_COLLADA_ROOT_IDS;
				break;
			}
			break;
		case RELIEF_TIN_BREAK_LINES_QUERY:
			query = RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_TIN_BREAK_LINES;
			break;
		case RELIEF_TIN_STOP_LINES_QUERY:
			query = RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_TIN_STOP_LINES;
			break;
		case RELIEF_BREAK_BREAK_LINES_QUERY:
			query = RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_BREAK_BREAK_LINES;
			break;
		case RELIEF_BREAK_RIDGE_OR_VALLEY_LINES_QUERY:
			query = RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_BREAK_RIDGE_OR_VALLEY_LINES;
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No relief object query found");
		}

		//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
		return query.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

	public static String getReliefHighlightingQuery (int lodToExportFrom, int queryNumber) {
		String query = null;
		switch (queryNumber) {
		case RELIEF_TIN_QUERY:
			query = RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_TIN;
			break;
		case RELIEF_TIN_BREAK_LINES_QUERY:
			query = RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_TIN_BREAK_LINES;
			break;
		case RELIEF_TIN_STOP_LINES_QUERY:
			query = RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_TIN_STOP_LINES;
			break;
		case RELIEF_BREAK_BREAK_LINES_QUERY:
			query = RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_BREAK_BREAK_LINES;
			break;
		case RELIEF_BREAK_RIDGE_OR_VALLEY_LINES_QUERY:
			query = RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_BREAK_RIDGE_OR_VALLEY_LINES;
			break;
		default:
			Logger.getInstance().log(LogLevel.INFO, "No relief object query found");
		}
		return query.replace("<LoD>", String.valueOf(lodToExportFrom));
	}

}
