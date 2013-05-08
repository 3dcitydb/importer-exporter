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
package de.tub.citydb.modules.kml.database;

import java.util.HashMap;

import de.tub.citydb.api.log.LogLevel;
import de.tub.citydb.config.project.kmlExporter.DisplayForm;
import de.tub.citydb.log.Logger;

public class Queries {

	// ----------------------------------------------------------------------
	// 	GENERIC PURPOSE QUERIES
	// ----------------------------------------------------------------------

    public static final String GET_IDS =
    	"SELECT co.id, co.gmlid, co.class_id " +
		"FROM CITYOBJECT co " +
		"WHERE " +
		  "(SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2002, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?,?,?)), 'mask=overlapbdydisjoint') ='TRUE') " +
		"UNION ALL " +
    	"SELECT co.id, co.gmlid, co.class_id " +
		"FROM CITYOBJECT co " +
		"WHERE " +
		  "(SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2003, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), 'mask=inside+coveredby') ='TRUE') " +
		"UNION ALL " +
    	"SELECT co.id, co.gmlid, co.class_id " +
		"FROM CITYOBJECT co " +
		"WHERE " +
		  "(SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2003, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), 'mask=equal') ='TRUE') " +
		"ORDER BY 3"; // ORDER BY co.class_id

	public static final String GET_EXTRUDED_HEIGHT =
		"SELECT " + // "b.measured_height, " +
		"SDO_GEOM.SDO_MAX_MBR_ORDINATE(co.envelope, 3) - SDO_GEOM.SDO_MIN_MBR_ORDINATE(co.envelope, 3) AS envelope_measured_height " +
		"FROM CITYOBJECT co " + // ", BUILDING b " +
		"WHERE " +
			"co.id = ?"; // + " AND b.building_root_id = co.id";

	public static final String GET_STRVAL_GENERICATTRIB_FROM_ID =
		"SELECT coga.strval " +
		"FROM CITYOBJECT_GENERICATTRIB coga " + 
		"WHERE coga.cityobject_id = ? AND coga.attrname = ? ";

	public static final String GET_ID_FROM_GMLID =
		"SELECT id FROM CITYOBJECT WHERE gmlid = ?";

    public static final String GET_ID_AND_OBJECTCLASS_FROM_GMLID =
		"SELECT id, class_id FROM CITYOBJECT WHERE gmlid = ?";

    public static final String GET_GMLID_AND_OBJECTCLASS_FROM_ID =
		"SELECT gmlid, class_id FROM CITYOBJECT WHERE id = ?";

    public static final String INSERT_GE_ZOFFSET =
		"INSERT INTO CITYOBJECT_GENERICATTRIB (ID, ATTRNAME, DATATYPE, STRVAL, CITYOBJECT_ID) " +
		"VALUES (CITYOBJECT_GENERICATT_SEQ.NEXTVAL, ?, 1, ?, ?)";
	
	public static final String TRANSFORM_GEOMETRY_TO_WGS84 =
		"SELECT SDO_CS.TRANSFORM(?, 4326) FROM DUAL";

	public static final String TRANSFORM_GEOMETRY_TO_WGS84_3D =
		"SELECT SDO_CS.TRANSFORM(?, 4329) FROM DUAL";

	public static final String GET_ENVELOPE_IN_WGS84_FROM_ID =
		"SELECT SDO_CS.TRANSFORM(co.envelope, 4326) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_ENVELOPE_IN_WGS84_3D_FROM_ID =
		"SELECT SDO_CS.TRANSFORM(co.envelope, 4329) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_CENTROID_IN_WGS84_FROM_ID =
		"SELECT SDO_CS.TRANSFORM(SDO_GEOM.SDO_CENTROID(co.envelope, 0.001), 4326) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_CENTROID_IN_WGS84_3D_FROM_ID =
		"SELECT SDO_CS.TRANSFORM(SDO_GEOM.SDO_CENTROID(co.envelope, 0.001), 4329) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";
	
	public static final String GET_CENTROID_LAT_IN_WGS84_FROM_ID =
		"SELECT v.Y FROM TABLE(" +
			"SELECT SDO_UTIL.GETVERTICES(SDO_CS.TRANSFORM(SDO_GEOM.SDO_CENTROID(co.envelope, 0.001), 4326)) " +
			"FROM CITYOBJECT co " + 
			"WHERE co.id = ?) v";

	public static final String GET_CENTROID_LAT_IN_WGS84_3D_FROM_ID =
		"SELECT v.Y FROM TABLE(" +
			"SELECT SDO_UTIL.GETVERTICES(SDO_CS.TRANSFORM(SDO_GEOM.SDO_CENTROID(co.envelope, 0.001), 4329)) " +
			"FROM CITYOBJECT co " + 
			"WHERE co.id = ?) v";

	public static final String GET_CENTROID_LON_IN_WGS84_FROM_ID =
		"SELECT v.X FROM TABLE(" +
			"SELECT SDO_UTIL.GETVERTICES(SDO_CS.TRANSFORM(SDO_GEOM.SDO_CENTROID(co.envelope, 0.001), 4326)) " +
			"FROM CITYOBJECT co " + 
			"WHERE co.id = ?) v";

	public static final String GET_CENTROID_LON_IN_WGS84_3D_FROM_ID =
		"SELECT v.X FROM TABLE(" +
			"SELECT SDO_UTIL.GETVERTICES(SDO_CS.TRANSFORM(SDO_GEOM.SDO_CENTROID(co.envelope, 0.001), 4329)) " +
			"FROM CITYOBJECT co " + 
			"WHERE co.id = ?) v";

	public static final String GET_ENVELOPE_LAT_MIN_IN_WGS84_FROM_ID =
		"SELECT SDO_GEOM.SDO_MIN_MBR_ORDINATE(SDO_CS.TRANSFORM(co.envelope, 4326), 2) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_ENVELOPE_LAT_MIN_IN_WGS84_3D_FROM_ID =
		"SELECT SDO_GEOM.SDO_MIN_MBR_ORDINATE(SDO_CS.TRANSFORM(co.envelope, 4329), 2) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_ENVELOPE_LAT_MAX_IN_WGS84_FROM_ID =
		"SELECT SDO_GEOM.SDO_MAX_MBR_ORDINATE(SDO_CS.TRANSFORM(co.envelope, 4326), 2) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_ENVELOPE_LAT_MAX_IN_WGS84_3D_FROM_ID =
		"SELECT SDO_GEOM.SDO_MAX_MBR_ORDINATE(SDO_CS.TRANSFORM(co.envelope, 4329), 2) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_ENVELOPE_LON_MIN_IN_WGS84_FROM_ID =
		"SELECT SDO_GEOM.SDO_MIN_MBR_ORDINATE(SDO_CS.TRANSFORM(co.envelope, 4326), 1) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_ENVELOPE_LON_MIN_IN_WGS84_3D_FROM_ID =
		"SELECT SDO_GEOM.SDO_MIN_MBR_ORDINATE(SDO_CS.TRANSFORM(co.envelope, 4329), 1) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_ENVELOPE_LON_MAX_IN_WGS84_FROM_ID =
		"SELECT SDO_GEOM.SDO_MAX_MBR_ORDINATE(SDO_CS.TRANSFORM(co.envelope, 4326), 1) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_ENVELOPE_LON_MAX_IN_WGS84_3D_FROM_ID =
		"SELECT SDO_GEOM.SDO_MAX_MBR_ORDINATE(SDO_CS.TRANSFORM(co.envelope, 4329), 1) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_ENVELOPE_HEIGHT_MIN_IN_WGS84_FROM_ID =
		"SELECT SDO_GEOM.SDO_MIN_MBR_ORDINATE(SDO_CS.TRANSFORM(co.envelope, 4326), 3) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_ENVELOPE_HEIGHT_MIN_IN_WGS84_3D_FROM_ID =
		"SELECT SDO_GEOM.SDO_MIN_MBR_ORDINATE(SDO_CS.TRANSFORM(co.envelope, 4329), 3) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_ENVELOPE_HEIGHT_MAX_IN_WGS84_FROM_ID =
		"SELECT SDO_GEOM.SDO_MAX_MBR_ORDINATE(SDO_CS.TRANSFORM(co.envelope, 4326), 3) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

	public static final String GET_ENVELOPE_HEIGHT_MAX_IN_WGS84_3D_FROM_ID =
		"SELECT SDO_GEOM.SDO_MAX_MBR_ORDINATE(SDO_CS.TRANSFORM(co.envelope, 4329), 3) " +
		"FROM CITYOBJECT co " +
		"WHERE co.id = ?";

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
				"AND ts.type = 'GroundSurface' " +
				"AND sg.root_id = ts.lod4_multi_surface_id " +
				"AND sg.geometry IS NOT NULL "; // +
//			"ORDER BY ts.building_id";

		private static final String BUILDING_PART_COLLADA_LOD4_ROOT_IDS =
			"SELECT b.lod4_geometry_id " + 
			"FROM BUILDING b " + 
			"WHERE " +  
				"b.id = ? " +
				"AND b.lod4_geometry_id IS NOT NULL " +
			"UNION " + 
			"SELECT ts.lod4_multi_surface_id " + 
			"FROM THEMATIC_SURFACE ts " + 
			"WHERE " +  
				"ts.building_id = ? " +
				"AND ts.lod4_multi_surface_id IS NOT NULL " +
			"UNION " + 
	/*
			"SELECT r.lod4_geometry_id " + 
			"FROM ROOM r " + 
			"WHERE " +  
	  			"r.building_id = ? " +
				"AND r.lod4_geometry_id IS NOT NULL " +
	*/
			"SELECT ts.lod4_multi_surface_id " + 
			"FROM ROOM r, THEMATIC_SURFACE ts " + 
			"WHERE " +  
	  			"r.building_id = ? " +
				"AND ts.room_id = r.id " +
				"AND ts.lod4_multi_surface_id IS NOT NULL " +
			"UNION " + 
			"SELECT bf.lod4_geometry_id " + 
			"FROM ROOM r, BUILDING_FURNITURE bf " + 
			"WHERE " +  
				"r.building_id = ? " +
				"AND bf.room_id = r.id " +
				"AND bf.lod4_geometry_id IS NOT NULL " +
			"UNION " + 
			"SELECT bi.lod4_geometry_id " + 
			"FROM ROOM r, BUILDING_INSTALLATION bi " + 
			"WHERE " +  
				"r.building_id = ? " +
				"AND bi.room_id = r.id " +
				"AND bi.lod4_geometry_id IS NOT NULL " +
			"UNION " + 
			"SELECT o.lod4_multi_surface_id " + 
			"FROM THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE o2ts, OPENING o " + 
			"WHERE " +  
				"ts.building_id = ? " +
				"AND ts.lod4_multi_surface_id IS NOT NULL " +
				"AND o2ts.thematic_surface_id = ts.id " +
				"AND o.id = o2ts.opening_id";
				
		private static final String BUILDING_PART_GEOMETRY_LOD4 =
			"SELECT sg.geometry, ts.type, sg.id " +
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
				"AND ts.type = 'GroundSurface' " +
				"AND sg.root_id = ts.lod3_multi_surface_id " +
				"AND sg.geometry IS NOT NULL "; // +
//			"ORDER BY ts.building_id";

		private static final String BUILDING_PART_COLLADA_LOD3_ROOT_IDS =
			"SELECT b.lod3_geometry_id " + 
			"FROM BUILDING b " + 
			"WHERE " +  
				"b.id = ? " +
				"AND b.lod3_geometry_id IS NOT NULL " +
			"UNION " + 
			"SELECT ts.lod3_multi_surface_id " + 
			"FROM THEMATIC_SURFACE ts " + 
			"WHERE " +  
				"ts.building_id = ? " +
				"AND ts.lod3_multi_surface_id IS NOT NULL " +
			"UNION " + 
			"SELECT bi.lod3_geometry_id " + 
			"FROM BUILDING_INSTALLATION bi " + 
			"WHERE " +  
	  			"bi.building_id = ? " +
				"AND bi.lod3_geometry_id IS NOT NULL " +
			"UNION " + 
			"SELECT o.lod3_multi_surface_id " + 
			"FROM THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE o2ts, OPENING o " + 
			"WHERE " +  
				"ts.building_id = ? " +
				"AND ts.lod3_multi_surface_id IS NOT NULL " +
				"AND o2ts.thematic_surface_id = ts.id " +
				"AND o.id = o2ts.opening_id";

		private static final String BUILDING_PART_GEOMETRY_LOD3 =
			"SELECT sg.geometry, ts.type, sg.id " +
			"FROM SURFACE_GEOMETRY sg " +
			"LEFT JOIN THEMATIC_SURFACE ts ON ts.lod3_multi_surface_id = sg.root_id " +
			"WHERE " +
				"sg.geometry IS NOT NULL " +
				"AND sg.root_id IN (" + BUILDING_PART_COLLADA_LOD3_ROOT_IDS	+ ")";

		private static final String BUILDING_PART_COLLADA_LOD2_ROOT_IDS =
			"SELECT b.lod2_geometry_id " + 
			"FROM BUILDING b " + 
			"WHERE " +  
				"b.id = ? " +
				"AND b.lod2_geometry_id IS NOT NULL " +
			"UNION " + 
			"SELECT ts.lod2_multi_surface_id " + 
			"FROM THEMATIC_SURFACE ts " + 
			"WHERE " +  
				"ts.building_id = ? " +
				"AND ts.lod2_multi_surface_id IS NOT NULL " +
			"UNION " + 
			"SELECT bi.lod2_geometry_id " + 
			"FROM BUILDING_INSTALLATION bi " + 
			"WHERE " +  
	  			"bi.building_id = ? " +
				"AND bi.lod2_geometry_id IS NOT NULL";

		
		private static final String BUILDING_PART_COLLADA_LOD1_ROOT_IDS =
			"SELECT b.lod1_geometry_id " +
			"FROM BUILDING b " +
			"WHERE " +
				"b.id = ? " +
				"AND b.lod1_geometry_id IS NOT NULL";

		private static final String COLLADA_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID_0 =
			"SELECT sg.geometry, sg.id, sg.parent_id, sd.type, " +
					"sd.x3d_shininess, sd.x3d_transparency, sd.x3d_ambient_intensity, sd.x3d_specular_color, sd.x3d_diffuse_color, sd.x3d_emissive_color, sd.x3d_is_smooth, " +
					"sd.tex_image_uri, sd.tex_image, tp.texture_coordinates, a.theme " +
			"FROM SURFACE_GEOMETRY sg " +
				"LEFT JOIN TEXTUREPARAM tp ON tp.surface_geometry_id = sg.id " + 
				"LEFT JOIN SURFACE_DATA sd ON sd.id = tp.surface_data_id " +
				"LEFT JOIN APPEAR_TO_SURFACE_DATA a2sd ON a2sd.surface_data_id = sd.id " +
				"LEFT JOIN APPEARANCE a ON a2sd.appearance_id = a.id " +
			"WHERE " +
				"sg.root_id = ? "; // +
//				"AND (a.theme = ? OR a.theme IS NULL) " +
//				"ORDER BY sg.parent_id ASC"; // own root surfaces first

		public static final String[] COLLADA_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID = new String[] {
			COLLADA_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID_0 + "AND sg.geometry IS NULL ORDER BY sg.id", // parents
			COLLADA_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID_0 + "AND sg.geometry IS NOT NULL" // elementary surfaces
		};

		private static final String BUILDING_PART_GEOMETRY_LOD2 =
			"SELECT sg.geometry, ts.type, sg.id " +
			"FROM SURFACE_GEOMETRY sg " +
			"LEFT JOIN THEMATIC_SURFACE ts ON ts.lod2_multi_surface_id = sg.root_id " +
			"WHERE " +
				"sg.geometry IS NOT NULL " +
				"AND sg.root_id IN (" +
					"SELECT b.lod2_geometry_id " + 
					"FROM BUILDING b " + 
					"WHERE " +  
						"b.id = ? " +
						"AND b.lod2_geometry_id IS NOT NULL " +
					"UNION " + 
					"SELECT ts.lod2_multi_surface_id " + 
					"FROM THEMATIC_SURFACE ts " + 
					"WHERE " +  
			  			"ts.building_id = ? " +
						"AND ts.lod2_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT bi.lod2_geometry_id " + 
					"FROM BUILDING_INSTALLATION bi " + 
					"WHERE " +  
						"bi.building_id = ? " +
						"AND bi.lod2_geometry_id IS NOT NULL)";

		private static final String BUILDING_PART_FOOTPRINT_LOD2 =
			"SELECT sg.geometry " +
			"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts " +
			"WHERE " +
				"ts.building_id = ? " +
				"AND ts.type = 'GroundSurface' " +
				"AND sg.root_id = ts.lod2_multi_surface_id " +
				"AND sg.geometry IS NOT NULL " +
			"ORDER BY ts.building_id";

		private static final String BUILDING_PART_GEOMETRY_LOD1 =
			"SELECT sg.geometry, NULL as type, sg.id " +
			"FROM SURFACE_GEOMETRY sg, BUILDING b " +
			"WHERE " +
				"b.id = ? " +
				"AND sg.root_id = b.lod1_geometry_id " +
				"AND sg.geometry IS NOT NULL "; // +
//			"ORDER BY b.id";

		private static final String BUILDING_PART_GEOMETRY_HIGHLIGHTING_LOD1 =
			"SELECT sg.geometry, sg.id " +
			"FROM SURFACE_GEOMETRY sg, BUILDING b " +
			"WHERE " +
				"b.id = ? " +
				"AND sg.root_id = b.lod1_geometry_id " +
				"AND sg.geometry IS NOT NULL ";

		private static final String BUILDING_PART_GEOMETRY_HIGHLIGHTING_LOD2 =
			"SELECT sg.geometry, sg.id " +
			"FROM SURFACE_GEOMETRY sg " +
			"WHERE " +
				"sg.geometry IS NOT NULL " +
				"AND sg.root_id IN (" +
					"SELECT b.lod2_geometry_id " + 
					"FROM BUILDING b " + 
					"WHERE " +  
						"b.id = ? " +
						"AND b.lod2_geometry_id IS NOT NULL " +
					"UNION " + 
					"SELECT ts.lod2_multi_surface_id " + 
					"FROM THEMATIC_SURFACE ts " + 
					"WHERE " +  
						"ts.building_id = ? " +
						"AND ts.lod2_multi_surface_id IS NOT NULL)";

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
					"SELECT b.lod4_geometry_id " + 
					"FROM BUILDING b " + 
					"WHERE " +  
						"b.id = ? " +
						"AND b.lod4_geometry_id IS NOT NULL " +
					"UNION " + 
					"SELECT ts.lod4_multi_surface_id " + 
					"FROM THEMATIC_SURFACE ts " + 
					"WHERE " +  
						"ts.building_id = ? " +
						"AND ts.lod4_multi_surface_id IS NOT NULL " +
					"UNION " + 
					"SELECT o.lod4_multi_surface_id " + 
					"FROM THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE o2ts, OPENING o " + 
					"WHERE " +  
						"ts.building_id = ? " +
						"AND ts.lod4_multi_surface_id IS NOT NULL " +
						"AND o2ts.thematic_surface_id = ts.id " +
						"AND o.id = o2ts.opening_id)";


	    private static final String BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD2_OR_HIGHER =
			"SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (" +

			"SELECT * FROM (" +
			"SELECT * FROM (" +
			
	    	"SELECT geodb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
//	    	"SELECT geodb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
//			"SELECT geodb_util.to_2d(sg.geometry, (select srid from database_srs)) AS simple_geom " +
//			"SELECT sg.geometry AS simple_geom " +
			"FROM SURFACE_GEOMETRY sg " +
			"WHERE " +
			  "sg.root_id IN( " +
			     "SELECT b.lod<LoD>_geometry_id " +
			     "FROM BUILDING b " +
			     "WHERE "+
			       "b.id = ? " +
			       "AND b.lod<LoD>_geometry_id IS NOT NULL " +
			     "UNION " +
			     "SELECT ts.lod<LoD>_multi_surface_id " +
			     "FROM THEMATIC_SURFACE ts " +
			     "WHERE "+
			       "ts.building_id = ? " +
			       "AND ts.lod<LoD>_multi_surface_id IS NOT NULL "+
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

	    private static final String BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1 =
			"SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom " +
			"FROM (" +

			"SELECT * FROM (" +
			"SELECT * FROM (" +
			
	    	"SELECT geodb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
//	    	"SELECT geodb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
//			"SELECT geodb_util.to_2d(sg.geometry, (select srid from database_srs)) AS simple_geom " +
//			"SELECT sg.geometry AS simple_geom " +
			"FROM SURFACE_GEOMETRY sg " +
			"WHERE " +
			  "sg.root_id IN( " +
			     "SELECT b.lod<LoD>_geometry_id " +
			     "FROM BUILDING b " +
			     "WHERE "+
			       "b.id = ? " +
			       "AND b.lod<LoD>_geometry_id IS NOT NULL " +
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

	    public static String getBuildingPartAggregateGeometries (double tolerance,
	    													 int srid2D,
	    													 int lodToExportFrom,
	    													 double groupBy1,
	    													 double groupBy2,
	    													 double groupBy3) {
	    	if (lodToExportFrom > 1) {
	    	   	return BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD2_OR_HIGHER.replace("<TOLERANCE>", String.valueOf(tolerance))
	    	   															   .replace("<2D_SRID>", String.valueOf(srid2D))
	    	   															   .replace("<LoD>", String.valueOf(lodToExportFrom))
	    	   															   .replace("<GROUP_BY_1>", String.valueOf(groupBy1))
	    	   															   .replace("<GROUP_BY_2>", String.valueOf(groupBy2))
	    	   															   .replace("<GROUP_BY_3>", String.valueOf(groupBy3));
	    	}
	    	// else
		   	return BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1.replace("<TOLERANCE>", String.valueOf(tolerance))
			   												 .replace("<2D_SRID>", String.valueOf(srid2D))
			   												 .replace("<LoD>", String.valueOf(lodToExportFrom))
			   												 .replace("<GROUP_BY_1>", String.valueOf(groupBy1))
			   												 .replace("<GROUP_BY_2>", String.valueOf(groupBy2))
			   												 .replace("<GROUP_BY_3>", String.valueOf(groupBy3));
	    }

		private static final String BUILDING_PART_FOOTPRINT_LOD1 =
			BUILDING_PART_GET_AGGREGATE_GEOMETRIES_FOR_LOD1.replace("<TOLERANCE>", "0.001")
						 							  .replace("<2D_SRID>", "(SELECT SRID FROM DATABASE_SRS)")
						 							  .replace("<LoD>", "1")
						 							  .replace("<GROUP_BY_1>", "256")
						 							  .replace("<GROUP_BY_2>", "64")
						 							  .replace("<GROUP_BY_3>", "16");

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
	    	buildingPartQueriesLod1.put(DisplayForm.FOOTPRINT, BUILDING_PART_FOOTPRINT_LOD1);
	    	buildingPartQueriesLod1.put(DisplayForm.EXTRUDED, BUILDING_PART_FOOTPRINT_LOD1);
	    	buildingPartQueriesLod1.put(DisplayForm.GEOMETRY, BUILDING_PART_GEOMETRY_LOD1);
	    	buildingPartQueriesLod1.put(DisplayForm.COLLADA, BUILDING_PART_COLLADA_LOD1_ROOT_IDS);
	    }

	    public static String getBuildingPartQuery (int lodToExportFrom, DisplayForm displayForm) {
	    	String query = null;
	    	switch (lodToExportFrom) {
	    		case 1:
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
	    	
//	    	Logger.getInstance().log(LogLevelType.DEBUG, query);
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
	// CITY OBJECT GROUP QUERIES
	// ----------------------------------------------------------------------
	
	public static final String CITYOBJECTGROUP_FOOTPRINT =
		"SELECT sg.geometry " +
		"FROM SURFACE_GEOMETRY sg, CITYOBJECTGROUP cog " +
		"WHERE " +
			"cog.id = ? " +
			"AND sg.root_id = cog.surface_geometry_id " +
			"AND sg.geometry IS NOT NULL ";

	public static final String CITYOBJECTGROUP_MEMBERS = 
		"SELECT co.id, co.gmlid, co.class_id " + 
		"FROM CITYOBJECT co " +
		"WHERE co.ID IN (SELECT g2co.cityobject_id "+  
		                "FROM GROUP_TO_CITYOBJECT g2co "+ 
		                "WHERE g2co.cityobjectgroup_id = ?) " +
   		"ORDER BY co.class_id";

	public static final String CITYOBJECTGROUP_MEMBERS_IN_BBOX = 
		"SELECT co.id, co.gmlid, co.class_id " + 
		"FROM CITYOBJECT co " +
		"WHERE co.ID IN (SELECT g2co.cityobject_id "+  
		                "FROM GROUP_TO_CITYOBJECT g2co "+ 
		                "WHERE g2co.cityobjectgroup_id = ?) " +
		"AND (SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2002, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?,?,?)), 'mask=overlapbdydisjoint') ='TRUE') " +
		"UNION ALL " +
		"SELECT co.id, co.gmlid, co.class_id " + 
		"FROM CITYOBJECT co " +
		"WHERE co.ID IN (SELECT g2co.cityobject_id "+  
		                "FROM GROUP_TO_CITYOBJECT g2co "+ 
		                "WHERE g2co.cityobjectgroup_id = ?) " +
		"AND (SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2003, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), 'mask=inside+coveredby') ='TRUE') " +
		"UNION ALL " +
		"SELECT co.id, co.gmlid, co.class_id " + 
		"FROM CITYOBJECT co " +
		"WHERE co.ID IN (SELECT g2co.cityobject_id "+  
		                "FROM GROUP_TO_CITYOBJECT g2co "+ 
		                "WHERE g2co.cityobjectgroup_id = ?) " +
		"AND (SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2003, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), 'mask=equal') ='TRUE') " +
		"ORDER BY 3"; // ORDER BY co.class_id
	
	// ----------------------------------------------------------------------
	// SOLITARY VEGETATION OBJECT QUERIES
	// ----------------------------------------------------------------------
	
	private static final String SOLITARY_VEGETATION_OBJECT_BASIS_DATA =
		"SELECT ig.relative_geometry_id, svo.lod<LoD>_implicit_ref_point, " +
			   "svo.lod<LoD>_implicit_transformation, svo.lod<LoD>_geometry_id " +
		"FROM SOLITARY_VEGETAT_OBJECT svo " + 
		"LEFT JOIN IMPLICIT_GEOMETRY ig ON ig.id = svo.lod<LoD>_implicit_rep_id " + 
		"WHERE svo.id = ?";

    public static String getSolitaryVegetationObjectBasisData (int lodToExportFrom) {
    	return SOLITARY_VEGETATION_OBJECT_BASIS_DATA.replace("<LoD>", String.valueOf(lodToExportFrom));
    }

	private static final String SOLITARY_VEGETATION_OBJECT_FOOTPRINT_EXTRUDED_GEOMETRY =
		"SELECT sg.geometry, 'Vegetation' as type, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE sg.root_id = ? " + 
		"AND sg.geometry IS NOT NULL";
	
	private static final String SOLITARY_VEGETATION_OBJECT_COLLADA_ROOT_IDS =
		"SELECT ? FROM DUAL "; // dummy

    public static String getSolitaryVegetationObjectGeometryContents (DisplayForm displayForm) {
    	String query = null;
    	switch (displayForm.getForm()) {
    		case DisplayForm.FOOTPRINT:
    		case DisplayForm.EXTRUDED:
    		case DisplayForm.GEOMETRY:
    			query = SOLITARY_VEGETATION_OBJECT_FOOTPRINT_EXTRUDED_GEOMETRY;
    	    	break;
    		case DisplayForm.COLLADA:
    			query = SOLITARY_VEGETATION_OBJECT_COLLADA_ROOT_IDS;
    	    	break;
    	    default:
    	    	Logger.getInstance().log(LogLevel.INFO, "No solitary vegetation object query found");
    	}
    	
//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
    	return query;
    }

    private static final String SOLITARY_VEGETATION_OBJECT_GEOMETRY_HIGHLIGHTING =
		"SELECT sg.geometry, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE sg.root_id IN ( " +
			"SELECT ig.relative_geometry_id " + 
			"FROM SOLITARY_VEGETAT_OBJECT svo, IMPLICIT_GEOMETRY ig " + 
			"WHERE svo.id = ? " +
				"AND ig.id = svo.lod<LoD>_implicit_rep_id " +
			"UNION " +
			"SELECT svo.lod<LoD>_geometry_id " +
			"FROM SOLITARY_VEGETAT_OBJECT svo " + 
			"WHERE svo.id = ?) " +
		"AND sg.geometry IS NOT NULL";

    public static String getSolitaryVegetationObjectHighlightingQuery (int lodToExportFrom) {
    	return SOLITARY_VEGETATION_OBJECT_GEOMETRY_HIGHLIGHTING.replace("<LoD>", String.valueOf(lodToExportFrom));
    }

	// ----------------------------------------------------------------------
	// PLANT COVER QUERIES
	// ----------------------------------------------------------------------
	
	private static final String PLANT_COVER_FOOTPRINT_EXTRUDED_GEOMETRY =
		"SELECT sg.geometry, 'Vegetation' as type, sg.id " +
		"FROM SURFACE_GEOMETRY sg, PLANT_COVER pc " + 
		"WHERE pc.id = ? " +
			"AND sg.root_id = pc.lod<LoD>_geometry_id " + 
			"AND sg.geometry IS NOT NULL";
	
	private static final String PLANT_COVER_COLLADA_ROOT_IDS =
		"SELECT pc.lod<LoD>_geometry_id " +
		"FROM PLANT_COVER pc " + 
		"WHERE pc.id = ?";

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
    	
//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
    	return query.replace("<LoD>", String.valueOf(lodToExportFrom));
    }

    public static String getPlantCoverHighlightingQuery (int lodToExportFrom) {
    	return PLANT_COVER_FOOTPRINT_EXTRUDED_GEOMETRY.replace("<LoD>", String.valueOf(lodToExportFrom));
    }

	// ----------------------------------------------------------------------
	// GENERIC CITY OBJECT QUERIES
	// ----------------------------------------------------------------------
	
	private static final String GENERIC_CITYOBJECT_BASIS_DATA =
		"SELECT ig.relative_geometry_id, gco.lod<LoD>_implicit_ref_point, " +
			   "gco.lod<LoD>_implicit_transformation, gco.lod<LoD>_geometry_id " +
		"FROM GENERIC_CITYOBJECT gco " +
		"LEFT JOIN IMPLICIT_GEOMETRY ig ON ig.id = gco.lod<LoD>_implicit_rep_id " + 
		"WHERE gco.id = ?";

    public static String getGenericCityObjectBasisData (int lodToExportFrom) {
    	return GENERIC_CITYOBJECT_BASIS_DATA.replace("<LoD>", String.valueOf(lodToExportFrom));
    }

	private static final String GENERIC_CITYOBJECT_FOOTPRINT_EXTRUDED_GEOMETRY =
		"SELECT sg.geometry, 'Generic' as type, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE sg.root_id = ? " + 
		"AND sg.geometry IS NOT NULL";
	
	private static final String GENERIC_CITYOBJECT_COLLADA_ROOT_IDS =
		"SELECT ? FROM DUAL "; // dummy

    public static String getGenericCityObjectGeometryContents (DisplayForm displayForm) {
    	String query = null;
    	switch (displayForm.getForm()) {
    		case DisplayForm.FOOTPRINT:
    		case DisplayForm.EXTRUDED:
    		case DisplayForm.GEOMETRY:
    			query = GENERIC_CITYOBJECT_FOOTPRINT_EXTRUDED_GEOMETRY;
    	    	break;
    		case DisplayForm.COLLADA:
    			query = GENERIC_CITYOBJECT_COLLADA_ROOT_IDS;
    	    	break;
    	    default:
    	    	Logger.getInstance().log(LogLevel.INFO, "No GenericCityObject query found");
    	}
    	
//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
    	return query;
    }

    private static final String GENERIC_CITYOBJECT_GEOMETRY_HIGHLIGHTING =
		"SELECT sg.geometry, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE sg.root_id IN ( " +
			"SELECT ig.relative_geometry_id " + 
			"FROM GENERIC_CITYOBJECT gco, IMPLICIT_GEOMETRY ig " + 
			"WHERE gco.id = ? " +
				"AND ig.id = gco.lod<LoD>_implicit_rep_id " +
			"UNION " +
			"SELECT gco.lod<LoD>_geometry_id " +
			"FROM GENERIC_CITYOBJECT gco " + 
			"WHERE gco.id = ?) " +
		"AND sg.geometry IS NOT NULL";

    public static String getGenericCityObjectHighlightingQuery (int lodToExportFrom) {
    	return GENERIC_CITYOBJECT_GEOMETRY_HIGHLIGHTING.replace("<LoD>", String.valueOf(lodToExportFrom));
    }

	// ----------------------------------------------------------------------
	// CITY FURNITURE QUERIES
	// ----------------------------------------------------------------------
	
	private static final String CITY_FURNITURE_BASIS_DATA =
		"SELECT ig.relative_geometry_id, cf.lod<LoD>_implicit_ref_point, " +
			   "cf.lod<LoD>_implicit_transformation, cf.lod<LoD>_geometry_id " +
		"FROM CITY_FURNITURE cf " + 
		"LEFT JOIN IMPLICIT_GEOMETRY ig ON ig.id = cf.lod<LoD>_implicit_rep_id " + 
		"WHERE cf.id = ?";

    public static String getCityFurnitureBasisData (int lodToExportFrom) {
    	return CITY_FURNITURE_BASIS_DATA.replace("<LoD>", String.valueOf(lodToExportFrom));
    }

	private static final String CITY_FURNITURE_FOOTPRINT_EXTRUDED_GEOMETRY =
		"SELECT sg.geometry, 'Furniture' as type, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE sg.root_id = ? " + 
		"AND sg.geometry IS NOT NULL";
	
	private static final String CITY_FURNITURE_COLLADA_ROOT_IDS =
		"SELECT ? FROM DUAL "; // dummy

    public static String getCityFurnitureGeometryContents (DisplayForm displayForm) {
    	String query = null;
    	switch (displayForm.getForm()) {
    		case DisplayForm.FOOTPRINT:
    		case DisplayForm.EXTRUDED:
    		case DisplayForm.GEOMETRY:
    			query = CITY_FURNITURE_FOOTPRINT_EXTRUDED_GEOMETRY;
    	    	break;
    		case DisplayForm.COLLADA:
    			query = CITY_FURNITURE_COLLADA_ROOT_IDS;
    	    	break;
    	    default:
    	    	Logger.getInstance().log(LogLevel.INFO, "No city furniture query found");
    	}
    	
//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
    	return query;
    }

    private static final String CITY_FURNITURE_GEOMETRY_HIGHLIGHTING =
		"SELECT sg.geometry, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE sg.root_id IN ( " +
			"SELECT ig.relative_geometry_id " + 
			"FROM CITY_FURNITURE cf, IMPLICIT_GEOMETRY ig " + 
			"WHERE cf.id = ? " +
				"AND ig.id = cf.lod<LoD>_implicit_rep_id " +
			"UNION " +
			"SELECT cf.lod<LoD>_geometry_id " +
			"FROM CITY_FURNITURE cf " + 
			"WHERE cf.id = ?) " +
		"AND sg.geometry IS NOT NULL";

    public static String getCityFurnitureHighlightingQuery (int lodToExportFrom) {
    	return CITY_FURNITURE_GEOMETRY_HIGHLIGHTING.replace("<LoD>", String.valueOf(lodToExportFrom));
    }
    
	// ----------------------------------------------------------------------
	// WATER BODY QUERIES
	// ----------------------------------------------------------------------
	
	private static final String WATERBODY_LOD1_ROOT_IDS =
			"SELECT wb.lod<LoD>_solid_id " +
			"FROM WATERBODY wb " +
			"WHERE wb.id = ? " +
				"AND wb.lod<LoD>_solid_id IS NOT NULL";

	private static final String WATERBODY_ROOT_IDS =
			WATERBODY_LOD1_ROOT_IDS +
			" UNION " +
			"SELECT wbs.lod<LoD>_surface_id " + // min lod value here is 2
			"FROM WATERBOD_TO_WATERBND_SRF wb2wbs, WATERBOUNDARY_SURFACE wbs " +
			"WHERE wb2wbs.waterbody_id = ? " +
				"AND wbs.id = wb2wbs.waterboundary_surface_id " +
				"AND wbs.lod<LoD>_surface_id IS NOT NULL";

	private static final String WATERBODY_FOOTPRINT_EXTRUDED_GEOMETRY =
		"SELECT sg.geometry, 'Water' as type, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE sg.root_id IN (" + WATERBODY_ROOT_IDS +
			") AND sg.geometry IS NOT NULL";
	
	private static final String WATERBODY_FOOTPRINT_EXTRUDED_GEOMETRY_LOD1 =
		"SELECT sg.geometry, 'Water' as type, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE sg.root_id IN (" +
			"SELECT wb.lod1_solid_id " +
			"FROM WATERBODY wb " +
			"WHERE wb.id = ? " +
			"UNION " +
			"SELECT wb.lod1_multi_surface_id " +
			"FROM WATERBODY wb " +
			"WHERE wb.id = ? " +
		") AND sg.geometry IS NOT NULL " +
		"UNION " +
		"SELECT wb.lod1_multi_curve " +
		"FROM WATERBODY wb " +
		"WHERE wb.id = ? ";
	
	private static final String WATERBODY_FOOTPRINT_LOD0 =
		"SELECT sg.geometry, 'Water' as type, sg.id " +
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
    	
//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
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
		"SELECT sg.geometry, 'LandUse' as type, sg.id " +
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
    	
//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
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
			TRANSPORTATION_COMPLEX_LOD1_ROOT_IDS +
			" UNION " +
			"SELECT ta.lod<LoD>_multi_surface_id " + // min lod value here is 2
			"FROM TRAFFIC_AREA ta " +
			"WHERE ta.transportation_complex_id = ? " +
				"AND ta.lod<LoD>_multi_surface_id IS NOT NULL";

	private static final String TRANSPORTATION_COMPLEX_FOOTPRINT_EXTRUDED_GEOMETRY =
		"SELECT sg.geometry, 'Transportation' as type, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE sg.root_id IN (" + TRANSPORTATION_COMPLEX_ROOT_IDS +
			") AND sg.geometry IS NOT NULL";
		
	private static final String TRANSPORTATION_COMPLEX_FOOTPRINT_EXTRUDED_GEOMETRY_LOD1 =
		"SELECT sg.geometry, 'Transportation' as type, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE sg.root_id IN (" +
			"SELECT tc.lod1_multi_surface_id " +
			"FROM TRANSPORTATION_COMPLEX tc " +
			"WHERE tc.id = ? " +
				"AND tc.lod1_multi_surface_id IS NOT NULL" +
		") AND sg.geometry IS NOT NULL";

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
		"SELECT sg.geometry, 'Relief' as type, sg.id " +
		"FROM RELIEF_FEATURE rf, RELIEF_FEAT_TO_REL_COMP rf2rc, TIN_RELIEF tr, SURFACE_GEOMETRY sg " +
		"WHERE rf.id = ? " +
	   		"AND rf.lod = <LoD> " +
	   		"AND rf2rc.relief_feature_id = rf.id " +
	   		"AND tr.id = rf2rc.relief_component_id " +
			"AND sg.root_id = tr.surface_geometry_id " + 
			"AND sg.geometry IS NOT NULL";
	
	public static final int RELIEF_TIN_BREAK_LINES_QUERY = 1;
	private static final String RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_TIN_BREAK_LINES =
		"SELECT tr.break_lines, 'Relief' as type, -1 " +
		"FROM RELIEF_FEATURE rf, RELIEF_FEAT_TO_REL_COMP rf2rc, TIN_RELIEF tr " +
		"WHERE rf.id = ? " +
			"AND rf.lod = <LoD> " +
			"AND rf2rc.relief_feature_id = rf.id " +
			"AND tr.id = rf2rc.relief_component_id " +
			"AND tr.break_lines IS NOT NULL";

	public static final int RELIEF_TIN_STOP_LINES_QUERY = 2;
	private static final String RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_TIN_STOP_LINES =
		"SELECT tr.stop_lines, 'Relief' as type, -1 " +
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
		"SELECT br.break_lines, 'Relief' as type, -1 " +
		"FROM RELIEF_FEATURE rf, RELIEF_FEAT_TO_REL_COMP rf2rc, BREAKLINE_RELIEF br " +
		"WHERE rf.id = ? " +
			"AND rf.lod = <LoD> " +
			"AND rf2rc.relief_feature_id = rf.id " +
			"AND br.id = rf2rc.relief_component_id " +
			"AND br.break_lines IS NOT NULL";

	public static final int RELIEF_BREAK_RIDGE_OR_VALLEY_LINES_QUERY = 4;
	private static final String RELIEF_FOOTPRINT_EXTRUDED_GEOMETRY_BREAK_RIDGE_OR_VALLEY_LINES =
		"SELECT br.ridge_or_valley_lines, 'Relief' as type, -1 " +
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
