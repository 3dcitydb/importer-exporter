package de.tub.citydb.db.kmlExporter;

import java.util.HashMap;

import de.tub.citydb.config.project.kmlExporter.DisplayLevel;
import de.tub.citydb.log.LogLevelType;
import de.tub.citydb.log.Logger;

public class TileQueries {

	private static final String QUERY_FOOTPRINT_LOD4_GET_BUILDING_DATA_ALT =
		"SELECT SDO_CS.TRANSFORM(SDO_AGGR_UNION(SDOAGGRTYPE(sg.geometry, 0.05)), 4326), " +
				"MAX(b.id) AS building_id " +
		"FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY sg " +
		"WHERE " +
			"co.gmlid = ? " +
		"AND b.id = co.id " +
		"AND sg.root_id = b.lod4_geometry_id " +
		"AND sg.geometry IS NOT NULL " +
		"GROUP BY b.id " +
		"ORDER BY b.id";

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

	private static final String QUERY_EXTRUDED_LOD4_GET_BUILDING_DATA_ALT =
		"SELECT SDO_CS.TRANSFORM(SDO_AGGR_UNION(SDOAGGRTYPE(sg.geometry, 0.05)), 4326), " +
				"MAX(b.id) AS building_id, MAX(SDO_GEOM.SDO_MAX_MBR_ORDINATE(co.envelope, 3)) AS measured_height " +
		"FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY sg " +
		"WHERE " +
			"co.gmlid = ? " +
		"AND b.id = co.id " +
		"AND sg.root_id = b.lod4_geometry_id " +
		"AND sg.geometry IS NOT NULL " +
		"GROUP BY b.id " +
		"ORDER BY b.id";

	private static final String QUERY_EXTRUDED_LOD4_GET_BUILDING_DATA =
		"SELECT SDO_CS.TRANSFORM(sg.geometry, 4326), b.measured_height " +
		"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts, CITYOBJECT co, BUILDING b " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND b.id = co.id " +
			"AND ts.building_id = co.id " +
			"AND ts.type = 'GroundSurface' " +
			"AND sg.root_id = ts.lod4_multi_surface_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY ts.building_id";

	private static final String QUERY_GEOMETRY_LOD4_GET_BUILDING_DATA_ALT =
		"SELECT sg.geometry, ts.type " +
		"FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY sg " +
			"LEFT JOIN THEMATIC_SURFACE ts ON ts.building_id = co.id " +
		"WHERE " +
			"co.gmlid = ? " +
		"AND b.id = co.id " +
		"AND sg.root_id = b.lod4_geometry_id " +
		"AND sg.geometry IS NOT NULL " +
		"ORDER BY b.id, ts.type";

	private static final String QUERY_GEOMETRY_LOD4_GET_BUILDING_DATA =
		"SELECT sg.geometry, ts.type " +
		"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND ts.building_id = co.id " +
			"AND sg.root_id = ts.lod4_multi_surface_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY ts.building_id, ts.type";

	private static final String QUERY_COLLADA_LOD4_GET_BUILDING_ROOT_SURFACES =
		"SELECT b.lod4_geometry_id " + 
		"FROM CITYOBJECT co, BUILDING b " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.id = co.id " +
			"AND b.lod4_geometry_id IS NOT NULL " +
		"UNION " + 
		"SELECT ts.lod4_multi_surface_id " + 
		"FROM CITYOBJECT co, THEMATIC_SURFACE ts " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND ts.building_id = co.id " +
			"AND ts.lod4_multi_surface_id IS NOT NULL";
	
	private static final String QUERY_FOOTPRINT_LOD3_GET_BUILDING_DATA_ALT =
		"SELECT SDO_CS.TRANSFORM(SDO_AGGR_UNION(SDOAGGRTYPE(sg.geometry, 0.05)), 4326), " +
				"MAX(b.id) AS building_id " +
		"FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY sg " +
		"WHERE " +
			"co.gmlid = ? " +
		"AND b.id = co.id " +
		"AND sg.root_id = b.lod3_geometry_id " +
		"AND sg.geometry IS NOT NULL " +
		"GROUP BY b.id " +
		"ORDER BY b.id";

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

	private static final String QUERY_EXTRUDED_LOD3_GET_BUILDING_DATA_ALT =
		"SELECT SDO_CS.TRANSFORM(SDO_AGGR_UNION(SDOAGGRTYPE(sg.geometry, 0.05)), 4326), " +
				"MAX(b.id) AS building_id, MAX(SDO_GEOM.SDO_MAX_MBR_ORDINATE(co.envelope, 3)) AS measured_height " +
		"FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY sg " +
		"WHERE " +
			"co.gmlid = ? " +
		"AND b.id = co.id " +
		"AND sg.root_id = b.lod3_geometry_id " +
		"AND sg.geometry IS NOT NULL " +
		"GROUP BY b.id " +
		"ORDER BY b.id";

	private static final String QUERY_EXTRUDED_LOD3_GET_BUILDING_DATA =
		"SELECT SDO_CS.TRANSFORM(sg.geometry, 4326), b.measured_height " +
		"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts, CITYOBJECT co, BUILDING b " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND b.id = co.id " +
			"AND ts.building_id = co.id " +
			"AND ts.type = 'GroundSurface' " +
			"AND sg.root_id = ts.lod3_multi_surface_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY ts.building_id";

	private static final String QUERY_GEOMETRY_LOD3_GET_BUILDING_DATA_ALT =
		"SELECT sg.geometry, ts.type " +
		"FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY sg " +
			"LEFT JOIN THEMATIC_SURFACE ts ON ts.building_id = co.id " +
		"WHERE " +
			"co.gmlid = ? " +
		"AND b.id = co.id " +
		"AND sg.root_id = b.lod3_geometry_id " +
		"AND sg.geometry IS NOT NULL " +
		"ORDER BY b.id, ts.type";

	private static final String QUERY_GEOMETRY_LOD3_GET_BUILDING_DATA =
		"SELECT sg.geometry, ts.type " +
		"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND ts.building_id = co.id " +
			"AND sg.root_id = ts.lod3_multi_surface_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY ts.building_id, ts.type";

	private static final String QUERY_COLLADA_LOD3_GET_BUILDING_ROOT_SURFACES =
		"SELECT b.lod3_geometry_id " + 
		"FROM CITYOBJECT co, BUILDING b " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.id = co.id " +
			"AND b.lod3_geometry_id IS NOT NULL " +
		"UNION " + 
		"SELECT ts.lod3_multi_surface_id " + 
		"FROM CITYOBJECT co, THEMATIC_SURFACE ts " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND ts.building_id = co.id " +
			"AND ts.lod3_multi_surface_id IS NOT NULL";

	private static final String QUERY_COLLADA_LOD2_GET_BUILDING_ROOT_SURFACES =
		"SELECT b.lod2_geometry_id " + 
		"FROM CITYOBJECT co, BUILDING b " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND b.id = co.id " +
			"AND b.lod2_geometry_id IS NOT NULL " +
		"UNION " + 
		"SELECT ts.lod2_multi_surface_id " + 
		"FROM CITYOBJECT co, THEMATIC_SURFACE ts " + 
		"WHERE " +  
			"co.gmlid = ? " +
			"AND ts.building_id = co.id " +
			"AND ts.lod2_multi_surface_id IS NOT NULL";

	public static final String QUERY_COLLADA_GET_BUILDING_DATA =
		// geometry will not be transformed: cartesian coordinates needed for collada
		"SELECT sg.geometry, sg.id, " +
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

	private static final String QUERY_GEOMETRY_LOD2_GET_BUILDING_DATA_ALT =
		"SELECT sg.geometry, ts.type " +
		"FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY sg " +
			"LEFT JOIN THEMATIC_SURFACE ts ON ts.building_id = co.id " +
		"WHERE " +
			"co.gmlid = ? " +
		"AND b.id = co.id " +
		"AND sg.root_id = b.lod2_geometry_id " +
		"AND sg.geometry IS NOT NULL " +
		"ORDER BY b.id, ts.type";

	private static final String QUERY_EXTRUDED_LOD2_GET_BUILDING_DATA_ALT =
		"SELECT SDO_CS.TRANSFORM(SDO_AGGR_UNION(SDOAGGRTYPE(sg.geometry, 0.05)), 4326), " +
				"MAX(b.id) AS building_id, MAX(SDO_GEOM.SDO_MAX_MBR_ORDINATE(co.envelope, 3)) AS measured_height " +
		"FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY sg " +
		"WHERE " +
			"co.gmlid = ? " +
		"AND b.id = co.id " +
		"AND sg.root_id = b.lod2_geometry_id " +
		"AND sg.geometry IS NOT NULL " +
		"GROUP BY b.id " +
		"ORDER BY b.id";

	private static final String QUERY_FOOTPRINT_LOD2_GET_BUILDING_DATA_ALT =
		"SELECT SDO_CS.TRANSFORM(SDO_AGGR_UNION(SDOAGGRTYPE(sg.geometry, 0.05)), 4326), " +
				"MAX(b.id) AS building_id " +
		"FROM CITYOBJECT co, BUILDING b, SURFACE_GEOMETRY sg " +
		"WHERE " +
			"co.gmlid = ? " +
		"AND b.id = co.id " +
		"AND sg.root_id = b.lod2_geometry_id " +
		"AND sg.geometry IS NOT NULL " +
		"GROUP BY b.id " +
		"ORDER BY b.id";
	
	private static final String QUERY_GEOMETRY_LOD2_GET_BUILDING_DATA =
		"SELECT sg.geometry, ts.type " +
		"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND ts.building_id = co.id " +
			"AND sg.root_id = ts.lod2_multi_surface_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY ts.building_id, ts.type";

	private static final String QUERY_EXTRUDED_LOD2_GET_BUILDING_DATA =
		"SELECT SDO_CS.TRANSFORM(sg.geometry, 4326), b.measured_height " +
		"FROM SURFACE_GEOMETRY sg, THEMATIC_SURFACE ts, CITYOBJECT co, BUILDING b " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND b.id = co.id " +
			"AND ts.building_id = co.id " +
			"AND ts.type = 'GroundSurface' " +
			"AND sg.root_id = ts.lod2_multi_surface_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY ts.building_id";

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
		"SELECT sg.geometry, ts.type " +
		"FROM SURFACE_GEOMETRY sg, BUILDING b, CITYOBJECT co " +
			"LEFT JOIN THEMATIC_SURFACE ts ON ts.building_id = co.id " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND b.id = co.id " +
			"AND sg.root_id = b.lod1_geometry_id " +
			"AND sg.geometry IS NOT NULL " +
		"ORDER BY b.id, ts.type";

	private static final String QUERY_EXTRUDED_LOD1_GET_BUILDING_DATA =
		"SELECT SDO_CS.TRANSFORM(SDO_AGGR_UNION(SDOAGGRTYPE(sg.geometry, 0.05)), 4326), " +
				"MAX(b.id) AS building_id, MAX(b.measured_height) AS measured_height " +
//		"SELECT sg.geometry, b.id AS building_id, " +
//				"SDO_GEOM.SDO_MAX_MBR_ORDINATE(co.envelope, 3) AS measured_height " +
		"FROM SURFACE_GEOMETRY sg, BUILDING b, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND b.id = co.id " +
			"AND sg.root_id = b.lod1_geometry_id " +
			"AND sg.geometry IS NOT NULL ";

	private static final String QUERY_FOOTPRINT_LOD1_GET_BUILDING_DATA =
		"SELECT SDO_CS.TRANSFORM(SDO_AGGR_UNION(SDOAGGRTYPE(sg.geometry, 0.05)), 4326), " +
				"MAX(b.id) AS building_id " +
		"FROM SURFACE_GEOMETRY sg, BUILDING b, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND b.id = co.id " +
			"AND sg.root_id = b.lod1_geometry_id " +
			"AND sg.geometry IS NOT NULL ";

	private static final String QUERY_GEOMETRY_FOR_HIGHLIGHTING_LOD1_GET_BUILDING_DATA =
		"SELECT sg.geometry " +
		"FROM SURFACE_GEOMETRY sg, BUILDING b, CITYOBJECT co " +
		"WHERE " +
			"co.gmlid = ? " +
			"AND b.id = co.id " +
			"AND sg.root_id = b.lod1_geometry_id " +
			"AND sg.geometry IS NOT NULL ";

	private static final String QUERY_GEOMETRY_FOR_HIGHLIGHTING_LOD2_GET_BUILDING_DATA =
		"SELECT sg.geometry " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE " +
			"sg.geometry IS NOT NULL " +
			"AND sg.root_id IN (" +
				"SELECT b.lod2_geometry_id " + 
				"FROM CITYOBJECT co, BUILDING b " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.id = co.id " +
					"AND b.lod2_geometry_id IS NOT NULL " +
				"UNION " + 
				"SELECT ts.lod2_multi_surface_id " + 
				"FROM CITYOBJECT co, THEMATIC_SURFACE ts " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND ts.building_id = co.id " +
					"AND ts.lod2_multi_surface_id IS NOT NULL)";

	private static final String QUERY_GEOMETRY_FOR_HIGHLIGHTING_LOD3_GET_BUILDING_DATA =
		"SELECT sg.geometry " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE " +
			"sg.geometry IS NOT NULL " +
			"AND sg.root_id IN (" +
				"SELECT b.lod3_geometry_id " + 
				"FROM CITYOBJECT co, BUILDING b " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.id = co.id " +
					"AND b.lod3_geometry_id IS NOT NULL " +
				"UNION " + 
				"SELECT ts.lod3_multi_surface_id " + 
				"FROM CITYOBJECT co, THEMATIC_SURFACE ts " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND ts.building_id = co.id " +
					"AND ts.lod3_multi_surface_id IS NOT NULL)";

	private static final String QUERY_GEOMETRY_FOR_HIGHLIGHTING_LOD4_GET_BUILDING_DATA =
		"SELECT sg.geometry " +
		"FROM SURFACE_GEOMETRY sg " +
		"WHERE " +
			"sg.geometry IS NOT NULL " +
			"AND sg.root_id IN (" +
				"SELECT b.lod4_geometry_id " + 
				"FROM CITYOBJECT co, BUILDING b " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND b.id = co.id " +
					"AND b.lod4_geometry_id IS NOT NULL " +
				"UNION " + 
				"SELECT ts.lod4_multi_surface_id " + 
				"FROM CITYOBJECT co, THEMATIC_SURFACE ts " + 
				"WHERE " +  
					"co.gmlid = ? " +
					"AND ts.building_id = co.id " +
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


    private static final HashMap<Integer, String> singleBuildingQueriesLod4Alt = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod4Alt.put(DisplayLevel.FOOTPRINT, QUERY_FOOTPRINT_LOD4_GET_BUILDING_DATA_ALT);
    	singleBuildingQueriesLod4Alt.put(DisplayLevel.EXTRUDED, QUERY_EXTRUDED_LOD4_GET_BUILDING_DATA_ALT);
    	singleBuildingQueriesLod4Alt.put(DisplayLevel.GEOMETRY, QUERY_GEOMETRY_LOD4_GET_BUILDING_DATA_ALT);
// dummy, currently there is no alternative query for collada
    	singleBuildingQueriesLod4Alt.put(DisplayLevel.COLLADA, QUERY_COLLADA_LOD4_GET_BUILDING_ROOT_SURFACES);
    }

    private static final HashMap<Integer, String> singleBuildingQueriesLod4 = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod4.put(DisplayLevel.FOOTPRINT, QUERY_FOOTPRINT_LOD4_GET_BUILDING_DATA);
    	singleBuildingQueriesLod4.put(DisplayLevel.EXTRUDED, QUERY_EXTRUDED_LOD4_GET_BUILDING_DATA);
    	singleBuildingQueriesLod4.put(DisplayLevel.GEOMETRY, QUERY_GEOMETRY_LOD4_GET_BUILDING_DATA);
    	singleBuildingQueriesLod4.put(DisplayLevel.COLLADA, QUERY_COLLADA_LOD4_GET_BUILDING_ROOT_SURFACES);
    }

    private static final HashMap<Integer, String> singleBuildingQueriesLod3Alt = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod3Alt.put(DisplayLevel.FOOTPRINT, QUERY_FOOTPRINT_LOD3_GET_BUILDING_DATA_ALT);
    	singleBuildingQueriesLod3Alt.put(DisplayLevel.EXTRUDED, QUERY_EXTRUDED_LOD3_GET_BUILDING_DATA_ALT);
    	singleBuildingQueriesLod3Alt.put(DisplayLevel.GEOMETRY, QUERY_GEOMETRY_LOD3_GET_BUILDING_DATA_ALT);
// dummy, currently there is no alternative query for collada
    	singleBuildingQueriesLod3Alt.put(DisplayLevel.COLLADA, QUERY_COLLADA_LOD3_GET_BUILDING_ROOT_SURFACES);
    }

    private static final HashMap<Integer, String> singleBuildingQueriesLod3 = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod3.put(DisplayLevel.FOOTPRINT, QUERY_FOOTPRINT_LOD3_GET_BUILDING_DATA);
    	singleBuildingQueriesLod3.put(DisplayLevel.EXTRUDED, QUERY_EXTRUDED_LOD3_GET_BUILDING_DATA);
    	singleBuildingQueriesLod3.put(DisplayLevel.GEOMETRY, QUERY_GEOMETRY_LOD3_GET_BUILDING_DATA);
    	singleBuildingQueriesLod3.put(DisplayLevel.COLLADA, QUERY_COLLADA_LOD3_GET_BUILDING_ROOT_SURFACES);
    }

    private static final HashMap<Integer, String> singleBuildingQueriesLod2Alt = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod2Alt.put(DisplayLevel.FOOTPRINT, QUERY_FOOTPRINT_LOD2_GET_BUILDING_DATA_ALT);
    	singleBuildingQueriesLod2Alt.put(DisplayLevel.EXTRUDED, QUERY_EXTRUDED_LOD2_GET_BUILDING_DATA_ALT);
    	singleBuildingQueriesLod2Alt.put(DisplayLevel.GEOMETRY, QUERY_GEOMETRY_LOD2_GET_BUILDING_DATA_ALT);
// dummy, currently there is no alternative query for collada
    	singleBuildingQueriesLod2Alt.put(DisplayLevel.COLLADA, QUERY_COLLADA_LOD2_GET_BUILDING_ROOT_SURFACES);
    }

    private static final HashMap<Integer, String> singleBuildingQueriesLod2 = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod2.put(DisplayLevel.FOOTPRINT, QUERY_FOOTPRINT_LOD2_GET_BUILDING_DATA);
    	singleBuildingQueriesLod2.put(DisplayLevel.EXTRUDED, QUERY_EXTRUDED_LOD2_GET_BUILDING_DATA);
    	singleBuildingQueriesLod2.put(DisplayLevel.GEOMETRY, QUERY_GEOMETRY_LOD2_GET_BUILDING_DATA);
    	singleBuildingQueriesLod2.put(DisplayLevel.COLLADA, QUERY_COLLADA_LOD2_GET_BUILDING_ROOT_SURFACES);
    }

	private static final HashMap<Integer, String> singleBuildingQueriesLod1 = new HashMap<Integer, String>();
    static {
    	singleBuildingQueriesLod1.put(DisplayLevel.FOOTPRINT, QUERY_FOOTPRINT_LOD1_GET_BUILDING_DATA);
    	singleBuildingQueriesLod1.put(DisplayLevel.EXTRUDED, QUERY_EXTRUDED_LOD1_GET_BUILDING_DATA);
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
    	    	Logger.getInstance().log(LogLevelType.INFO, "No single building query found for LoD" + lodToExportFrom);
    	}
    	
//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
    	return query;
    }

    public static String getSingleBuildingQueryAlt (int lodToExportFrom, DisplayLevel displayLevel) {
    	String query = null;
    	switch (lodToExportFrom) {
    		case 1:
// dummy, currently there is no alternative query for LoD1
    			query = singleBuildingQueriesLod1.get(displayLevel.getLevel());
    			break;
    		case 2:
    	    	query = singleBuildingQueriesLod2Alt.get(displayLevel.getLevel());
    	    	break;
    		case 3:
    	    	query = singleBuildingQueriesLod3Alt.get(displayLevel.getLevel());
    	    	break;
    		case 4:
    	    	query = singleBuildingQueriesLod4Alt.get(displayLevel.getLevel());
    	    	break;
    	    default:
    	    	Logger.getInstance().log(LogLevelType.INFO, "No alternative single building query found for LoD" + lodToExportFrom);
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
    	    	Logger.getInstance().log(LogLevelType.INFO, "No single building highlighting query found for LoD" + lodToExportFrom);
    	}
    	
//    	Logger.getInstance().log(LogLevelType.DEBUG, query);
    	return query;
    }

    public static final String QUERY_GET_GMLIDS =
		"SELECT co.gmlid " +
		"FROM CITYOBJECT co " +
		"WHERE " +
		  "(SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2003, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?)), 'mask=inside') ='TRUE' " +
		  "OR SDO_RELATE(co.envelope, MDSYS.SDO_GEOMETRY(2002, ?, null, MDSYS.SDO_ELEM_INFO_ARRAY(1,2,1), " +
					  "MDSYS.SDO_ORDINATE_ARRAY(?,?,?,?,?,?)), 'mask=overlapbdydisjoint') ='TRUE') " +
		"ORDER BY co.gmlid";

}
