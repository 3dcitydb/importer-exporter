/*
 * This file is part of the 3D City Database Importer/Exporter.
 * Copyright (c) 2007 - 2011
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
import de.tub.citydb.config.project.kmlExporter.DisplayLevel;
import de.tub.citydb.log.Logger;

public class TileQueries {

	private static final String QUERY_FOOTPRINT_LOD4_GET_BUILDING_DATA =
		"SELECT SDO_CS.TRANSFORM(sg.geometry, 4326) " +
		"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND ts.building_id = co.id " +
			"AND ts.type = 'GroundSurface' " +
			"AND sg.root_id = ts.lod4_multi_surface_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY ts.building_id";

	private static final String QUERY_GEOMETRY_LOD4_GET_BUILDING_DATA =
		"SELECT sg.geometry, ts.type, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"LEFT JOIN THEMATIC_SURFACE ts ON ts.lod4_multi_surface_id = sg.root_id " +
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
						"AND bi.lod4_geometry_id IS NOT NULL)";

	private static final String QUERY_COLLADA_LOD4_GET_BUILDING_ROOT_SURFACES =
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
			"AND bi.lod4_geometry_id IS NOT NULL";
			
	private static final String QUERY_FOOTPRINT_LOD3_GET_BUILDING_DATA =
		"SELECT SDO_CS.TRANSFORM(sg.geometry, 4326) " +
		"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND ts.building_id = co.id " +
			"AND ts.type = 'GroundSurface' " +
			"AND sg.root_id = ts.lod3_multi_surface_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY ts.building_id";

	private static final String QUERY_GEOMETRY_LOD3_GET_BUILDING_DATA =
		"SELECT sg.geometry, ts.type, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"LEFT JOIN THEMATIC_SURFACE ts ON ts.lod3_multi_surface_id = sg.root_id " +
		"WHERE " +
			"sg.geometry IS NOT NULL " +
			"AND sg.root_id IN (" +
				"SELECT b.lod3_geometry_id " + 
				"FROM CITYOBJECT co, BUILDING b " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.building_root_id = co.id " +
					"AND b.lod3_geometry_id IS NOT NULL " +
				"UNION " + 
				"SELECT ts.lod3_multi_surface_id " + 
				"FROM CITYOBJECT co, THEMATIC_SURFACE ts, BUILDING b " + 
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
					"AND bi.lod3_geometry_id IS NOT NULL)";

	private static final String QUERY_COLLADA_LOD3_GET_BUILDING_ROOT_SURFACES =
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
			"AND bi.lod3_geometry_id IS NOT NULL";

	private static final String QUERY_COLLADA_LOD2_GET_BUILDING_ROOT_SURFACES =
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


	public static final String QUERY_COLLADA_GET_BUILDING_DATA =
		// geometry will not be transformed: cartesian coordinates needed for collada
		"SELECT sg.geometry, sg.id, sg.parent_id, sd.type, " +
				"sd.x3d_shininess, sd.x3d_transparency, sd.x3d_ambient_intensity, sd.x3d_specular_color, sd.x3d_diffuse_color, sd.x3d_emissive_color, sd.x3d_is_smooth, " +
				"sd.tex_image_uri, sd.tex_image, tp.texture_coordinates, a.theme " +
		"FROM SURFACE_GEOMETRY sg " +
			"LEFT JOIN TEXTUREPARAM tp ON tp.surface_geometry_id = sg.id " + 
			"LEFT JOIN SURFACE_DATA sd ON sd.id = tp.surface_data_id " +
			"LEFT JOIN APPEAR_TO_SURFACE_DATA a2sd ON a2sd.surface_data_id = sd.id " +
			"LEFT JOIN APPEARANCE a ON a2sd.appearance_id = a.id " +
		"WHERE " +
			"sg.root_id = ? " +
//			"AND (a.theme = ? OR a.theme IS NULL) " +
		"ORDER BY sg.parent_id DESC"; // own root surfaces first

	private static final String QUERY_GEOMETRY_LOD2_GET_BUILDING_DATA =
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

	private static final String QUERY_FOOTPRINT_LOD2_GET_BUILDING_DATA =
		"SELECT SDO_CS.TRANSFORM(sg.geometry, 4326) " +
		"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND ts.building_id = co.id " +
			"AND ts.type = 'GroundSurface' " +
			"AND sg.root_id = ts.lod2_multi_surface_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY ts.building_id";

	private static final String QUERY_GEOMETRY_LOD1_GET_BUILDING_DATA =
		"SELECT sg.geometry, NULL as type, sg.id " +
		"FROM SURFACE_GEOMETRY sg, CITYOBJECT co, BUILDING b " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND sg.root_id = b.lod1_geometry_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY b.id";

	private static final String QUERY_FOOTPRINT_LOD1_GET_BUILDING_DATA =
		"SELECT SDO_CS.TRANSFORM(SDO_AGGR_UNION(SDOAGGRTYPE(sg.geometry, 0.05)), 4326), " +
				"MAX(b.building_root_id) AS building_id " +
		"FROM SURFACE_GEOMETRY sg, BUILDING b, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND sg.root_id = b.lod1_geometry_id " +
			"AND sg.geometry IS NOT NULL";

	private static final String QUERY_GEOMETRY_FOR_HIGHLIGHTING_LOD1_GET_BUILDING_DATA =
		"SELECT sg.geometry, sg.id " +
		"FROM SURFACE_GEOMETRY sg, BUILDING b, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND b.building_root_id = co.id " +
			"AND sg.root_id = b.lod1_geometry_id " +
			"AND sg.geometry IS NOT NULL ";

	private static final String QUERY_GEOMETRY_FOR_HIGHLIGHTING_LOD2_GET_BUILDING_DATA =
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

	private static final String QUERY_GEOMETRY_FOR_HIGHLIGHTING_LOD3_GET_BUILDING_DATA =
		"SELECT sg.geometry, sg.id " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE " +
			"sg.geometry IS NOT NULL " +
			"AND sg.root_id IN (" +
				"SELECT b.lod3_geometry_id " + 
				"FROM CITYOBJECT co, BUILDING b " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.building_root_id = co.id " +
					"AND b.lod3_geometry_id IS NOT NULL " +
				"UNION " + 
				"SELECT ts.lod3_multi_surface_id " + 
				"FROM CITYOBJECT co, THEMATIC_SURFACE ts, BUILDING b " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.building_root_id = co.id " +
					"AND ts.building_id = b.id " +
					"AND ts.lod3_multi_surface_id IS NOT NULL)";
	
	
	private static final String QUERY_GEOMETRY_FOR_HIGHLIGHTING_LOD4_GET_BUILDING_DATA =
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
					"AND ts.lod4_multi_surface_id IS NOT NULL)";

	public static final String QUERY_GET_STRVAL_GENERICATTRIB_FROM_GML_ID =
		"SELECT coga.strval " +
		"FROM CITYOBJECT co " + 
			"LEFT JOIN CITYOBJECT_GENERICATTRIB coga ON (coga.cityobject_id = co.id AND coga.attrname = ?) " +
		"WHERE co.gmlid = ?";

	public static final String QUERY_INSERT_GE_ZOFFSET =
		"INSERT INTO CITYOBJECT_GENERICATTRIB (ID, ATTRNAME, DATATYPE, STRVAL, CITYOBJECT_ID) " +
		"VALUES (CITYOBJECT_GENERICATT_SEQ.NEXTVAL, ?, 1, ?, (SELECT ID FROM CITYOBJECT WHERE gmlid = ?))";
	
	public static final String TRANSFORM_GEOMETRY_TO_WGS84 =
		"SELECT SDO_CS.TRANSFORM(?, 4326) FROM DUAL";

	public static final String QUERY_GET_ENVELOPE_IN_WGS84_FROM_GML_ID =
		"SELECT SDO_CS.TRANSFORM(co.envelope, 4326) " +
		"FROM CITYOBJECT co " +
		"WHERE co.gmlid = ?";


	private static final HashMap<Integer, String> singleBuildingQueriesLod4 = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod4.put(DisplayLevel.FOOTPRINT, QUERY_FOOTPRINT_LOD4_GET_BUILDING_DATA);
    	singleBuildingQueriesLod4.put(DisplayLevel.EXTRUDED, QUERY_FOOTPRINT_LOD4_GET_BUILDING_DATA);
    	singleBuildingQueriesLod4.put(DisplayLevel.GEOMETRY, QUERY_GEOMETRY_LOD4_GET_BUILDING_DATA);
    	singleBuildingQueriesLod4.put(DisplayLevel.COLLADA, QUERY_COLLADA_LOD4_GET_BUILDING_ROOT_SURFACES);
    }

    private static final HashMap<Integer, String> singleBuildingQueriesLod3 = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod3.put(DisplayLevel.FOOTPRINT, QUERY_FOOTPRINT_LOD3_GET_BUILDING_DATA);
    	singleBuildingQueriesLod3.put(DisplayLevel.EXTRUDED, QUERY_FOOTPRINT_LOD3_GET_BUILDING_DATA);
    	singleBuildingQueriesLod3.put(DisplayLevel.GEOMETRY, QUERY_GEOMETRY_LOD3_GET_BUILDING_DATA);
    	singleBuildingQueriesLod3.put(DisplayLevel.COLLADA, QUERY_COLLADA_LOD3_GET_BUILDING_ROOT_SURFACES);
    }

    private static final HashMap<Integer, String> singleBuildingQueriesLod2 = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod2.put(DisplayLevel.FOOTPRINT, QUERY_FOOTPRINT_LOD2_GET_BUILDING_DATA);
    	singleBuildingQueriesLod2.put(DisplayLevel.EXTRUDED, QUERY_FOOTPRINT_LOD2_GET_BUILDING_DATA);
    	singleBuildingQueriesLod2.put(DisplayLevel.GEOMETRY, QUERY_GEOMETRY_LOD2_GET_BUILDING_DATA);
    	singleBuildingQueriesLod2.put(DisplayLevel.COLLADA, QUERY_COLLADA_LOD2_GET_BUILDING_ROOT_SURFACES);
    }

	private static final HashMap<Integer, String> singleBuildingQueriesLod1 = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod1.put(DisplayLevel.FOOTPRINT, QUERY_FOOTPRINT_LOD1_GET_BUILDING_DATA);
    	singleBuildingQueriesLod1.put(DisplayLevel.EXTRUDED, QUERY_FOOTPRINT_LOD1_GET_BUILDING_DATA);
    	singleBuildingQueriesLod1.put(DisplayLevel.GEOMETRY, QUERY_GEOMETRY_LOD1_GET_BUILDING_DATA);
    }

    public static String getSingleBuildingQuery (int lodToExportFrom, DisplayLevel displayLevel) {
    	String query = null;
    	switch (lodToExportFrom) {
    		case 1:
    	    	query = singleBuildingQueriesLod1.get(displayLevel.getLevel());
    	    	break;
    		case 2:
    	    	query = singleBuildingQueriesLod2.get(displayLevel.getLevel());
    	    	break;
    		case 3:
    	    	query = singleBuildingQueriesLod3.get(displayLevel.getLevel());
    	    	break;
    		case 4:
    	    	query = singleBuildingQueriesLod4.get(displayLevel.getLevel());
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
    			query = QUERY_GEOMETRY_FOR_HIGHLIGHTING_LOD1_GET_BUILDING_DATA;
    	    	break;
    		case 2:
    			query = QUERY_GEOMETRY_FOR_HIGHLIGHTING_LOD2_GET_BUILDING_DATA;
    			break;
    		case 3:
    			query = QUERY_GEOMETRY_FOR_HIGHLIGHTING_LOD3_GET_BUILDING_DATA;
    	    	break;
    		case 4:
    			query = QUERY_GEOMETRY_FOR_HIGHLIGHTING_LOD4_GET_BUILDING_DATA;
    	    	break;
    	    default:
    	    	Logger.getInstance().log(LogLevel.INFO, "No single building highlighting query found for LoD" + lodToExportFrom);
    	}
    	
//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
    	return query;
    }

    public static final String QUERY_GET_GMLIDS =
		"SELECT co.gmlid, co.class_id " +
		"FROM CITYOBJECT co " +
		"WHERE " +
		  "(SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2003, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), 'mask=inside+coveredby+equal') ='TRUE' " +
		  "OR SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2002, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?,?,?)), 'mask=overlapbdydisjoint') ='TRUE') " +
		"ORDER BY co.gmlid";
/*
    public static final String QUERY_GET_GEOMETRIES_FOR_LOD =
		
//		"SELECT SDO_CS.TRANSFORM(geodb_match.to_2d(sg.geometry, (select srid from database_srs)) , 4326) " +
		"SELECT SDO_CS.TRANSFORM(sg.geometry, 4326) " +
		"FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts " +
		"WHERE " +
		  "co.gmlid = ? " +
		"AND b.building_root_id = co.ID " +
		"AND ((ts.building_id = b.ID " +
		      "AND ts.lod<LoD>_multi_surface_id IS NOT NULL " +
		      "AND sg.root_id = ts.lod<LoD>_multi_surface_id) " +
		  "OR (b.lod<LoD>_geometry_id IS NOT NULL " +
		      "AND sg.root_id = b.lod<LoD>_geometry_id)) " +
		"AND sg.geometry IS NOT NULL";
*/
    public static final String QUERY_GET_AGGREGATE_GEOMETRIES_FOR_LOD =
		
		"SELECT SDO_CS.TRANSFORM(sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom,0.05)), 4326) aggr_geom " +
		"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom,0.05)) aggr_geom " +
		"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom,0.05)) aggr_geom " +
		"FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom,0.05)) aggr_geom " +
		"FROM (" +

//		"SELECT geodb_match.to_2d(sg.geometry, (select srid from database_srs)) AS simple_geom " +
		"SELECT sg.geometry AS simple_geom " +
		"FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts " +
		"WHERE " +
		  "co.gmlid = ? " +
		"AND b.building_root_id = co.ID " +
		"AND ((ts.building_id = b.ID " +
		      "AND ts.lod<LoD>_multi_surface_id IS NOT NULL " +
		      "AND sg.root_id = ts.lod<LoD>_multi_surface_id) " +
		  "OR (b.lod<LoD>_geometry_id IS NOT NULL " +
		      "AND sg.root_id = b.lod<LoD>_geometry_id)) " +
		"AND sg.geometry IS NOT NULL" +
		
		") " +
		"GROUP BY mod(rownum, <GROUP_BY_1>) " +
		") " +
		"GROUP BY mod (rownum, <GROUP_BY_2>) " +
		") " +
		"GROUP BY mod (rownum, <GROUP_BY_3>) " +
		")";

	public static final String QUERY_EXTRUDED_HEIGHTS =
		"SELECT " + // "b.measured_height, " +
		"SDO_GEOM.SDO_MAX_MBR_ORDINATE(co.envelope, 3) - SDO_GEOM.SDO_MIN_MBR_ORDINATE(co.envelope, 3) AS envelope_measured_height " +
		"FROM CITYOBJECT co " + // ", BUILDING b " +
		"WHERE " +
			"co.gmlid = ?"; // + " AND b.building_root_id = co.id";

}
