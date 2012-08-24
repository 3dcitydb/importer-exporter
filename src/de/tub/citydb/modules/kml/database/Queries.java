/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2012
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

    public static final String GET_GMLIDS =
    	"SELECT co.gmlid, co.class_id " +
		"FROM CITYOBJECT co " +
		"WHERE " +
		  "(SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2002, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?,?,?)), 'mask=overlapbdydisjoint') ='TRUE') " +
		"UNION ALL " +
    	"SELECT co.gmlid, co.class_id " +
		"FROM CITYOBJECT co " +
		"WHERE " +
		  "(SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2003, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), 'mask=inside+coveredby') ='TRUE') " +
		"UNION ALL " +
    	"SELECT co.gmlid, co.class_id " +
		"FROM CITYOBJECT co " +
		"WHERE " +
		  "(SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2003, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), 'mask=equal') ='TRUE') " +
		"ORDER BY 2"; // ORDER BY co.class_id

    public static final String GET_OBJECTCLASS =
		"SELECT co.class_id " +
		"FROM CITYOBJECT co " +
		"WHERE co.gmlid = ?";

	public static final String GET_EXTRUDED_HEIGHT =
		"SELECT " + // "b.measured_height, " +
		"SDO_GEOM.SDO_MAX_MBR_ORDINATE(co.envelope, 3) - SDO_GEOM.SDO_MIN_MBR_ORDINATE(co.envelope, 3) AS envelope_measured_height " +
		"FROM CITYOBJECT co " + // ", BUILDING b " +
		"WHERE " +
			"co.gmlid = ?"; // + " AND b.building_root_id = co.id";

	public static final String GET_STRVAL_GENERICATTRIB_FROM_GML_ID =
		"SELECT coga.strval " +
		"FROM CITYOBJECT co " + 
			"LEFT JOIN CITYOBJECT_GENERICATTRIB coga ON (coga.cityobject_id = co.id AND coga.attrname = ?) " +
		"WHERE co.gmlid = ?";

	public static final String INSERT_GE_ZOFFSET =
		"INSERT INTO CITYOBJECT_GENERICATTRIB (ID, ATTRNAME, DATATYPE, STRVAL, CITYOBJECT_ID) " +
		"VALUES (CITYOBJECT_GENERICATT_SEQ.NEXTVAL, ?, 1, ?, (SELECT ID FROM CITYOBJECT WHERE gmlid = ?))";
	
	public static final String TRANSFORM_GEOMETRY_TO_WGS84 =
		"SELECT SDO_CS.TRANSFORM(?, 4326) FROM DUAL";

	public static final String TRANSFORM_GEOMETRY_TO_WGS84_3D =
		"SELECT SDO_CS.TRANSFORM(?, 4329) FROM DUAL";

	public static final String GET_ENVELOPE_IN_WGS84_FROM_GML_ID =
		"SELECT SDO_CS.TRANSFORM(co.envelope, 4326) " +
		"FROM CITYOBJECT co " +
		"WHERE co.gmlid = ?";

	public static final String GET_ENVELOPE_IN_WGS84_3D_FROM_GML_ID =
		"SELECT SDO_CS.TRANSFORM(co.envelope, 4329) " +
		"FROM CITYOBJECT co " +
		"WHERE co.gmlid = ?";

	public static final String GET_CENTROID_IN_WGS84_FROM_GML_ID =
		"SELECT SDO_CS.TRANSFORM(SDO_GEOM.SDO_CENTROID(co.envelope, 0.001), 4326) " +
		"FROM CITYOBJECT co " +
		"WHERE co.gmlid = ?";

	public static final String GET_CENTROID_IN_WGS84_3D_FROM_GML_ID =
		"SELECT SDO_CS.TRANSFORM(SDO_GEOM.SDO_CENTROID(co.envelope, 0.001), 4329) " +
		"FROM CITYOBJECT co " +
		"WHERE co.gmlid = ?";
	
	public static final String GET_CENTROID_LAT_IN_WGS84_FROM_GML_ID =
		"SELECT v.Y FROM TABLE(" +
			"SELECT SDO_UTIL.GETVERTICES(SDO_CS.TRANSFORM(SDO_GEOM.SDO_CENTROID(co.envelope, 0.001), 4326)) " +
			"FROM CITYOBJECT co " + 
			"WHERE co.gmlid = ?) v";

	public static final String GET_CENTROID_LAT_IN_WGS84_3D_FROM_GML_ID =
		"SELECT v.Y FROM TABLE(" +
			"SELECT SDO_UTIL.GETVERTICES(SDO_CS.TRANSFORM(SDO_GEOM.SDO_CENTROID(co.envelope, 0.001), 4329)) " +
			"FROM CITYOBJECT co " + 
			"WHERE co.gmlid = ?) v";

	public static final String GET_CENTROID_LON_IN_WGS84_FROM_GML_ID =
		"SELECT v.X FROM TABLE(" +
			"SELECT SDO_UTIL.GETVERTICES(SDO_CS.TRANSFORM(SDO_GEOM.SDO_CENTROID(co.envelope, 0.001), 4326)) " +
			"FROM CITYOBJECT co " + 
			"WHERE co.gmlid = ?) v";

	public static final String GET_CENTROID_LON_IN_WGS84_3D_FROM_GML_ID =
		"SELECT v.X FROM TABLE(" +
			"SELECT SDO_UTIL.GETVERTICES(SDO_CS.TRANSFORM(SDO_GEOM.SDO_CENTROID(co.envelope, 0.001), 4329)) " +
			"FROM CITYOBJECT co " + 
			"WHERE co.gmlid = ?) v";

	// ----------------------------------------------------------------------
	// 	BUILDING QUERIES
	// ----------------------------------------------------------------------

	private static final String BUILDING_FOOTPRINT_LOD4 =
		"SELECT sg.geometry " +
		"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND ts.building_id = co.id " +
			"AND ts.type = 'GroundSurface' " +
			"AND sg.root_id = ts.lod4_multi_surface_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY ts.building_id";

	private static final String BUILDING_COLLADA_LOD4_ROOT_IDS =
		"SELECT b.lod4_geometry_id " + 
		"FROM CITYOBJECT co, BUILDING b " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND b.lod4_geometry_id IS NOT NULL " +
		"UNION " + 
		"SELECT ts.lod4_multi_surface_id " + 
		"FROM CITYOBJECT co, BUILDING b, THEMATIC_SURFACE ts " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND ts.building_id = b.id " +
			"AND ts.lod4_multi_surface_id IS NOT NULL " +
		"UNION " + 
/*
		"SELECT r.lod4_geometry_id " + 
		"FROM CITYOBJECT co, BUILDING b, ROOM r " + 
		"WHERE " +  
			"co.gmlid = ? " +
  			"AND b.building_root_id = co.id " +
			"AND r.building_id = b.id " +
			"AND r.lod4_geometry_id IS NOT NULL " +
*/
		"SELECT ts.lod4_multi_surface_id " + 
		"FROM CITYOBJECT co, BUILDING b, ROOM r, THEMATIC_SURFACE ts " + 
		"WHERE " +  
			"co.gmlid = ? " +
  			"AND b.building_root_id = co.id " +
			"AND r.building_id = b.id " +
			"AND ts.room_id = r.id " +
			"AND ts.lod4_multi_surface_id IS NOT NULL " +
		"UNION " + 
		"SELECT bf.lod4_geometry_id " + 
		"FROM CITYOBJECT co, BUILDING b, ROOM r, BUILDING_FURNITURE bf " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND r.building_id = b.id " +
			"AND bf.room_id = r.id " +
			"AND bf.lod4_geometry_id IS NOT NULL " +
		"UNION " + 
		"SELECT bi.lod4_geometry_id " + 
		"FROM CITYOBJECT co, BUILDING b, ROOM r, BUILDING_INSTALLATION bi " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND r.building_id = b.id " +
			"AND bi.room_id = r.id " +
			"AND bi.lod4_geometry_id IS NOT NULL " +
		"UNION " + 
		"SELECT o.lod4_multi_surface_id " + 
		"FROM CITYOBJECT co, BUILDING b, THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE o2ts, OPENING o " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND ts.building_id = b.id " +
			"AND ts.lod4_multi_surface_id IS NOT NULL " +
			"AND o2ts.thematic_surface_id = ts.id " +
			"AND o.id = o2ts.opening_id";
			
	private static final String BUILDING_GEOMETRY_LOD4 =
		"SELECT sg.geometry, ts.type, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"LEFT JOIN THEMATIC_SURFACE ts ON ts.lod4_multi_surface_id = sg.root_id " +
		"WHERE " +
			"sg.geometry IS NOT NULL " +
			"AND sg.root_id IN (" + BUILDING_COLLADA_LOD4_ROOT_IDS + ")";

	private static final String BUILDING_FOOTPRINT_LOD3 =
		"SELECT sg.geometry " +
		"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND ts.building_id = co.id " +
			"AND ts.type = 'GroundSurface' " +
			"AND sg.root_id = ts.lod3_multi_surface_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY ts.building_id";

	private static final String BUILDING_COLLADA_LOD3_ROOT_IDS =
		"SELECT b.lod3_geometry_id " + 
		"FROM CITYOBJECT co, BUILDING b " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND b.lod3_geometry_id IS NOT NULL " +
		"UNION " + 
		"SELECT ts.lod3_multi_surface_id " + 
		"FROM CITYOBJECT co, BUILDING b, THEMATIC_SURFACE ts " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND ts.building_id = b.id " +
			"AND ts.lod3_multi_surface_id IS NOT NULL " +
		"UNION " + 
		"SELECT bi.lod3_geometry_id " + 
		"FROM CITYOBJECT co, BUILDING b, BUILDING_INSTALLATION bi " + 
		"WHERE " +  
			"co.gmlid = ? " +
  			"AND b.building_root_id = co.id " +
			"AND bi.building_id = b.id " +
			"AND bi.lod3_geometry_id IS NOT NULL " +
		"UNION " + 
		"SELECT o.lod3_multi_surface_id " + 
		"FROM CITYOBJECT co, BUILDING b, THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE o2ts, OPENING o " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND ts.building_id = b.id " +
			"AND ts.lod3_multi_surface_id IS NOT NULL " +
			"AND o2ts.thematic_surface_id = ts.id " +
			"AND o.id = o2ts.opening_id";

	private static final String BUILDING_GEOMETRY_LOD3 =
		"SELECT sg.geometry, ts.type, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"LEFT JOIN THEMATIC_SURFACE ts ON ts.lod3_multi_surface_id = sg.root_id " +
		"WHERE " +
			"sg.geometry IS NOT NULL " +
			"AND sg.root_id IN (" + BUILDING_COLLADA_LOD3_ROOT_IDS	+ ")";

	private static final String BUILDING_COLLADA_LOD2_ROOT_IDS =
		"SELECT b.lod2_geometry_id " + 
		"FROM CITYOBJECT co, BUILDING b " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND b.lod2_geometry_id IS NOT NULL " +
		"UNION " + 
		"SELECT ts.lod2_multi_surface_id " + 
		"FROM CITYOBJECT co, BUILDING b, THEMATIC_SURFACE ts " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND ts.building_id = b.id " +
			"AND ts.lod2_multi_surface_id IS NOT NULL " +
		"UNION " + 
		"SELECT bi.lod2_geometry_id " + 
		"FROM CITYOBJECT co, BUILDING b, BUILDING_INSTALLATION bi " + 
		"WHERE " +  
			"co.gmlid = ? " +
  			"AND b.building_root_id = co.id " +
			"AND bi.building_id = b.id " +
			"AND bi.lod2_geometry_id IS NOT NULL";

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
//			"AND (a.theme = ? OR a.theme IS NULL) " +
//			"ORDER BY sg.parent_id ASC"; // own root surfaces first

	public static final String[] COLLADA_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID = new String[] {
		COLLADA_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID_0 + "AND sg.geometry IS NULL", // parents
		COLLADA_GEOMETRY_AND_APPEARANCE_FROM_ROOT_ID_0 + "AND sg.geometry IS NOT NULL" // elementary surfaces
	};

	private static final String BUILDING_GEOMETRY_LOD2 =
		"SELECT sg.geometry, ts.type, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"LEFT JOIN THEMATIC_SURFACE ts ON ts.lod2_multi_surface_id = sg.root_id " +
		"WHERE " +
			"sg.geometry IS NOT NULL " +
			"AND sg.root_id IN (" +
				"SELECT b.lod2_geometry_id " + 
				"FROM CITYOBJECT co, BUILDING b " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.building_root_id = co.id " +
					"AND b.lod2_geometry_id IS NOT NULL " +
				"UNION " + 
				"SELECT ts.lod2_multi_surface_id " + 
				"FROM CITYOBJECT co, THEMATIC_SURFACE ts, BUILDING b " + 
				"WHERE " +  
					"co.gmlid = ? " +
		  			"AND b.building_root_id = co.id " +
					"AND ts.building_id = b.id " +
					"AND ts.lod2_multi_surface_id IS NOT NULL " +
				"UNION " + 
				"SELECT bi.lod2_geometry_id " + 
				"FROM CITYOBJECT co, BUILDING b, BUILDING_INSTALLATION bi " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.building_root_id = co.id " +
					"AND bi.building_id = b.id " +
					"AND bi.lod2_geometry_id IS NOT NULL)";

	private static final String BUILDING_FOOTPRINT_LOD2 =
		"SELECT sg.geometry " +
		"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND ts.building_id = co.id " +
			"AND ts.type = 'GroundSurface' " +
			"AND sg.root_id = ts.lod2_multi_surface_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY ts.building_id";

	private static final String BUILDING_GEOMETRY_LOD1 =
		"SELECT sg.geometry, NULL as type, sg.id " +
		"FROM SURFACE_GEOMETRY sg, CITYOBJECT co, BUILDING b " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND sg.root_id = b.lod1_geometry_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY b.id";

	private static final String BUILDING_GEOMETRY_HIGHLIGHTING_LOD1 =
		"SELECT sg.geometry, sg.id " +
		"FROM SURFACE_GEOMETRY sg, BUILDING b, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND sg.root_id = b.lod1_geometry_id " +
			"AND sg.geometry IS NOT NULL ";

	private static final String BUILDING_GEOMETRY_HIGHLIGHTING_LOD2 =
		"SELECT sg.geometry, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE " +
			"sg.geometry IS NOT NULL " +
			"AND sg.root_id IN (" +
				"SELECT b.lod2_geometry_id " + 
				"FROM CITYOBJECT co, BUILDING b " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.building_root_id = co.id " +
					"AND b.lod2_geometry_id IS NOT NULL " +
				"UNION " + 
				"SELECT ts.lod2_multi_surface_id " + 
				"FROM CITYOBJECT co, THEMATIC_SURFACE ts, BUILDING b " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.building_root_id = co.id " +
					"AND ts.building_id = b.id " +
					"AND ts.lod2_multi_surface_id IS NOT NULL)";

	private static final String BUILDING_GEOMETRY_HIGHLIGHTING_LOD3 =
		"SELECT sg.geometry, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE " +
			"sg.geometry IS NOT NULL " +
			"AND sg.root_id IN (" + BUILDING_COLLADA_LOD3_ROOT_IDS + ")";
	
	
	private static final String BUILDING_GEOMETRY_HIGHLIGHTING_LOD4 =
		"SELECT sg.geometry, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE " +
			"sg.geometry IS NOT NULL " +
			"AND sg.root_id IN (" +
				"SELECT b.lod4_geometry_id " + 
				"FROM CITYOBJECT co, BUILDING b " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.building_root_id = co.id " +
					"AND b.lod4_geometry_id IS NOT NULL " +
				"UNION " + 
				"SELECT ts.lod4_multi_surface_id " + 
				"FROM CITYOBJECT co, THEMATIC_SURFACE ts, BUILDING b " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.building_root_id = co.id " +
					"AND ts.building_id = b.id " +
					"AND ts.lod4_multi_surface_id IS NOT NULL" +
				"UNION " + 
				"SELECT o.lod4_multi_surface_id " + 
				"FROM CITYOBJECT co, BUILDING b, THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE o2ts, OPENING o " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.building_root_id = co.id " +
					"AND ts.building_id = b.id " +
					"AND ts.lod4_multi_surface_id IS NOT NULL " +
					"AND o2ts.thematic_surface_id = ts.id " +
					"AND o.id = o2ts.opening_id)";

	private static final HashMap<Integer, String> singleBuildingQueriesLod4 = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod4.put(DisplayForm.FOOTPRINT, BUILDING_FOOTPRINT_LOD4);
    	singleBuildingQueriesLod4.put(DisplayForm.EXTRUDED, BUILDING_FOOTPRINT_LOD4);
    	singleBuildingQueriesLod4.put(DisplayForm.GEOMETRY, BUILDING_GEOMETRY_LOD4);
    	singleBuildingQueriesLod4.put(DisplayForm.COLLADA, BUILDING_COLLADA_LOD4_ROOT_IDS);
    }

    private static final HashMap<Integer, String> singleBuildingQueriesLod3 = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod3.put(DisplayForm.FOOTPRINT, BUILDING_FOOTPRINT_LOD3);
    	singleBuildingQueriesLod3.put(DisplayForm.EXTRUDED, BUILDING_FOOTPRINT_LOD3);
    	singleBuildingQueriesLod3.put(DisplayForm.GEOMETRY, BUILDING_GEOMETRY_LOD3);
    	singleBuildingQueriesLod3.put(DisplayForm.COLLADA, BUILDING_COLLADA_LOD3_ROOT_IDS);
    }

    private static final HashMap<Integer, String> singleBuildingQueriesLod2 = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod2.put(DisplayForm.FOOTPRINT, BUILDING_FOOTPRINT_LOD2);
    	singleBuildingQueriesLod2.put(DisplayForm.EXTRUDED, BUILDING_FOOTPRINT_LOD2);
    	singleBuildingQueriesLod2.put(DisplayForm.GEOMETRY, BUILDING_GEOMETRY_LOD2);
    	singleBuildingQueriesLod2.put(DisplayForm.COLLADA, BUILDING_COLLADA_LOD2_ROOT_IDS);
    }

    public static String getSingleBuildingQuery (int lodToExportFrom, DisplayForm displayForm) {
    	String query = null;
    	switch (lodToExportFrom) {
    		case 1:
    	    	query = singleBuildingQueriesLod1.get(displayForm.getForm());
    	    	break;
    		case 2:
    	    	query = singleBuildingQueriesLod2.get(displayForm.getForm());
    	    	break;
    		case 3:
    	    	query = singleBuildingQueriesLod3.get(displayForm.getForm());
    	    	break;
    		case 4:
    	    	query = singleBuildingQueriesLod4.get(displayForm.getForm());
    	    	break;
    	    default:
    	    	Logger.getInstance().log(LogLevel.INFO, "No single building query found for LoD" + lodToExportFrom);
    	}
    	
//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
    	return query;
    }

    public static String getSingleBuildingHighlightingQuery (int lodToExportFrom) {
    	String query = null;
    	switch (lodToExportFrom) {
    		case 1:
    			query = BUILDING_GEOMETRY_HIGHLIGHTING_LOD1;
    	    	break;
    		case 2:
    			query = BUILDING_GEOMETRY_HIGHLIGHTING_LOD2;
    			break;
    		case 3:
    			query = BUILDING_GEOMETRY_HIGHLIGHTING_LOD3;
    	    	break;
    		case 4:
    			query = BUILDING_GEOMETRY_HIGHLIGHTING_LOD4;
    	    	break;
    	    default:
    	    	Logger.getInstance().log(LogLevel.INFO, "No single building highlighting query found for LoD" + lodToExportFrom);
    	}
    	
//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
    	return query;
    }

    private static final String BUILDING_GET_AGGREGATE_GEOMETRIES_FOR_LOD2_OR_HIGHER =
		"SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
		"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
		"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
		"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom " +
		"FROM (" +

		"SELECT * FROM (" +
		"SELECT * FROM (" +
		
    	"SELECT geodb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
//    	"SELECT geodb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
//		"SELECT geodb_util.to_2d(sg.geometry, (select srid from database_srs)) AS simple_geom " +
//		"SELECT sg.geometry AS simple_geom " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE " +
		  "sg.root_id IN( " +
		     "SELECT b.lod<LoD>_geometry_id " +
		     "FROM CITYOBJECT co, BUILDING b " +
		     "WHERE "+
		       "co.gmlid = ? " +
		       "AND b.building_root_id = co.id " +
		       "AND b.lod<LoD>_geometry_id IS NOT NULL " +
		     "UNION " +
		     "SELECT ts.lod<LoD>_multi_surface_id " +
		     "FROM CITYOBJECT co, BUILDING b, THEMATIC_SURFACE ts " +
		     "WHERE "+
		       "co.gmlid = ? " +
		       "AND b.building_root_id = co.id " +
		       "AND ts.building_id = b.id " +
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

    private static final String BUILDING_GET_AGGREGATE_GEOMETRIES_FOR_LOD1 =
		"SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
		"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
		"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom " +
		"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom " +
		"FROM (" +

		"SELECT * FROM (" +
		"SELECT * FROM (" +
		
    	"SELECT geodb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
//    	"SELECT geodb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom " +
//		"SELECT geodb_util.to_2d(sg.geometry, (select srid from database_srs)) AS simple_geom " +
//		"SELECT sg.geometry AS simple_geom " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE " +
		  "sg.root_id IN( " +
		     "SELECT b.lod<LoD>_geometry_id " +
		     "FROM CITYOBJECT co, BUILDING b " +
		     "WHERE "+
		       "co.gmlid = ? " +
		       "AND b.building_root_id = co.id " +
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

    public static String getBuildingAggregateGeometries (double tolerance,
    													 int srid2D,
    													 int lodToExportFrom,
    													 double groupBy1,
    													 double groupBy2,
    													 double groupBy3) {
    	if (lodToExportFrom > 1) {
    	   	return BUILDING_GET_AGGREGATE_GEOMETRIES_FOR_LOD2_OR_HIGHER.replace("<TOLERANCE>", String.valueOf(tolerance))
    	   															   .replace("<2D_SRID>", String.valueOf(srid2D))
    	   															   .replace("<LoD>", String.valueOf(lodToExportFrom))
    	   															   .replace("<GROUP_BY_1>", String.valueOf(groupBy1))
    	   															   .replace("<GROUP_BY_2>", String.valueOf(groupBy2))
    	   															   .replace("<GROUP_BY_3>", String.valueOf(groupBy3));
    	}
    	// else
	   	return BUILDING_GET_AGGREGATE_GEOMETRIES_FOR_LOD1.replace("<TOLERANCE>", String.valueOf(tolerance))
		   												 .replace("<2D_SRID>", String.valueOf(srid2D))
		   												 .replace("<LoD>", String.valueOf(lodToExportFrom))
		   												 .replace("<GROUP_BY_1>", String.valueOf(groupBy1))
		   												 .replace("<GROUP_BY_2>", String.valueOf(groupBy2))
		   												 .replace("<GROUP_BY_3>", String.valueOf(groupBy3));
    }

	private static final String BUILDING_FOOTPRINT_LOD1 =
		BUILDING_GET_AGGREGATE_GEOMETRIES_FOR_LOD1.replace("<TOLERANCE>", "0.001")
					 							  .replace("<2D_SRID>", "(SELECT SRID FROM DATABASE_SRS)")
					 							  .replace("<LoD>", "1")
					 							  .replace("<GROUP_BY_1>", "256")
					 							  .replace("<GROUP_BY_2>", "64")
					 							  .replace("<GROUP_BY_3>", "16");

	private static final HashMap<Integer, String> singleBuildingQueriesLod1 = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod1.put(DisplayForm.FOOTPRINT, BUILDING_FOOTPRINT_LOD1);
    	singleBuildingQueriesLod1.put(DisplayForm.EXTRUDED, BUILDING_FOOTPRINT_LOD1);
    	singleBuildingQueriesLod1.put(DisplayForm.GEOMETRY, BUILDING_GEOMETRY_LOD1);
    }
				
	// ----------------------------------------------------------------------
	// CITY OBJECT GROUP QUERIES
	// ----------------------------------------------------------------------
	
	public static final String CITYOBJECTGROUP_FOOTPRINT =
		"SELECT sg.geometry " +
		"FROM SURFACE_GEOMETRY sg, CITYOBJECTGROUP cog, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND cog.id = co.id " +
			"AND sg.root_id = cog.surface_geometry_id " +
			"AND sg.geometry IS NOT NULL ";

	public static final String CITYOBJECTGROUP_MEMBERS = 
		"SELECT co.gmlid, co.class_id " + 
		"FROM cityobject co " +
		"WHERE co.ID IN (SELECT g2co.cityobject_id "+  
		                "FROM group_to_cityobject g2co, cityobjectgroup cog, cityobject co "+ 
		                "WHERE co.gmlid = ? " + 
		                "AND cog.ID = co.ID " +
		                "AND g2co.cityobjectgroup_id = cog.ID) " +
   		"ORDER BY co.class_id";

	public static final String CITYOBJECTGROUP_MEMBERS_IN_BBOX = 
		"SELECT co.gmlid, co.class_id " + 
		"FROM cityobject co " +
		"WHERE co.ID IN (SELECT g2co.cityobject_id "+  
		                "FROM group_to_cityobject g2co, cityobjectgroup cog, cityobject co "+ 
		                "WHERE co.gmlid = ? " + 
		                "AND cog.ID = co.ID " +
		                "AND g2co.cityobjectgroup_id = cog.ID) " +
		"AND (SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2002, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?,?,?)), 'mask=overlapbdydisjoint') ='TRUE') " +
		"UNION ALL " +
		"SELECT co.gmlid, co.class_id " + 
		"FROM cityobject co " +
		"WHERE co.ID IN (SELECT g2co.cityobject_id "+  
		                "FROM group_to_cityobject g2co, cityobjectgroup cog, cityobject co "+ 
		                "WHERE co.gmlid = ? " + 
		                "AND cog.ID = co.ID " +
		                "AND g2co.cityobjectgroup_id = cog.ID) " +
		"AND (SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2003, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), 'mask=inside+coveredby') ='TRUE') " +
		"UNION ALL " +
		"SELECT co.gmlid, co.class_id " + 
		"FROM cityobject co " +
		"WHERE co.ID IN (SELECT g2co.cityobject_id "+  
		                "FROM group_to_cityobject g2co, cityobjectgroup cog, cityobject co "+ 
		                "WHERE co.gmlid = ? " + 
		                "AND cog.ID = co.ID " +
		                "AND g2co.cityobjectgroup_id = cog.ID) " +
		"AND (SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2003, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), 'mask=equal') ='TRUE') " +
		"ORDER BY 2"; // ORDER BY co.class_id
	
	// ----------------------------------------------------------------------
	// SOLITARY VEGETATION OBJECT QUERIES
	// ----------------------------------------------------------------------
	
	private static final String SOLITARY_VEGETATION_OBJECT_BASIS_DATA =
		"SELECT ig.relative_geometry_id, svo.lod<LoD>_implicit_ref_point, " +
			   "svo.lod<LoD>_implicit_transformation, svo.lod<LoD>_geometry_id " +
		"FROM CITYOBJECT co, SOLITARY_VEGETAT_OBJECT svo, IMPLICIT_GEOMETRY ig " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND svo.id = co.id " +
			"AND ig.id = svo.lod<LoD>_implicit_rep_id";

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
			"FROM CITYOBJECT co, SOLITARY_VEGETAT_OBJECT svo, IMPLICIT_GEOMETRY ig " + 
			"WHERE " +  
				"co.gmlid = ? " +
				"AND svo.id = co.id " +
				"AND ig.id = svo.lod<LoD>_implicit_rep_id " +
			"UNION " +
			"SELECT svo.lod<LoD>_geometry_id " +
			"FROM CITYOBJECT co, SOLITARY_VEGETAT_OBJECT svo " + 
			"WHERE " +  
				"co.gmlid = ? " +
				"AND svo.id = co.id) " +
		"AND sg.geometry IS NOT NULL";

    public static String getSolitaryVegetationObjectHighlightingQuery (int lodToExportFrom) {
    	return SOLITARY_VEGETATION_OBJECT_GEOMETRY_HIGHLIGHTING.replace("<LoD>", String.valueOf(lodToExportFrom));
    }
}
