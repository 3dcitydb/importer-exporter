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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.citygml4j.model.citygml.CityGMLClass;

import oracle.jdbc.OracleResultSet;
import oracle.spatial.geometry.JGeometry;
import oracle.sql.STRUCT;

import de.tub.citydb.api.database.BalloonTemplateHandler;
import de.tub.citydb.database.DatabaseConnectionPool;
import de.tub.citydb.log.Logger;
import de.tub.citydb.util.Util;

@SuppressWarnings("serial")
public class BalloonTemplateHandlerImpl implements BalloonTemplateHandler {

	private static final String ADDRESS_TABLE = "ADDRESS";
	private static final LinkedHashSet<String> ADDRESS_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("STREET");
		add("HOUSE_NUMBER");
		add("PO_BOX");
		add("ZIP_CODE");
		add("CITY");
		add("STATE");
		add("COUNTRY");
		add("MULTI_POINT");
		add("XAL_SOURCE");
	}};

	private static final String ADDRESS_TO_BUILDING_TABLE = "ADDRESS_TO_BUILDING";
	private static final LinkedHashSet<String> ADDRESS_TO_BUILDING_COLUMNS = new LinkedHashSet<String>() {{
		add("BUILDING_ID");
		add("ADDRESS_ID");
	}};

	private static final String APPEAR_TO_SURFACE_DATA_TABLE = "APPEAR_TO_SURFACE_DATA";
	private static final LinkedHashSet<String> APPEAR_TO_SURFACE_DATA_COLUMNS = new LinkedHashSet<String>() {{
		add("SURFACE_DATA_ID");
		add("APPEARANCE_ID");
	}};

	private static final String APPEARANCE_TABLE = "APPEARANCE";
	private static final LinkedHashSet<String> APPEARANCE_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("GMLID");
		add("GMLID_CODESPACE");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("THEME");
		add("CITYMODEL_ID");
		add("CITYOBJECT_ID");
	}};
/*
	private static final String BREAKLINE_RELIEF_TABLE = "BREAKLINE_RELIEF";
	private static final LinkedHashSet<String> BREAKLINE_RELIEF_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("RIDGE_OR_VALLEY_LINES");
		add("BREAK_LINES");
	}};
*/
	private static final String BUILDING_TABLE = "BUILDING";
	private static final LinkedHashSet<String> BUILDING_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("BUILDING_PARENT_ID");
		add("BUILDING_ROOT_ID");
		add("DESCRIPTION");
		add("CLASS");
		add("FUNCTION");
		add("USAGE");
		add("YEAR_OF_CONSTRUCTION");
		add("YEAR_OF_DEMOLITION");
		add("ROOF_TYPE");
		add("MEASURED_HEIGHT");
		add("STOREYS_ABOVE_GROUND");
		add("STOREYS_BELOW_GROUND");
		add("STOREY_HEIGHTS_ABOVE_GROUND");
		add("STOREY_HEIGHTS_BELOW_GROUND");
		add("LOD1_TERRAIN_INTERSECTION");
		add("LOD2_TERRAIN_INTERSECTION");
		add("LOD3_TERRAIN_INTERSECTION");
		add("LOD4_TERRAIN_INTERSECTION");
		add("LOD2_MULTI_CURVE");
		add("LOD3_MULTI_CURVE");
		add("LOD4_MULTI_CURVE");
		add("LOD1_GEOMETRY_ID");
		add("LOD2_GEOMETRY_ID");
		add("LOD3_GEOMETRY_ID");
		add("LOD4_GEOMETRY_ID");
	}};

	private static final String BUILDING_INSTALLATION_TABLE = "BUILDING_INSTALLATION";
	private static final LinkedHashSet<String> BUILDING_INSTALLATION_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("IS_EXTERNAL");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("CLASS");
		add("FUNCTION");
		add("USAGE");
		add("BUILDING_ID");
		add("ROOM_ID");
		add("LOD2_GEOMETRY_ID");
		add("LOD3_GEOMETRY_ID");
		add("LOD4_GEOMETRY_ID");
	}};

	private static final String CITY_FURNITURE_TABLE = "CITY_FURNITURE";
	private static final LinkedHashSet<String> CITY_FURNITURE_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("CLASS");
		add("FUNCTION");
		add("LOD1_TERRAIN_INTERSECTION");
		add("LOD2_TERRAIN_INTERSECTION");
		add("LOD3_TERRAIN_INTERSECTION");
		add("LOD4_TERRAIN_INTERSECTION");
		add("LOD1_GEOMETRY_ID");
		add("LOD2_GEOMETRY_ID");
		add("LOD3_GEOMETRY_ID");
		add("LOD4_GEOMETRY_ID");
		add("LOD1_IMPLICIT_REP_ID");
		add("LOD2_IMPLICIT_REP_ID");
		add("LOD3_IMPLICIT_REP_ID");
		add("LOD4_IMPLICIT_REP_ID");
		add("LOD1_IMPLICIT_REF_POINT");
		add("LOD2_IMPLICIT_REF_POINT");
		add("LOD3_IMPLICIT_REF_POINT");
		add("LOD4_IMPLICIT_REF_POINT");
		add("LOD1_IMPLICIT_TRANSFORMATION");
		add("LOD2_IMPLICIT_TRANSFORMATION");
		add("LOD3_IMPLICIT_TRANSFORMATION");
		add("LOD4_IMPLICIT_TRANSFORMATION");
	}};

	private static final String CITYMODEL_TABLE = "CITYMODEL";
	private static final LinkedHashSet<String> CITYMODEL_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("GMLID");
		add("GMLID_CODESPACE");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("ENVELOPE");
		add("CREATION_DATE");
		add("TERMINATION_DATE");
		add("LAST_MODIFICATION_DATE");
		add("UPDATING_PERSON");
		add("REASON_FOR_UPDATE");
		add("LINEAGE");
	}};

	private static final String CITYOBJECT_TABLE = "CITYOBJECT";
	private static final LinkedHashSet<String> CITYOBJECT_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("CLASS_ID");
		add("GMLID");
		add("GMLID_CODESPACE");
		add("ENVELOPE");
		add("CREATION_DATE");
		add("TERMINATION_DATE");
		add("LAST_MODIFICATION_DATE");
		add("UPDATING_PERSON");
		add("REASON_FOR_UPDATE");
		add("LINEAGE");
		add("XML_SOURCE");
	}};

	private static final String CITYOBJECT_GENERICATTRIB_TABLE = "CITYOBJECT_GENERICATTRIB";
	private static final LinkedHashSet<String> CITYOBJECT_GENERICATTRIB_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("ATTRNAME");
		add("DATATYPE");
		add("STRVAL");
		add("INTVAL");
		add("REALVAL");
		add("URIVAL");
		add("DATEVAL");
		add("GEOMVAL");
		add("BLOBVAL");
		add("CITYOBJECT_ID");
		add("SURFACE_GEOMETRY_ID");
	}};

	private static final String CITYOBJECTGROUP_TABLE = "CITYOBJECTGROUP";
	private static final LinkedHashSet<String> CITYOBJECTGROUP_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("CLASS");
		add("FUNCTION");
		add("USAGE");
		add("GEOMETRY");
		add("SURFACE_GEOMETRY_ID");
		add("PARENT_CITYOBJECT_ID");
	}};

	private static final String CITYOBJECT_MEMBER_TABLE = "CITYOBJECT_MEMBER";
	private static final LinkedHashSet<String> CITYOBJECT_MEMBER_COLUMNS = new LinkedHashSet<String>() {{
		add("CITYMODEL_ID");
		add("CITYOBJECT_ID");
	}};

	private static final String COLLECT_GEOM_TABLE = "COLLECT_GEOM";
	private static final LinkedHashSet<String> COLLECT_GEOM_COLUMNS = new LinkedHashSet<String>() {{
		add("BUILDING_ID");
		add("GEOMETRY_ID");
		add("CITYOBJECT_ID");
	}};

	private static final String DATABASE_SRS_TABLE = "DATABASE_SRS";
	private static final LinkedHashSet<String> DATABASE_SRS_COLUMNS = new LinkedHashSet<String>() {{
		add("SRID");
		add("GML_SRS_NAME");
	}};

	private static final String EXTERNAL_REFERENCE_TABLE = "EXTERNAL_REFERENCE";
	private static final LinkedHashSet<String> EXTERNAL_REFERENCE_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("INFOSYS");
		add("NAME");
		add("URI");
		add("CITYOBJECT_ID");
	}};

	private static final String GENERALIZATION_TABLE = "GENERALIZATION";
	private static final LinkedHashSet<String> GENERALIZATION_COLUMNS = new LinkedHashSet<String>() {{
		add("CITYOBJECT_ID");
		add("GENERALIZES_TO_ID");
	}};

	private static final String GENERIC_CITYOBJECT_TABLE = "GENERIC_CITYOBJECT";
	private static final LinkedHashSet<String> GENERIC_CITYOBJECT_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("CLASS");
		add("FUNCTION");
		add("USAGE");
		add("LOD1_TERRAIN_INTERSECTION");
		add("LOD2_TERRAIN_INTERSECTION");
		add("LOD3_TERRAIN_INTERSECTION");
		add("LOD4_TERRAIN_INTERSECTION");
		add("LOD1_GEOMETRY_ID");
		add("LOD2_GEOMETRY_ID");
		add("LOD3_GEOMETRY_ID");
		add("LOD4_GEOMETRY_ID");
		add("LOD1_IMPLICIT_REP_ID");
		add("LOD2_IMPLICIT_REP_ID");
		add("LOD3_IMPLICIT_REP_ID");
		add("LOD4_IMPLICIT_REP_ID");
		add("LOD1_IMPLICIT_REF_POINT");
		add("LOD2_IMPLICIT_REF_POINT");
		add("LOD3_IMPLICIT_REF_POINT");
		add("LOD4_IMPLICIT_REF_POINT");
		add("LOD1_IMPLICIT_TRANSFORMATION");
		add("LOD2_IMPLICIT_TRANSFORMATION");
		add("LOD3_IMPLICIT_TRANSFORMATION");
		add("LOD4_IMPLICIT_TRANSFORMATION");
	}};

	private static final String GROUP_TO_CITYOBJECT_TABLE = "GROUP_TO_CITYOBJECT";
	private static final LinkedHashSet<String> GROUP_TO_CITYOBJECT_COLUMNS = new LinkedHashSet<String>() {{
		add("CITYOBJECT_ID");
		add("CITYOBJECTGROUP_ID");
		add("ROLE");
	}};

	private static final String LAND_USE_TABLE = "LAND_USE";
	private static final LinkedHashSet<String> LAND_USE_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("CLASS");
		add("FUNCTION");
		add("USAGE");
		add("LOD0_MULTI_SURFACE_ID");
		add("LOD1_MULTI_SURFACE_ID");
		add("LOD2_MULTI_SURFACE_ID");
		add("LOD3_MULTI_SURFACE_ID");
		add("LOD4_MULTI_SURFACE_ID");
	}};
/*
	private static final String MASSPOINT_RELIEF_TABLE = "MASSPOINT_RELIEF";
	private static final LinkedHashSet<String> MASSPOINT_RELIEF_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("RELIEF_POINTS");
	}};
*/
	private static final String OBJECTCLASS_TABLE = "OBJECTCLASS";
	private static final LinkedHashSet<String> OBJECTCLASS_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("CLASSNAME");
		add("SUPERCLASS_ID");
	}};

	private static final String OPENING_TABLE = "OPENING";
	private static final LinkedHashSet<String> OPENING_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("TYPE");
		add("ADDRESS_ID");
		add("LOD3_MULTI_SURFACE_ID");
		add("LOD4_MULTI_SURFACE_ID");
	}};

	private static final String OPENING_TO_THEM_SURFACE_TABLE = "OPENING_TO_THEM_SURFACE";
	private static final LinkedHashSet<String> OPENING_TO_THEM_SURFACE_COLUMNS = new LinkedHashSet<String>() {{
		add("OPENING_ID");
		add("THEMATIC_SURFACE_ID");
	}};

	private static final String PLANT_COVER_TABLE = "PLANT_COVER";
	private static final LinkedHashSet<String> PLANT_COVER_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("CLASS");
		add("FUNCTION");
		add("AVERAGE_HEIGHT");
		add("LOD1_GEOMETRY_ID");
		add("LOD2_GEOMETRY_ID");
		add("LOD3_GEOMETRY_ID");
		add("LOD4_GEOMETRY_ID");
	}};
/*
	private static final String RASTER_RELIEF_TABLE = "RASTER_RELIEF";
	private static final LinkedHashSet<String> RASTER_RELIEF_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("RASTERPROPERTY");
	}};
*/
	private static final String RASTER_RELIEF_IMP_TABLE = "RASTER_RELIEF_IMP";
	private static final LinkedHashSet<String> RASTER_RELIEF_IMP_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("RASTERPROPERTY");
		add("RELIEF_ID");
		add("RASTER_RELIEF_ID");
		add("FILENAME");
		add("FOOTPRINT");
	}};

	private static final String RELIEF_COMPONENT_TABLE = "RELIEF_COMPONENT";
	private static final LinkedHashSet<String> RELIEF_COMPONENT_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("LOD");
		add("EXTENT");
	}};
/*
	private static final String RELIEF_FEAT_TO_REL_COMP_TABLE = "RELIEF_FEAT_TO_REL_COMP";
	private static final LinkedHashSet<String> RELIEF_FEAT_TO_REL_COMP_COLUMNS = new LinkedHashSet<String>() {{
		add("RELIEF_COMPONENT_ID");
		add("RELIEF_FEATURE_ID");
	}};
*/
	private static final String RELIEF_FEATURE_TABLE = "RELIEF_FEATURE";
	private static final LinkedHashSet<String> RELIEF_FEATURE_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("LOD");
	}};

	private static final String ROOM_TABLE = "ROOM";
	private static final LinkedHashSet<String> ROOM_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("CLASS");
		add("FUNCTION");
		add("USAGE");
		add("BUILDING_ID");
		add("LOD4_GEOMETRY_ID");
	}};

	private static final String SOLITARY_VEGETAT_OBJECT_TABLE = "SOLITARY_VEGETAT_OBJECT";
	private static final LinkedHashSet<String> SOLITARY_VEGETAT_OBJECT_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("CLASS");
		add("SPECIES");
		add("FUNCTION");
		add("HEIGHT");
		add("TRUNC_DIAMETER");
		add("CROWN_DIAMETER");
		add("LOD1_GEOMETRY_ID");
		add("LOD2_GEOMETRY_ID");
		add("LOD3_GEOMETRY_ID");
		add("LOD4_GEOMETRY_ID");
		add("LOD1_IMPLICIT_REP_ID");
		add("LOD2_IMPLICIT_REP_ID");
		add("LOD3_IMPLICIT_REP_ID");
		add("LOD4_IMPLICIT_REP_ID");
		add("LOD1_IMPLICIT_REF_POINT");
		add("LOD2_IMPLICIT_REF_POINT");
		add("LOD3_IMPLICIT_REF_POINT");
		add("LOD4_IMPLICIT_REF_POINT");
		add("LOD1_IMPLICIT_TRANSFORMATION");
		add("LOD2_IMPLICIT_TRANSFORMATION");
		add("LOD3_IMPLICIT_TRANSFORMATION");
		add("LOD4_IMPLICIT_TRANSFORMATION");
	}};

	private static final String SURFACE_DATA_TABLE = "SURFACE_DATA";
	private static final LinkedHashSet<String> SURFACE_DATA_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("GMLID");
		add("GMLID_CODESPACE");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("IS_FRONT");
		add("TYPE");
		add("X3D_SHININESS");
		add("X3D_TRANSPARENCY");
		add("X3D_AMBIENT_INTENSITY");
		add("X3D_SPECULAR_COLOR");
		add("X3D_DIFFUSE_COLOR");
		add("X3D_EMISSIVE_COLOR");
		add("X3D_IS_SMOOTH");
		add("TEX_IMAGE_URI");
		add("TEX_IMAGE");
		add("TEX_MIME_TYPE");
		add("TEX_TEXTURE_TYPE");
		add("TEX_WRAP_MODE");
		add("TEX_BORDER_COLOR");
		add("GT_PREFER_WORLDFILE");
		add("GT_ORIENTATION");
		add("GT_REFERENCE_POINT");
	}};

	private static final String SURFACE_GEOMETRY_TABLE = "SURFACE_GEOMETRY";
	private static final LinkedHashSet<String> SURFACE_GEOMETRY_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("GMLID");
		add("GMLID_CODESPACE");
		add("PARENT_ID");
		add("ROOT_ID");
		add("IS_SOLID");
		add("IS_COMPOSITE");
		add("IS_TRIANGULATED");
		add("IS_XLINK");
		add("IS_REVERSE");
		add("GEOMETRY");
	}};

	private static final String TEXTUREPARAM_TABLE = "TEXTUREPARAM";
	private static final LinkedHashSet<String> TEXTUREPARAM_COLUMNS = new LinkedHashSet<String>() {{
		add("SURFACE_GEOMETRY_ID");
		add("IS_TEXTURE_PARAMETRIZATION");
		add("WORLD_TO_TEXTURE");
		add("TEXTURE_COORDINATES");
		add("SURFACE_DATA_ID");
	}};

	private static final String THEMATIC_SURFACE_TABLE = "THEMATIC_SURFACE";
	private static final LinkedHashSet<String> THEMATIC_SURFACE_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("TYPE");
		add("BUILDING_ID");
		add("ROOM_ID");
		add("LOD2_MULTI_SURFACE_ID");
		add("LOD3_MULTI_SURFACE_ID");
		add("LOD4_MULTI_SURFACE_ID");
	}};
/*
	private static final String TIN_RELIEF_TABLE = "TIN_RELIEF";
	private static final LinkedHashSet<String> TIN_RELIEF_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("MAX_LENGTH");
		add("STOP_LINES");
		add("BREAK_LINES");
		add("CONTROL_POINTS");
		add("SURFACE_GEOMETRY_ID");
	}};
*/
	private static final String TRAFFIC_AREA_TABLE = "TRAFFIC_AREA";
	private static final LinkedHashSet<String> TRAFFIC_AREA_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("IS_AUXILIARY");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("FUNCTION");
		add("USAGE");
		add("SURFACE_MATERIAL");
		add("LOD2_MULTI_SURFACE_ID");
		add("LOD3_MULTI_SURFACE_ID");
		add("LOD4_MULTI_SURFACE_ID");
		add("TRANSPORTATION_COMPLEX_ID");
	}};

	private static final String TRANSPORTATION_COMPLEX_TABLE = "TRANSPORTATION_COMPLEX";
	private static final LinkedHashSet<String> TRANSPORTATION_COMPLEX_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("FUNCTION");
		add("USAGE");
		add("TYPE");
		add("LOD0_NETWORK");
		add("LOD1_MULTI_SURFACE_ID");
		add("LOD2_MULTI_SURFACE_ID");
		add("LOD3_MULTI_SURFACE_ID");
		add("LOD4_MULTI_SURFACE_ID");
	}};

	private static final String WATERBODY_TO_WATERBOUNDARY_SURFACE_TABLE = "WATERBOD_TO_WATERBND_SRF";
	private static final LinkedHashSet<String> WATERBODY_TO_WATERBOUNDARY_SURFACE_COLUMNS = new LinkedHashSet<String>() {{
		add("WATERBOUNDARY_SURFACE_ID");
		add("WATERBODY_ID");
	}};

	private static final String WATERBODY_TABLE = "WATERBODY";
	private static final LinkedHashSet<String> WATERBODY_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("CLASS");
		add("FUNCTION");
		add("USAGE");
		add("LOD0_MULTI_CURVE");
		add("LOD1_MULTI_CURVE");
		add("LOD1_SOLID_ID");
		add("LOD2_SOLID_ID");
		add("LOD3_SOLID_ID");
		add("LOD4_SOLID_ID");
		add("LOD0_MULTI_SURFACE_ID");
		add("LOD1_MULTI_SURFACE_ID");
	}};

	private static final String WATERBOUNDARY_SURFACE_TABLE = "WATERBOUNDARY_SURFACE";
	private static final LinkedHashSet<String> WATERBOUNDARY_SURFACE_COLUMNS = new LinkedHashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("TYPE");
		add("WATER_LEVEL");
		add("LOD2_SURFACE_ID");
		add("LOD3_SURFACE_ID");
		add("LOD4_SURFACE_ID");
	}};

	private static final String MAX = "MAX";
	private static final String MIN = "MIN";
	private static final String AVG = "AVG";
	private static final String COUNT = "COUNT";
	private static final String SUM = "SUM";
	private static final String FIRST = "FIRST";
	private static final String LAST = "LAST";

	private static final LinkedHashSet<String> AGGREGATION_FUNCTIONS = new LinkedHashSet<String>() {{
		add(MAX);
		add(MIN);
		add(AVG);
		add(COUNT);
		add(SUM);
		add(FIRST);
		add(LAST);
	}};

	public Set<String> getSupportedAggregationFunctions() {
		return AGGREGATION_FUNCTIONS;
	}

	private static final String SPECIAL_KEYWORDS = "SPECIAL_KEYWORDS";
	private static final String CENTROID_WGS84 = "CENTROID_WGS84";
	private static final String CENTROID_WGS84_LAT = "CENTROID_WGS84_LAT";
	private static final String CENTROID_WGS84_LON = "CENTROID_WGS84_LON";
	private static final String BBOX_WGS84_LAT_MIN = "BBOX_WGS84_LAT_MIN";
	private static final String BBOX_WGS84_LAT_MAX = "BBOX_WGS84_LAT_MAX";
	private static final String BBOX_WGS84_LON_MIN = "BBOX_WGS84_LON_MIN";
	private static final String BBOX_WGS84_LON_MAX = "BBOX_WGS84_LON_MAX";
	private static final String BBOX_WGS84_HEIGHT_MIN = "BBOX_WGS84_HEIGHT_MIN";
	private static final String BBOX_WGS84_HEIGHT_MAX = "BBOX_WGS84_HEIGHT_MAX";
	private static final String BBOX_WGS84_LAT_LON = "BBOX_WGS84_LAT_LON";
	private static final String BBOX_WGS84_LON_LAT = "BBOX_WGS84_LON_LAT";

	private static final LinkedHashSet<String> SPECIAL_KEYWORDS_SET = new LinkedHashSet<String>() {{
		add(CENTROID_WGS84);
		add(CENTROID_WGS84_LAT);
		add(CENTROID_WGS84_LON);
		add(BBOX_WGS84_LAT_MIN);
		add(BBOX_WGS84_LAT_MAX);
		add(BBOX_WGS84_LON_MIN);
		add(BBOX_WGS84_LON_MAX);
		add(BBOX_WGS84_HEIGHT_MIN);
		add(BBOX_WGS84_HEIGHT_MAX);
		add(BBOX_WGS84_LAT_LON);
		add(BBOX_WGS84_LON_LAT);
	}};
	
	private static HashMap<String, Set<String>> _3DCITYDB_TABLES_AND_COLUMNS = new HashMap<String, Set<String>>() {{
		put(ADDRESS_TABLE, ADDRESS_COLUMNS);
		put(ADDRESS_TO_BUILDING_TABLE, ADDRESS_TO_BUILDING_COLUMNS);
		put(APPEAR_TO_SURFACE_DATA_TABLE, APPEAR_TO_SURFACE_DATA_COLUMNS);
		put(APPEARANCE_TABLE, APPEARANCE_COLUMNS);
//		put(BREAKLINE_RELIEF_TABLE, BREAKLINE_RELIEF_COLUMNS);
		put(BUILDING_TABLE, BUILDING_COLUMNS);
		put(BUILDING_INSTALLATION_TABLE, BUILDING_INSTALLATION_COLUMNS);
		put(CITY_FURNITURE_TABLE, CITY_FURNITURE_COLUMNS);
		put(CITYMODEL_TABLE, CITYMODEL_COLUMNS);
		put(CITYOBJECT_TABLE, CITYOBJECT_COLUMNS);
		put(CITYOBJECT_GENERICATTRIB_TABLE, CITYOBJECT_GENERICATTRIB_COLUMNS);
		put(CITYOBJECTGROUP_TABLE, CITYOBJECTGROUP_COLUMNS);
		put(CITYOBJECT_MEMBER_TABLE, CITYOBJECT_MEMBER_COLUMNS);
		put(COLLECT_GEOM_TABLE, COLLECT_GEOM_COLUMNS);
		put(DATABASE_SRS_TABLE, DATABASE_SRS_COLUMNS);
		put(EXTERNAL_REFERENCE_TABLE, EXTERNAL_REFERENCE_COLUMNS);
		put(GENERALIZATION_TABLE, GENERALIZATION_COLUMNS);
		put(GENERIC_CITYOBJECT_TABLE, GENERIC_CITYOBJECT_COLUMNS);
		put(GROUP_TO_CITYOBJECT_TABLE, GROUP_TO_CITYOBJECT_COLUMNS);
		put(LAND_USE_TABLE, LAND_USE_COLUMNS);
//		put(MASSPOINT_RELIEF_TABLE, MASSPOINT_RELIEF_COLUMNS);
		put(OBJECTCLASS_TABLE, OBJECTCLASS_COLUMNS);
		put(OPENING_TABLE, OPENING_COLUMNS);
		put(OPENING_TO_THEM_SURFACE_TABLE, OPENING_TO_THEM_SURFACE_COLUMNS);
		put(PLANT_COVER_TABLE, PLANT_COVER_COLUMNS);
//		put(RASTER_RELIEF_TABLE, RASTER_RELIEF_COLUMNS);
		put(RASTER_RELIEF_IMP_TABLE, RASTER_RELIEF_IMP_COLUMNS);
		put(RELIEF_COMPONENT_TABLE, RELIEF_COMPONENT_COLUMNS);
		put(RELIEF_FEATURE_TABLE, RELIEF_FEATURE_COLUMNS);
//		put(RELIEF_FEAT_TO_REL_COMP_TABLE, RELIEF_FEAT_TO_REL_COMP_COLUMNS);
		put(ROOM_TABLE, ROOM_COLUMNS);
		put(SOLITARY_VEGETAT_OBJECT_TABLE, SOLITARY_VEGETAT_OBJECT_COLUMNS);
		put(SPECIAL_KEYWORDS, SPECIAL_KEYWORDS_SET);
		put(SURFACE_DATA_TABLE, SURFACE_DATA_COLUMNS);
		put(SURFACE_GEOMETRY_TABLE, SURFACE_GEOMETRY_COLUMNS);
		put(TEXTUREPARAM_TABLE, TEXTUREPARAM_COLUMNS);
		put(THEMATIC_SURFACE_TABLE, THEMATIC_SURFACE_COLUMNS);
//		put(TIN_RELIEF_TABLE, TIN_RELIEF_COLUMNS);
		put(TRAFFIC_AREA_TABLE, TRAFFIC_AREA_COLUMNS);
		put(TRANSPORTATION_COMPLEX_TABLE, TRANSPORTATION_COMPLEX_COLUMNS);
		put(WATERBODY_TO_WATERBOUNDARY_SURFACE_TABLE, WATERBODY_TO_WATERBOUNDARY_SURFACE_COLUMNS);
		put(WATERBODY_TABLE, WATERBODY_COLUMNS);
		put(WATERBOUNDARY_SURFACE_TABLE, WATERBOUNDARY_SURFACE_COLUMNS);
	}};

	public HashMap<String, Set<String>> getSupportedTablesAndColumns() {
		return _3DCITYDB_TABLES_AND_COLUMNS;
	}

/*
	public Set<String> getSpecialKeywords() {
		return SPECIAL_KEYWORDS;
	}
*/
	public static final String balloonDirectoryName = "balloons";
	public static final String parentFrameStart =
		"<html>\n" +
		"  <body onload=\"resizeFrame(document.getElementById('childframe'))\">\n" +
		"    <script type=\"text/javascript\">\n" +
		"      function resizeFrame(f) {\n" +
		"        f.style.height = (f.contentWindow.document.body.scrollHeight + 20) + \"px\";\n" +
		"        f.style.width = (f.contentWindow.document.body.scrollWidth + 20) + \"px\";\n" +
      	"      }\n" +
      	"    </script>\n" +
      	"    <iframe frameborder=0 border=0 src=\"./" + balloonDirectoryName + "/";

	public static final String parentFrameEnd = ".html\" id=\"childframe\"></iframe>\n" +
		"  </body>\n" +
		"</html>";

	private Connection connection;
	private CityGMLClass cityGMLClassForBalloonHandler = null;

	List<BalloonStatement> statementList = null;
	List<String> htmlChunkList = null;

	public BalloonTemplateHandlerImpl(File templateFile, Connection connection) {
		setConnection(connection);
		setTemplate(templateFile);
	}

	public BalloonTemplateHandlerImpl(String templateString, Connection connection) {
		setConnection(connection);
		setTemplate(templateString);
	}

	private void setConnection(Connection connection) {
		this.connection = connection;
	}

	private void setTemplate(File templateFile) {
		statementList = new ArrayList<BalloonStatement>();
		htmlChunkList = new ArrayList<String>();

		if (templateFile == null) return; // it was a dummy call
		
		// read file as String
	    byte[] buffer = new byte[(int)templateFile.length()];
	    FileInputStream f = null;
	    try {
		    f = new FileInputStream(templateFile);
		    f.read(buffer);
	    }
	    catch (FileNotFoundException fnfe) {
			Logger.getInstance().warn("Exception when trying to read file: " + templateFile.getAbsolutePath() + "\nFile not found.");
	    } 
	    catch (Exception e) {
			Logger.getInstance().warn("Exception when trying to read file: " + templateFile.getAbsolutePath() + "\n");
			e.printStackTrace();
	    } 
	    finally {
	        if (f != null) try { f.close(); } catch (Exception ignored) { }
	    }
	    String template = new String(buffer);
		try {
			fillStatementAndHtmlChunkList(template);
		}
		catch (Exception e) {
			Logger.getInstance().warn("Following message applies to file: " + templateFile.getAbsolutePath());
			Logger.getInstance().warn(e.getMessage());
		}
	}
	
	private void setTemplate(String templateString) {
		statementList = new ArrayList<BalloonStatement>();
		htmlChunkList = new ArrayList<String>();

		if (templateString == null) return; // it was a dummy call
		
		try {
			fillStatementAndHtmlChunkList(templateString);
		}
		catch (Exception e) {
			Logger.getInstance().warn(e.getMessage());
		}
	}
	
	public String getBalloonContent(String template, long id, int lod) throws Exception {
		if (connection == null) throw new SQLException("Null or invalid connection");

		String balloonContent = "";
		List<BalloonStatement> statementListBackup = statementList;
		List<String> htmlChunkListBackup = htmlChunkList;
		statementList = new ArrayList<BalloonStatement>();
		htmlChunkList = new ArrayList<String>();
		try {
			fillStatementAndHtmlChunkList(template);
			balloonContent = getBalloonContent(id, lod);
		}
		catch (Exception e) {
			Logger.getInstance().warn("Following message applies to generic attribute 'Balloon_Content' for cityobject with id = " + id);
			Logger.getInstance().warn(e.getMessage());
		}
		statementList = statementListBackup;
		htmlChunkList = htmlChunkListBackup;
		return balloonContent;
	}

	public String getBalloonContent(String gmlId, int lod) throws Exception {
		if (connection == null) throw new SQLException("Null or invalid connection");
		if (statementList == null && htmlChunkList == null) throw new Exception("Invalid template file"); 

		StringBuffer balloonContent = new StringBuffer();
		
		if (statementList != null) {
			// when properly initialized this happens only at the first object
			// otherwise it avoids problems from lousy initialization of BalloonTemplateHandlers in threads
			// at the cost of performance

			CityGMLClass cityObjectTypeForGmlId = null;
			long id = -1;
			
			OracleResultSet rs = null;
			PreparedStatement query = null;
			try {
				query = connection.prepareStatement(Queries.GET_ID_AND_OBJECTCLASS_FROM_GMLID);
				query.setString(1, gmlId);
				rs = (OracleResultSet)query.executeQuery();
				
				if (rs.next()) {
					cityObjectTypeForGmlId = Util.classId2cityObject(rs.getInt("class_id"));
					id = rs.getLong("id");
				}
			}
			catch (SQLException sqlEx) {}
			finally {
				if (rs != null) {
					try { rs.close(); }	catch (SQLException sqlEx) {}
					rs = null;
				}

				if (query != null) {
					try { query.close(); } catch (SQLException sqlEx) {}
					query = null;
				}
			}
			
			if (cityGMLClassForBalloonHandler != cityObjectTypeForGmlId) {
				for (BalloonStatement statement: statementList) {
					statement.setConversionTried(false);
				}
				cityGMLClassForBalloonHandler = cityObjectTypeForGmlId;
			}
			
			List<String> resultList = new ArrayList<String>();
			for (BalloonStatement statement: statementList) {
				resultList.add(executeStatement(statement, id, lod));
			}

			Iterator<String> htmlChunkIterator = htmlChunkList.iterator();
			Iterator<String> resultIterator = resultList.iterator();

			while (htmlChunkIterator.hasNext()) {
				balloonContent.append(htmlChunkIterator.next());
				if (resultIterator.hasNext()) {
					balloonContent.append(resultIterator.next());
				}
			}
		}
		return balloonContent.toString();
	}
	
	public String getBalloonContent(long id, int lod) throws Exception {
		if (connection == null) throw new SQLException("Null or invalid connection");
		if (statementList == null && htmlChunkList == null) throw new Exception("Invalid template file"); 

		StringBuffer balloonContent = new StringBuffer();
		
		if (statementList != null) {
			// when properly initialized this happens only at the first object
			// otherwise it avoids problems from lousy initialization of BalloonTemplateHandlers in threads
			// at the cost of performance

			CityGMLClass cityObjectTypeForId = null;
			
			OracleResultSet rs = null;
			PreparedStatement query = null;
			try {
				query = connection.prepareStatement(Queries.GET_GMLID_AND_OBJECTCLASS_FROM_ID);
				query.setLong(1, id);
				rs = (OracleResultSet)query.executeQuery();
				
				if (rs.next()) {
					cityObjectTypeForId = Util.classId2cityObject(rs.getInt("class_id"));
				}
			}
			catch (SQLException sqlEx) {}
			finally {
				if (rs != null) {
					try { rs.close(); }	catch (SQLException sqlEx) {}
					rs = null;
				}

				if (query != null) {
					try { query.close(); } catch (SQLException sqlEx) {}
					query = null;
				}
			}
			
			if (cityGMLClassForBalloonHandler != cityObjectTypeForId) {
				for (BalloonStatement statement: statementList) {
					statement.setConversionTried(false);
				}
				cityGMLClassForBalloonHandler = cityObjectTypeForId;
			}
			
			List<String> resultList = new ArrayList<String>();
			for (BalloonStatement statement: statementList) {
				resultList.add(executeStatement(statement, id, lod));
			}

			Iterator<String> htmlChunkIterator = htmlChunkList.iterator();
			Iterator<String> resultIterator = resultList.iterator();

			while (htmlChunkIterator.hasNext()) {
				balloonContent.append(htmlChunkIterator.next());
				if (resultIterator.hasNext()) {
					balloonContent.append(resultIterator.next());
				}
			}
		}
		return balloonContent.toString();
	}
	
	private String executeStatement(BalloonStatement statement, long id, int lod) {
		String result = "";
		if (statement != null) {
			PreparedStatement preparedStatement = null;
			OracleResultSet rs = null;
			try {
				if (statement.isForeach()) {
					return executeForeachStatement(statement, id, lod);
				}

				if (statement.isNested()) {
					String rawStatement = statement.getRawStatement();
					List<String> textBetweenNestedStatements = new ArrayList<String>();
					List<BalloonStatement> nestedStatementList = new ArrayList<BalloonStatement>();
					int nestingLevel = 0;
					int lastIndex = 0;
					int index = 0;
					int beginOfSubexpression = 0;

					while (nestingLevel > 0 || rawStatement.indexOf(END_TAG, index) > -1) {
						int indexOfNextStart = rawStatement.indexOf(START_TAG, index);
						int indexOfNextEnd = rawStatement.indexOf(END_TAG, index);
						if (indexOfNextStart != -1 && indexOfNextStart < indexOfNextEnd) {
							nestingLevel++;
							if (nestingLevel == 1) {
								textBetweenNestedStatements.add(rawStatement.substring(lastIndex, indexOfNextStart));
								beginOfSubexpression = indexOfNextStart + START_TAG.length();
							}
							index = indexOfNextStart + START_TAG.length();
						}
						else {
							nestingLevel--;
							index = indexOfNextEnd;
							if (nestingLevel == 0) {
								String originalNestedStatement = rawStatement.substring(beginOfSubexpression, index);
								BalloonStatement nestedStatement = new BalloonStatement(originalNestedStatement);
								nestedStatement.setNested(originalNestedStatement.contains(START_TAG));
								nestedStatementList.add(nestedStatement);
								lastIndex = index + END_TAG.length();
							}
							index = index + END_TAG.length();
						}
					}
					textBetweenNestedStatements.add(rawStatement.substring(index));
					
					StringBuffer notNestedAnymore = new StringBuffer();
					if (nestedStatementList != null) {
						List<String> resultList = new ArrayList<String>();
						for (BalloonStatement nestedStatement: nestedStatementList) {
							resultList.add(executeStatement(nestedStatement, id, lod));
						}

						Iterator<String> textIterator = textBetweenNestedStatements.iterator();
						Iterator<String> resultIterator = resultList.iterator();

						while (textIterator.hasNext()) {
							notNestedAnymore.append(textIterator.next());
							if (resultIterator.hasNext()) {
								notNestedAnymore.append(resultIterator.next());
							}
						}
					}

					BalloonStatement dummy = new BalloonStatement(notNestedAnymore.toString());
					preparedStatement = connection.prepareStatement(dummy.getProperSQLStatement(lod));
				}
				else { // not nested
					if (statement.getProperSQLStatement(lod) == null) {
						// malformed expression between proper START_TAG and END_TAG
						return result; // skip db call, rs and preparedStatement are currently null
					}
					preparedStatement = connection.prepareStatement(statement.getProperSQLStatement(lod));
				}

				for (int i = 1; i <= preparedStatement.getParameterMetaData().getParameterCount(); i++) {
					preparedStatement.setLong(i, id);
				}
				rs = (OracleResultSet)preparedStatement.executeQuery();
				while (rs.next()) {
					if (rs.getRow() > 1) {
						result = result + ", ";
					}
					Object object = rs.getObject(1);
					if (object != null) {
						if (object instanceof STRUCT){
							STRUCT buildingGeometryObj = (STRUCT)rs.getObject(1); 
							JGeometry surface = JGeometry.load(buildingGeometryObj);
							int dimensions = surface.getDimensions();
							double[] ordinatesArray = surface.getType() == JGeometry.GTYPE_POINT ?
													  surface.getPoint():
													  surface.getOrdinatesArray();
							result = result + "(";
							for (int i = 0; i < ordinatesArray.length; i = i + dimensions) {
								for (int j = 0; j < dimensions; j++) {
									result = result + ordinatesArray [i+j];
									if (j < dimensions-1) {
										result = result + ",";
									}
								}
								if (i+dimensions < ordinatesArray.length) {
									result = result + " ";
								}
							}
							result = result + ")";
						}
						else {
							result = result + rs.getObject(1).toString().replaceAll("\"", "&quot;"); // workaround, the JAXB KML marshaler does not escape " properly;
						}
					}
				}
			}
			catch (Exception e) {
				Logger.getInstance().warn(e.getMessage());
//				Logger.getInstance().warn("Exception when executing balloon statement: " + statement + "\n");
//				e.printStackTrace();
			}
			finally {
				try {
					if (rs != null) rs.close();
					if (preparedStatement != null) preparedStatement.close();
				}
				catch (Exception e2) {}
			}
		}
		return result;
	}

	private String executeForeachStatement(BalloonStatement statement, long id, int lod) {
		String resultBody = "";
		PreparedStatement preparedStatement = null;
		OracleResultSet rs = null;
		try {
			if (statement != null && statement.getProperSQLStatement(lod) != null) {
				preparedStatement = connection.prepareStatement(statement.getProperSQLStatement(lod));
				for (int i = 1; i <= preparedStatement.getParameterMetaData().getParameterCount(); i++) {
					preparedStatement.setLong(i, id);
				}

				rs = (OracleResultSet)preparedStatement.executeQuery();
				while (rs.next()) {
					String iterationBody = statement.getForeachBody();
					for (int n = 0; n <= statement.getColumnAmount(); n++) {
						String columnValue = "";
						if (n == 0) {
							columnValue = String.valueOf(rs.getRow());
						}
						else {
							Object object = rs.getObject(n);
							if (object != null) {
								if (object instanceof STRUCT){
									STRUCT buildingGeometryObj = (STRUCT)rs.getObject(1); 
									JGeometry surface = JGeometry.load(buildingGeometryObj);
									int dimensions = surface.getDimensions();
									double[] ordinatesArray = surface.getOrdinatesArray();
									columnValue = columnValue + "(";
									for (int i = 0; i < ordinatesArray.length; i = i + dimensions) {
										for (int j = 0; j < dimensions; j++) {
											columnValue = columnValue + ordinatesArray [i+j];
											if (j < dimensions-1) {
												columnValue = columnValue + ",";
											}
										}
										if (i + dimensions < ordinatesArray.length) {
											columnValue = columnValue + " ";
										}
									}
									columnValue = columnValue + ")";
								}
								else {
									columnValue = rs.getObject(n).toString().replaceAll("\"", "&quot;"); // workaround, the JAXB KML marshaler does not escape " properly
								}
							}
						}
						iterationBody = iterationBody.replaceAll("%" + n, columnValue);
					}
					resultBody = resultBody + iterationBody;
				}
			}
		}
		catch (Exception e) {
			Logger.getInstance().warn(e.getMessage());
		}
		finally {
			try {
				if (rs != null) rs.close();
				if (preparedStatement != null) preparedStatement.close();
			}
			catch (Exception e2) {}
		}
		return resultBody;
	}

	private void fillStatementAndHtmlChunkList(String template) throws Exception {
		// parse like it's 1999
	    int lastIndex = 0;
	    int index = 0;
	    while (template.indexOf(START_TAG, lastIndex) != -1) {

			index = template.indexOf(START_TAG, lastIndex);
			int nestingLevel = 1;
			htmlChunkList.add(template.substring(lastIndex, index));
			index = index + START_TAG.length();
			int beginOfExpression = index;
			while (nestingLevel > 0) {
				int indexOfNextStart = template.indexOf(START_TAG, index);
				int indexOfNextEnd = template.indexOf(END_TAG, index);
				if (indexOfNextEnd == -1) {
					throw new Exception("Malformed balloon template. Please review nested " + START_TAG + " expressions.");
				}
				if (indexOfNextStart != -1 && indexOfNextStart < indexOfNextEnd) {
					nestingLevel++;
					index = indexOfNextStart + START_TAG.length();
				}
				else {
					nestingLevel--;
					index = indexOfNextEnd;
					if (nestingLevel == 0) {
						String originalStatement = template.substring(beginOfExpression, index).trim();
						BalloonStatement statement = new BalloonStatement(originalStatement);
						statement.setNested(originalStatement.contains(START_TAG));
						statement.setForeach(originalStatement.toUpperCase().startsWith(FOREACH_TAG));
						if (statement.isForeach()) {
							// look for END FOREACH statement
							index = index + END_TAG.length();
							indexOfNextStart = template.indexOf(START_TAG, index);
							indexOfNextEnd = template.indexOf(END_TAG, index);
							if (indexOfNextStart == -1 || indexOfNextEnd == -1 || indexOfNextEnd < indexOfNextStart) {
								throw new Exception("Malformed balloon template. Please review " + START_TAG + FOREACH_TAG + " expressions.");
							}
							String closingStatement = template.substring(indexOfNextStart + START_TAG.length(), indexOfNextEnd).trim();
							if (!END_FOREACH_TAG.equalsIgnoreCase(closingStatement)) {
								throw new Exception("Malformed balloon template. Please review " + START_TAG + FOREACH_TAG + " expressions.");
							}
							statement.setForeachBody(template.substring(index, indexOfNextStart));
							index = indexOfNextEnd;
						}
						statementList.add(statement);
						lastIndex = index + END_TAG.length();
					}
					index = index + END_TAG.length();
				}
			}
	    }
	    htmlChunkList.add(template.substring(index)); // last chunk
	}


	private class BalloonStatement {
		private String rawStatement;
		private boolean nested = false;
		private String properSQLStatement = null;
		private boolean conversionTried = false;
		private int columnAmount;
		private boolean foreach = false;
		private String foreachBody;

		private String tableShortId;
		private boolean orderByColumnAllowed = true;

		BalloonStatement (String rawStatement) {
			this.setRawStatement(rawStatement);
		}

		private void setRawStatement(String rawStatement) {
			this.rawStatement = rawStatement;
		}

		private String getRawStatement() {
			return rawStatement;
		}

		private void setNested(boolean nested) {
			this.nested = nested;
		}

		private boolean isNested() {
			return nested;
		}

		private void setProperSQLStatement(String properSQLStatement) {
			this.properSQLStatement = properSQLStatement;
		}

		private String getProperSQLStatement(int lod) throws Exception {
			if (!conversionTried && properSQLStatement == null) {
				this.convertStatementToProperSQL(lod);
				conversionTried = true;
			}
			return properSQLStatement;
		}

		private void setConversionTried(boolean conversionTried) {
			this.conversionTried = conversionTried;
		}

		private boolean isForeach() {
			return foreach;
		}

		private void setForeach(boolean foreach) {
			this.foreach = foreach;
		}

		private String getForeachBody() {
			return foreachBody;
		}

		private void setForeachBody(String foreachBody) {
			this.foreachBody = foreachBody;
		}

		private int getColumnAmount() {
			return columnAmount;
		}

		private void setColumnAmount(int columnAmount) {
			this.columnAmount = columnAmount;
		}
		
		private void convertStatementToProperSQL(int lod) throws Exception {

			String sqlStatement = null; 
			String table = null;
			String aggregateFunction = null;
			List<String> columns = null;
			String condition = null;
			
			int index = rawStatement.indexOf('/');
			if (index == -1) {
				throw new Exception("Invalid statement \"" + rawStatement + "\". Column name not set.");
			}
			
			if (isForeach()) {
				table = rawStatement.substring(FOREACH_TAG.length(), index).trim();
			}
			else {
				table = rawStatement.substring(0, index).trim();
			}

			index++;
			if (SPECIAL_KEYWORDS.equalsIgnoreCase(table)) {
				sqlStatement = checkForSpecialKeywords(rawStatement.substring(index));
				if (sqlStatement != null) {
					setProperSQLStatement(sqlStatement);
					return;
				}
			}

			if (index >= rawStatement.length()) {
				throw new Exception("Invalid statement \"" + rawStatement + "\". Column name not set.");
			}
			if (rawStatement.charAt(index) == '[') { // beginning of aggregate function
				if (isForeach()) {
					throw new Exception("Invalid statement \"" + rawStatement + "\". No aggregation functions allowed here.");
				}
				index++;
				if (index >= rawStatement.length()) {
					throw new Exception("Invalid statement \"" + rawStatement + "\"");
				}
				if (rawStatement.indexOf(']', index) == -1) {
					throw new Exception("Invalid statement \"" + rawStatement + "\". Missing ']' character.");
				}
				aggregateFunction = rawStatement.substring(index, rawStatement.indexOf(']', index)).trim();
				index = rawStatement.indexOf(']', index) + 1;
				if (index >= rawStatement.length()) {
					throw new Exception("Invalid statement \"" + rawStatement + "\". Column name not set.");
				}
			}

			String columnsClauseString = null;
			if (rawStatement.indexOf('[', index) == -1) { // no condition
				columnsClauseString = rawStatement.substring(index).trim();
			}
			else {
				columnsClauseString = rawStatement.substring(index, rawStatement.indexOf('[', index)).trim();
				index = rawStatement.indexOf('[', index) + 1;
				if (index >= rawStatement.length()) {
					throw new Exception("Invalid statement \"" + rawStatement + "\"");
				}
				if (rawStatement.indexOf(']', index) == -1) {
					throw new Exception("Invalid statement \"" + rawStatement + "\". Missing ']' character.");
				}
				condition = rawStatement.substring(index, rawStatement.indexOf(']', index)).trim();
				try {
					if (Integer.parseInt(condition) < 0) {
						throw new Exception("Invalid condition \"" + condition + "\" in statement \"" + rawStatement);
					}
				}
				catch (NumberFormatException nfe) {
					ArrayList<Integer> indexOfPossibleSeparators = new ArrayList<Integer>();
					indexOfPossibleSeparators.add(condition.indexOf('='));
					indexOfPossibleSeparators.add(condition.indexOf("!="));
					indexOfPossibleSeparators.add(condition.indexOf("<>"));
					indexOfPossibleSeparators.add(condition.indexOf('<'));
					indexOfPossibleSeparators.add(condition.indexOf('>'));
					indexOfPossibleSeparators.add(condition.indexOf(" LIKE "));
					indexOfPossibleSeparators.add(condition.indexOf(" IS NULL "));
					indexOfPossibleSeparators.add(condition.indexOf(" IS NOT NULL "));

					int indexOfSeparator = Collections.max(indexOfPossibleSeparators);

					if (indexOfSeparator < 1) {
						throw new Exception("Invalid condition \"" + condition + "\" in statement \"" + rawStatement);
					}
					String conditionColumnName = condition.substring(0, indexOfSeparator).trim();
					if (!_3DCITYDB_TABLES_AND_COLUMNS.get(table).contains(conditionColumnName)) {
						throw new Exception("Unsupported column \"" + conditionColumnName + "\" in statement \"" + rawStatement + "\"");
					}
				}
			}

			if (columnsClauseString == null) {
				throw new Exception("Invalid statement \"" + rawStatement + "\". Column name not set.");
			}
			else {
				columns = new ArrayList<String>();
				StringTokenizer columnTokenizer = new StringTokenizer(columnsClauseString, ",");
				while (columnTokenizer.hasMoreTokens()) {
					columns.add(columnTokenizer.nextToken().toUpperCase().trim());
				}
				setColumnAmount(columns.size());
			}

			String aggregateString = "";
			String aggregateClosingString = "";
			if (aggregateFunction != null) {
				if (MAX.equalsIgnoreCase(aggregateFunction) ||
					MIN.equalsIgnoreCase(aggregateFunction) ||
					AVG.equalsIgnoreCase(aggregateFunction) ||
					COUNT.equalsIgnoreCase(aggregateFunction) ||
					SUM.equalsIgnoreCase(aggregateFunction)) {
					aggregateString = aggregateFunction + "(";
					aggregateClosingString = ")";
				}
				else if (!FIRST.equalsIgnoreCase(aggregateFunction) &&
						 !LAST.equalsIgnoreCase(aggregateFunction)) { 
					throw new Exception("Unsupported aggregate function \"" + aggregateFunction + "\" in statement \"" + rawStatement + "\"");
				}
			}

			switch (cityGMLClassForBalloonHandler) {
				case CITY_FURNITURE:
					sqlStatement = sqlStatementForCityFurniture(table, columns, aggregateString, aggregateClosingString, lod);
					break;
				case PLANT_COVER:
					sqlStatement = sqlStatementForPlantCover(table, columns, aggregateString, aggregateClosingString, lod);
					break;
				case SOLITARY_VEGETATION_OBJECT:
					sqlStatement = sqlStatementForSolVegObj(table, columns, aggregateString, aggregateClosingString, lod);
					break;
				case TRAFFIC_AREA:
				case AUXILIARY_TRAFFIC_AREA:
					sqlStatement = sqlStatementForTrafficArea(table, columns, aggregateString, aggregateClosingString, lod);
					break;
				case TRANSPORTATION_COMPLEX:
				case TRACK:
				case RAILWAY:
				case ROAD:
				case SQUARE:
					sqlStatement = sqlStatementForTransportationComplex(table, columns, aggregateString, aggregateClosingString, lod);
					break;
				case LAND_USE:
					sqlStatement = sqlStatementForLandUse(table, columns, aggregateString, aggregateClosingString, lod);
					break;
/*
				case RASTER_RELIEF:
				case MASSPOINT_RELIEF:
				case BREAKLINE_RELIEF:
				case TIN_RELIEF:
*/
				case RELIEF_FEATURE:
					sqlStatement = sqlStatementForRelief(table, columns, aggregateString, aggregateClosingString, lod);
					break;
				case WATER_BODY:
					sqlStatement = sqlStatementForWaterBody(table, columns, aggregateString, aggregateClosingString, lod);
					break;
				case WATER_CLOSURE_SURFACE:
				case WATER_GROUND_SURFACE:
				case WATER_SURFACE:
					sqlStatement = sqlStatementForWaterSurface(table, columns, aggregateString, aggregateClosingString, lod);
					break;
				case GENERIC_CITY_OBJECT:
					sqlStatement = sqlStatementForGenCityObj(table, columns, aggregateString, aggregateClosingString, lod);
					break;
				case BUILDING:
					sqlStatement = sqlStatementForBuilding(table, columns, aggregateString, aggregateClosingString, lod);
					break;
				default:
					sqlStatement = sqlStatementForAnyObject(table, columns, aggregateString, aggregateClosingString, lod);
					break;
			}

			if (sqlStatement != null) {
				int rownum = 0;
				if (condition != null) {
					try {
						rownum = Integer.parseInt(condition);
					}
					catch (Exception e) { // not a number, but a logical condition 
						sqlStatement = sqlStatement + " AND " + tableShortId + "." + condition;
					}
				}
				if (aggregateFunction == null) {
					if (orderByColumnAllowed) {
						sqlStatement = sqlStatement + " ORDER by " + tableShortId + "." + columns.get(0);
					}
				}
				else {
					if (rownum > 0) {
						sqlStatement = "SELECT * FROM (SELECT a.*, ROWNUM rnum FROM (" + sqlStatement 
									   + " ORDER by " + tableShortId + "." + columns.get(0)
									   + " ASC) a WHERE ROWNUM <= " + rownum + ") WHERE rnum >= " + rownum;
					}
					else if (FIRST.equalsIgnoreCase(aggregateFunction)) {
						sqlStatement = "SELECT * FROM (" + sqlStatement
									   + " ORDER by " + tableShortId + "." + columns.get(0)
									   + " ASC) WHERE ROWNUM = 1";
					}
					else if (LAST.equalsIgnoreCase(aggregateFunction)) {
						sqlStatement = "SELECT * FROM (" + sqlStatement
									   + " ORDER by " + tableShortId + "." + columns.get(0)
									   + " DESC) WHERE ROWNUM = 1";
					}
					// no ORDER by for MAX, MIN, AVG, COUNT, SUM
				}
			}

			setProperSQLStatement(sqlStatement);
		}

		private String sqlStatementForBuilding(String table,
											   List<String> columns,
											   String aggregateString,
											   String aggregateClosingString,
											   int lod) throws Exception {
			String sqlStatement = null; 

			if (ADDRESS_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM ADDRESS_TO_BUILDING a2b, ADDRESS a" +
							   " WHERE a2b.building_id = ?" +
							   " AND a.id = a2b.address_id";
			}
			else if (ADDRESS_TO_BUILDING_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM ADDRESS_TO_BUILDING a2b" +
							   " WHERE a2b.building_id = ?";
			}
			else if (BUILDING_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM BUILDING b" +
							   " WHERE b.id = ?";
			}
			else if (BUILDING_INSTALLATION_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM BUILDING b, BUILDING_INSTALLATION bi" +
							   " WHERE b.building_root_id = ?" +
							   " AND bi.building_id = b.id";
			}
			else if (OPENING_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM BUILDING b, THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE o2ts, OPENING o" +
							   " WHERE b.building_root_id = ?" +
							   " AND ts.building_id = b.id" +
							   " AND o2ts.thematic_surface_id = ts.id" +
							   " AND o.id = o2ts.opening_id";
			}
			else if (OPENING_TO_THEM_SURFACE_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM BUILDING b, THEMATIC_SURFACE ts, OPENING_TO_THEM_SURFACE o2ts" +
							   " WHERE b.building_root_id = ?" +
							   " AND ts.building_id = b.id" +
							   " AND o2ts.thematic_surface_id = ts.id";
			}
			else if (ROOM_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM BUILDING b, ROOM r" +
							   " WHERE b.building_root_id = ?" +
							   " AND r.building_id = b.id";
			}
			else if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM SURFACE_GEOMETRY sg" +
							   " WHERE sg.id IN" +
							   " (SELECT sg.id" +
							   " FROM BUILDING b, SURFACE_GEOMETRY sg" +
							   " WHERE b.building_root_id = ?" +
							   " AND sg.root_id = b.lod" + lod + "_geometry_id";
				if (lod > 1) {
					sqlStatement = sqlStatement +
							   " UNION " +
							   "SELECT sg.id" +
							   " FROM BUILDING b, THEMATIC_SURFACE ts, SURFACE_GEOMETRY sg" +
							   " WHERE b.building_root_id = ?" +
							   " AND ts.building_id = b.id" + 
							   " AND sg.root_id = ts.lod" + lod + "_multi_surface_id";
				}
				sqlStatement = sqlStatement + ")";
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
						   	   " FROM TEXTUREPARAM tp" +
						   	   " WHERE tp.surface_geometry_id IN" +
						   	   " (SELECT sg.id" + 
						   	   " FROM BUILDING b, SURFACE_GEOMETRY sg" + 
						   	   " WHERE b.building_root_id = ?" +
						   	   " AND sg.root_id = b.lod" + lod + "_geometry_id";
				if (lod > 1) {
					sqlStatement = sqlStatement +
							   " UNION " +
						   	   "SELECT sg.id" + 
						   	   " FROM BUILDING b, THEMATIC_SURFACE ts, SURFACE_GEOMETRY sg" +
						   	   " WHERE b.building_root_id = ?" +
							   " AND ts.building_id = b.id" + 
						   	   " AND sg.root_id = ts.lod" + lod + "_multi_surface_id";
				}
				sqlStatement = sqlStatement + ")";
			}
			else if (THEMATIC_SURFACE_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM BUILDING b, THEMATIC_SURFACE ts" +
							   " WHERE b.building_root_id = ?" +
							   " AND ts.building_id = b.id";
			}
			else {
				sqlStatement = sqlStatementForAnyObject(table, columns, aggregateString, aggregateClosingString, lod);
			}

			return sqlStatement; 
		}

		private String sqlStatementForLandUse(String table,
				 							  List<String> columns,
				 							  String aggregateString,
				 							  String aggregateClosingString,
				 							  int lod) throws Exception {
			String sqlStatement = null; 

			if (LAND_USE_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM LAND_USE lu" +
							   " WHERE lu.id = ?";
			}
			else if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM SURFACE_GEOMETRY sg, LAND_USE lu" + 
							   " WHERE lu.id = ?" +
							   " AND sg.root_id = lu.lod" + lod + "_multi_surface_id" + 
							   " AND sg.geometry IS NOT NULL";
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TEXTUREPARAM tp, SURFACE_GEOMETRY sg, LAND_USE lu" +
							   " WHERE lu.id = ?" +
							   " AND sg.root_id = lu.lod" + lod + "_multi_surface_id" + 
							   " AND sg.geometry IS NOT NULL" + 
							   " AND tp.surface_geometry_id = sg.id";
			}
			else {
				sqlStatement = sqlStatementForAnyObject(table, columns, aggregateString, aggregateClosingString, lod);
			}

			return sqlStatement; 
		}

		private String sqlStatementForSolVegObj(String table,
												List<String> columns,
												String aggregateString,
												String aggregateClosingString,
												int lod) throws Exception {
			String sqlStatement = null; 

			if (SOLITARY_VEGETAT_OBJECT_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM SOLITARY_VEGETAT_OBJECT svo" +
							   " WHERE svo.id = ?";
			}
			else if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM SURFACE_GEOMETRY sg" +
							   " WHERE sg.root_id IN" +
							   " (SELECT ig.relative_geometry_id" + 
							   " FROM SOLITARY_VEGETAT_OBJECT svo, IMPLICIT_GEOMETRY ig" + 
							   " WHERE svo.id = ?" +
							   " AND ig.id = svo.lod" + lod + "_implicit_rep_id" +
							   " UNION " +
							   "SELECT svo.lod" + lod + "_geometry_id" +
							   " FROM SOLITARY_VEGETAT_OBJECT svo" + 
							   " WHERE svo.id = ?)" +
							   " AND sg.geometry IS NOT NULL";
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TEXTUREPARAM tp" +
							   " WHERE tp.surface_geometry_id IN" +
							   " (SELECT sg.id" + 
							   " FROM SURFACE_GEOMETRY sg" +
							   " WHERE sg.root_id IN" +
							   " (SELECT ig.relative_geometry_id" + 
							   " FROM SOLITARY_VEGETAT_OBJECT svo, IMPLICIT_GEOMETRY ig" + 
							   " WHERE svo.id = ?" +
							   " AND ig.id = svo.lod" + lod + "_implicit_rep_id" +
							   " UNION " +
							   "SELECT svo.lod" + lod + "_geometry_id" +
							   " FROM SOLITARY_VEGETAT_OBJECT svo" + 
							   " WHERE svo.id = ?)" +
							   " AND sg.geometry IS NOT NULL)";
			}
			else if (PLANT_COVER_TABLE.equalsIgnoreCase(table)) { } // tolerate but do nothing
			else {
				sqlStatement = sqlStatementForAnyObject(table, columns, aggregateString, aggregateClosingString, lod);
			}

			return sqlStatement; 
		}

		private String sqlStatementForPlantCover(String table,
				 								 List<String> columns,
				 								 String aggregateString,
				 								 String aggregateClosingString,
				 								 int lod) throws Exception {
			String sqlStatement = null; 

			if (PLANT_COVER_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM PLANT_COVER pc" +
							   " WHERE pc.id = ?";
			}
			else if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM SURFACE_GEOMETRY sg, PLANT_COVER pc" + 
							   " WHERE pc.id = ?" +
							   " AND sg.root_id = pc.lod" + lod + "_geometry_id" + 
							   " AND sg.geometry IS NOT NULL";
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TEXTUREPARAM tp, SURFACE_GEOMETRY sg, PLANT_COVER pc" +
							   " WHERE pc.id = ?" +
							   " AND sg.root_id = pc.lod" + lod + "_geometry_id" + 
							   " AND sg.geometry IS NOT NULL" + 
							   " AND tp.surface_geometry_id = sg.id";
			}
			else if (SOLITARY_VEGETAT_OBJECT_TABLE.equalsIgnoreCase(table)) { } // tolerate but do nothing
			else {
				sqlStatement = sqlStatementForAnyObject(table, columns, aggregateString, aggregateClosingString, lod);
			}

			return sqlStatement; 
		}

		private String sqlStatementForWaterBody(String table,
												List<String> columns,
												String aggregateString,
												String aggregateClosingString,
												int lod) throws Exception {
			String sqlStatement = null; 

			if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM SURFACE_GEOMETRY sg" +
							   " WHERE sg.id IN" +
							   " (SELECT sg.id" +
							   " FROM WATERBODY wb, SURFACE_GEOMETRY sg" +
							   " WHERE wb.id = ?" +
							   " AND sg.root_id = wb.lod" + lod + "_solid_id";
				if (lod < 2) {
					sqlStatement = sqlStatement +
							   " UNION " +
							   "SELECT sg.id" +
							   " FROM WATERBODY wb, SURFACE_GEOMETRY sg" +
							   " WHERE wb.id = ?" +
							   " AND sg.root_id = wb.lod" + lod + "_multi_surface_id";
				}
				else {
					sqlStatement = sqlStatement +
					   " UNION " +
					   "SELECT sg.id" +
					   " FROM WATERBOD_TO_WATERBND_SRF wb2wbs, WATERBOUNDARY_SURFACE wbs, SURFACE_GEOMETRY sg" +
					   " WHERE wb2wbs.waterbody_id = ?" +
					   " AND wbs.id = wb2wbs.waterboundary_surface_id" +
					   " AND sg.root_id = wbs.lod" + lod + "_surface_id";
				}
				sqlStatement = sqlStatement + ")";
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TEXTUREPARAM tp" +
							   " WHERE tp.surface_geometry_id IN" +
							   " (SELECT sg.id" +
							   " FROM WATERBODY wb, SURFACE_GEOMETRY sg" +
							   " WHERE wb.id = ?" +
							   " AND sg.root_id = wb.lod" + lod + "_solid_id";
				if (lod < 2) {
					sqlStatement = sqlStatement +
							   " UNION " +
							   "SELECT sg.id" +
							   " FROM WATERBODY wb, SURFACE_GEOMETRY sg" +
							   " WHERE wb.id = ?" +
							   " AND sg.root_id = wb.lod" + lod + "_multi_surface_id";
				}
				else {
					sqlStatement = sqlStatement +
					   " UNION " +
					   "SELECT sg.id" +
					   " FROM WATERBOD_TO_WATERBND_SRF wb2wbs, WATERBOUNDARY_SURFACE wbs, SURFACE_GEOMETRY sg" +
					   " WHERE wb2wbs.waterbody_id = ?" +
					   " AND wbs.id = wb2wbs.waterboundary_surface_id" +
					   " AND sg.root_id = wbs.lod" + lod + "_surface_id";
				}
				sqlStatement = sqlStatement + ")";
			}
			else if (WATERBODY_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM WATERBODY wb" +
							   " WHERE wb.id = ?";
			}
			else if (WATERBOUNDARY_SURFACE_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
				   			   " FROM WATERBOD_TO_WATERBND_SRF wb2wbs, WATERBOUNDARY_SURFACE wbs" +
				   			   " WHERE wb2wbs.waterbody_id = ?" +
				   			   " AND wbs.id = wb2wbs.waterboundary_surface_id";
			}
			else {
				sqlStatement = sqlStatementForAnyObject(table, columns, aggregateString, aggregateClosingString, lod);
			}

			return sqlStatement; 
		}

		private String sqlStatementForWaterSurface(String table,
												   List<String> columns,
												   String aggregateString,
												   String aggregateClosingString,
												   int lod) throws Exception {
			String sqlStatement = null; 

			if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM SURFACE_GEOMETRY sg" +
							   " WHERE sg.id IN ";
				if (lod > 1) {
					sqlStatement = sqlStatement +
							   "(SELECT sg.id" +
							   " FROM WATERBOUNDARY_SURFACE wbs, SURFACE_GEOMETRY sg" +
							   " WHERE wbs.id = ?" +
							   " AND sg.root_id = wbs.lod" + lod + "_surface_id" +
							   " UNION " +
							   "SELECT sg.id" +
							   " FROM WATERBOD_TO_WATERBND_SRF wb2wbs, WATERBODY wb, SURFACE_GEOMETRY sg" +
							   " WHERE wb2wbs.waterboundary_surface_id = ?" +
							   " AND wb2wbs.waterbody_id = wb.id" +
							   " AND sg.root_id = wb.lod" + lod + "_solid_id)";
				}
				else {
					sqlStatement = sqlStatement +
							   "(SELECT sg.id" +
							   " FROM WATERBOD_TO_WATERBND_SRF wb2wbs, WATERBODY wb, SURFACE_GEOMETRY sg" +
							   " WHERE wb2wbs.waterboundary_surface_id = ?" +
							   " AND wb2wbs.waterbody_id = wb.id" +
							   " AND sg.root_id = wb.lod" + lod + "_multi_surface_id)";
				}
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TEXTUREPARAM tp" +
							   " WHERE tp.surface_geometry_id IN ";
				if (lod > 1) {
					sqlStatement = sqlStatement +
							   "(SELECT sg.id" +
							   " FROM WATERBOUNDARY_SURFACE wbs, SURFACE_GEOMETRY sg" +
							   " WHERE wbs.id = ?" +
							   " AND sg.root_id = wbs.lod" + lod + "_surface_id" +
							   " UNION " +
							   "SELECT sg.id" +
							   " FROM WATERBOD_TO_WATERBND_SRF wb2wbs, WATERBODY wb, SURFACE_GEOMETRY sg" +
							   " WHERE wb2wbs.waterboundary_surface_id = ?" +
							   " AND wb2wbs.waterbody_id = wb.id" +
							   " AND sg.root_id = wb.lod" + lod + "_solid_id)";
				}
				else {
					sqlStatement = sqlStatement +
							   "(SELECT sg.id" +
							   " FROM WATERBOD_TO_WATERBND_SRF wb2wbs, WATERBODY wb, SURFACE_GEOMETRY sg" +
							   " WHERE wb2wbs.waterboundary_surface_id = ?" +
							   " AND wb2wbs.waterbody_id = wb.id" +
							   " AND sg.root_id = wb.lod" + lod + "_multi_surface_id)";
				}
			}
			else if (WATERBODY_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM WATERBOD_TO_WATERBND_SRF wb2wbs, WATERBODY wb" +
							   " WHERE wb2wbs.waterboundary_surface_id = ?" +
							   " AND wb2wbs.waterbody_id = wb.id";
			}
			else if (WATERBOUNDARY_SURFACE_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM WATERBOUNDARY_SURFACE wbs" +
							   " WHERE wbs.id = ?";
			}
			else {
				sqlStatement = sqlStatementForAnyObject(table, columns, aggregateString, aggregateClosingString, lod);
			}

			return sqlStatement; 
		}

		private String sqlStatementForTrafficArea(String table,
												  List<String> columns,
												  String aggregateString,
												  String aggregateClosingString,
												  int lod) throws Exception {
			String sqlStatement = null; 

			if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(table) && (lod > 0)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM SURFACE_GEOMETRY sg" +
							   " WHERE sg.id IN" +
							   " (SELECT sg.id" +
							   " FROM TRAFFIC_AREA ta, TRANSPORTATION_COMPLEX tc, SURFACE_GEOMETRY sg" +
							   " WHERE ta.id = ?" +
							   " AND tc.id = ta.transportation_complex_id" +
							   " AND sg.root_id = tc.lod" + lod + "_multi_surface_id";
				if (lod > 1) {
						sqlStatement = sqlStatement +
							   " UNION " +
							   "SELECT sg.id" +
							   " FROM TRAFFIC_AREA ta, SURFACE_GEOMETRY sg" +
							   " WHERE ta.id = ?" +
							   " AND sg.root_id = ta.lod" + lod + "_multi_surface_id";
				}
				sqlStatement = sqlStatement + ")";
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(table) && (lod > 0)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TEXTUREPARAM tp" +
							   " WHERE tp.surface_geometry_id IN" +
							   " (SELECT sg.id" +
							   " FROM TRAFFIC_AREA ta, TRANSPORTATION_COMPLEX tc, SURFACE_GEOMETRY sg" +
							   " WHERE ta.id = ?" +
							   " AND tc.id = ta.transportation_complex_id" +
							   " AND sg.root_id = tc.lod" + lod + "_multi_surface_id";
				if (lod > 1) {
						sqlStatement = sqlStatement +
							   " UNION " +
							   "SELECT sg.id" +
							   " FROM TRAFFIC_AREA ta, SURFACE_GEOMETRY sg" +
							   " WHERE ta.id = ?" +
							   " AND sg.root_id = ta.lod" + lod + "_multi_surface_id";
				}
				sqlStatement = sqlStatement + ")";
			}
			else if (TRAFFIC_AREA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TRAFFIC_AREA ta" +
							   " WHERE ta.id = ?";
			}
			else if (TRANSPORTATION_COMPLEX_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TRAFFIC_AREA ta, TRANSPORTATION_COMPLEX tc" +
							   " WHERE ta.id = ?" +
							   " AND tc.id = ta.transportation_complex_id";
			}
			else {
				sqlStatement = sqlStatementForAnyObject(table, columns, aggregateString, aggregateClosingString, lod);
			}

			return sqlStatement; 
		}
		
		private String sqlStatementForTransportationComplex(String table,
															List<String> columns,
															String aggregateString,
															String aggregateClosingString,
															int lod) throws Exception {
			String sqlStatement = null; 

			if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(table) && (lod > 0)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM SURFACE_GEOMETRY sg" +
							   " WHERE sg.id IN " +
							   "(SELECT sg.id" +
							   " FROM TRANSPORTATION_COMPLEX tc, SURFACE_GEOMETRY sg" +
							   " WHERE tc.id = ?" +
							   " AND sg.root_id = tc.lod" + lod + "_multi_surface_id)";
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(table) && (lod > 0)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TEXTUREPARAM tp" +
							   " WHERE tp.surface_geometry_id IN " +
							   "(SELECT sg.id" +
							   " FROM TRANSPORTATION_COMPLEX tc, SURFACE_GEOMETRY sg" +
							   " WHERE tc.id = ?" +
							   " AND sg.root_id = tc.lod" + lod + "_multi_surface_id)";
			}
			else if (TRAFFIC_AREA_TABLE.equalsIgnoreCase(table)) { } // tolerate but do nothing
			else if (TRANSPORTATION_COMPLEX_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TRANSPORTATION_COMPLEX tc" +
							   " WHERE tc.id = ?";
			}
			else {
				sqlStatement = sqlStatementForAnyObject(table, columns, aggregateString, aggregateClosingString, lod);
			}

			return sqlStatement; 
		}

		private String sqlStatementForRelief(String table,
											 List<String> columns,
											 String aggregateString,
											 String aggregateClosingString,
											 int lod) throws Exception {
			String sqlStatement = null; 

			if (RASTER_RELIEF_IMP_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM RASTER_RELIEF_IMP rri" +
							   " WHERE rri.id = ?";
			}
			else if (RELIEF_COMPONENT_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = /* "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM CITYOBJECT co, RELIEF_COMPONENT rc" +
							   " WHERE co.gmlid = ?" +
							   " AND rc.id = co.id" +
							   " UNION " + */
							   "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM RELIEF_FEATURE rf, RELIEF_FEAT_TO_REL_COMP rf2rc, RELIEF_COMPONENT rc" +
							   " WHERE rf.id = ?" +
							   " AND rf.lod = " + lod +
							   " AND rf2rc.relief_feature_id = rf.id" +
							   " AND rc.id = rf2rc.relief_component_id";
			}
			else if (RELIEF_FEATURE_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM RELIEF_FEATURE rf" +
							   " WHERE rf.id = ?";
			}
			else if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(table) && (lod > 0)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM SURFACE_GEOMETRY sg" +
							   " WHERE sg.id IN " +
							   "(SELECT sg.id" +
							   " FROM TIN_RELIEF tr, SURFACE_GEOMETRY sg" +
							   " WHERE tr.id = ?" +
							   " AND sg.root_id = tr.surface_geometry_id)";
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(table) && (lod > 0)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TEXTUREPARAM tp" +
							   " WHERE tp.surface_geometry_id IN " +
							   "(SELECT sg.id" +
							   " FROM TIN_RELIEF tr, SURFACE_GEOMETRY sg" +
							   " WHERE tr.id = ?" +
							   " AND sg.root_id = tr.surface_geometry_id)";
			}
/*
			else if (TIN_RELIEF_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM CITYOBJECT co, TIN_RELIEF tr" +
							   " WHERE co.gmlid = ?" +
							   " AND tr.id = co.id";
			}
*/
			else {
				sqlStatement = sqlStatementForAnyObject(table, columns, aggregateString, aggregateClosingString, lod);
			}

			return sqlStatement; 
		}

		private String sqlStatementForGenCityObj(String table,
												 List<String> columns,
												 String aggregateString,
												 String aggregateClosingString,
												 int lod) throws Exception {
			String sqlStatement = null; 

			if (GENERIC_CITYOBJECT_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM GENERIC_CITYOBJECT gco" +
							   " WHERE gco.id = ?";
			}
			else if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM SURFACE_GEOMETRY sg" +
							   " WHERE sg.root_id IN" +
							   " (SELECT ig.relative_geometry_id" + 
							   " FROM GENERIC_CITYOBJECT gco, IMPLICIT_GEOMETRY ig" + 
							   " WHERE gco.id = ?" +
							   " AND ig.id = gco.lod" + lod + "_implicit_rep_id" +
							   " UNION " +
							   "SELECT gco.lod" + lod + "_geometry_id" +
							   " FROM GENERIC_CITYOBJECT gco" + 
							   " WHERE gco.id = ?)" +
							   " AND sg.geometry IS NOT NULL";
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TEXTUREPARAM tp" +
							   " WHERE tp.surface_geometry_id IN" +
							   " (SELECT sg.id" + 
							   " FROM SURFACE_GEOMETRY sg" +
							   " WHERE sg.root_id IN" +
							   " (SELECT ig.relative_geometry_id" + 
							   " FROM GENERIC_CITYOBJECT gco, IMPLICIT_GEOMETRY ig" + 
							   " WHERE gco.id = ?" +
							   " AND ig.id = gco.lod" + lod + "_implicit_rep_id" +
							   " UNION " +
							   "SELECT gco.lod" + lod + "_geometry_id" +
							   " FROM GENERIC_CITYOBJECT gco" + 
							   " WHERE gco.id = ?)" +
							   " AND sg.geometry IS NOT NULL)";
			}
			else {
				sqlStatement = sqlStatementForAnyObject(table, columns, aggregateString, aggregateClosingString, lod);
			}

			return sqlStatement; 
		}

		private String sqlStatementForCityFurniture(String table,
				 									List<String> columns,
				 									String aggregateString,
				 									String aggregateClosingString,
				 									int lod) throws Exception {
			String sqlStatement = null;

			if (CITY_FURNITURE_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
				               " FROM CITY_FURNITURE cf" +
				               " WHERE cf.id = ?";
			}
			else if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM SURFACE_GEOMETRY sg" +
							   " WHERE sg.root_id IN" +
							   " (SELECT ig.relative_geometry_id" + 
							   " FROM CITY_FURNITURE cf, IMPLICIT_GEOMETRY ig" + 
							   " WHERE cf.id = ?" +
							   " AND ig.id = cf.lod" + lod + "_implicit_rep_id" +
							   " UNION " +
							   "SELECT cf.lod" + lod + "_geometry_id" +
							   " FROM CITY_FURNITURE cf" + 
							   " WHERE cf.id = ?)" +
							   " AND sg.geometry IS NOT NULL";
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM TEXTUREPARAM tp" +
							   " WHERE tp.surface_geometry_id IN" +
							   " (SELECT sg.id" + 
							   " FROM SURFACE_GEOMETRY sg" +
							   " WHERE sg.root_id IN" +
							   " (SELECT ig.relative_geometry_id" + 
							   " FROM CITY_FURNITURE cf, IMPLICIT_GEOMETRY ig" + 
							   " WHERE cf.id = ?" +
							   " AND ig.id = cf.lod" + lod + "_implicit_rep_id" +
							   " UNION " +
							   "SELECT cf.lod" + lod + "_geometry_id" +
							   " FROM CITY_FURNITURE cf" + 
							   " WHERE cf.id = ?)" +
							   " AND sg.geometry IS NOT NULL)";
			}
			else {
				sqlStatement = sqlStatementForAnyObject(table, columns, aggregateString, aggregateClosingString, lod);
			}

			return sqlStatement; 
		}

		private String sqlStatementForAnyObject(String table,
												List<String> columns,
												String aggregateString,
												String aggregateClosingString,
												int lod) throws Exception {
			String sqlStatement = null; 

			if (APPEAR_TO_SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id";
			}
			else if (APPEARANCE_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a" +
							   " WHERE a.cityobject_id = ?";
			}
	 		else if (CITYMODEL_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM CITYOBJECT_MEMBER com, CITYMODEL cm" +
							   " WHERE com.cityobject_id = ?" +
							   " AND cm.id = com.citymodel_id";
			}
			else if (CITYOBJECT_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM CITYOBJECT co" +
							   " WHERE co.id = ?";
			}
			else if (CITYOBJECT_GENERICATTRIB_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM CITYOBJECT_GENERICATTRIB coga" +
							   " WHERE coga.cityobject_id = ?";
			}
			else if (CITYOBJECTGROUP_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM CITYOBJECTGROUP cog" +
							   " WHERE cog.id = ?";
			}
	 		else if (CITYOBJECT_MEMBER_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM CITYOBJECT_MEMBER com" +
							   " WHERE com.cityobject_id = ?";
			}
	 		else if (COLLECT_GEOM_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM COLLECT_GEOM cg" +
							   " WHERE cg.cityobject_id = ?";
			}
			else if (DATABASE_SRS_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM DATABASE_SRS dbsrs"; // unrelated to object
			}
			else if (EXTERNAL_REFERENCE_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM EXTERNAL_REFERENCE er" +
							   " WHERE er.cityobject_id = ?";
			}
			else if (GENERALIZATION_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM GENERALIZATION g" +
							   " WHERE g.cityobject_id = ?";
			}
			else if (GROUP_TO_CITYOBJECT_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM GROUP_TO_CITYOBJECT g2co" +
							   " WHERE g2co.cityobjectgroup_id = ?";
			}
			else if (OBJECTCLASS_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM CITYOBJECT co, OBJECTCLASS oc" +
							   " WHERE co.id = ?" +
							   " AND oc.id = co.class_id";
			}
			else if (SURFACE_DATA_TABLE.equalsIgnoreCase(table)) {
				sqlStatement = "SELECT " + aggregateString + getColumnsClause(table, columns) + aggregateClosingString +
							   " FROM APPEARANCE a, APPEAR_TO_SURFACE_DATA a2sd, SURFACE_DATA sd" +
							   " WHERE a.cityobject_id = ?" +
							   " AND a2sd.appearance_id = a.id" +
							   " AND sd.id = a2sd.surface_data_id";
			}
			else {
				throw new Exception("Unsupported table \"" + table + "\" for CityGML type " + cityGMLClassForBalloonHandler.toString());
			}

			return sqlStatement; 
		}
		
		private void setTableShortIdAndOrderByColumnAllowed(String tablename, List<String> columns) throws Exception {
			if (ADDRESS_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "a";
				orderByColumnAllowed = (!columns.get(0).equals("MULTI_POINT") && !columns.get(0).equals("XAL_SOURCE"));
			}
			else if (ADDRESS_TO_BUILDING_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "a2b";
			}
			else if (APPEAR_TO_SURFACE_DATA_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "a2sd";
			}
			else if (APPEARANCE_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "a";
			}
			else if (BUILDING_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "b";
				orderByColumnAllowed = (!columns.get(0).equals("LOD1_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD2_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD3_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD4_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD2_MULTI_CURVE") &&
										!columns.get(0).equals("LOD3_MULTI_CURVE") &&
										!columns.get(0).equals("LOD4_MULTI_CURVE"));
			}
			else if (BUILDING_INSTALLATION_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "bi";
			}
			else if (CITY_FURNITURE_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "cf";
				orderByColumnAllowed = (!columns.get(0).equals("LOD1_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD2_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD3_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD4_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD1_IMPLICIT_REF_POINT") &&
										!columns.get(0).equals("LOD2_IMPLICIT_REF_POINT") &&
										!columns.get(0).equals("LOD3_IMPLICIT_REF_POINT") &&
										!columns.get(0).equals("LOD4_IMPLICIT_REF_POINT"));
			}
			else if (CITYMODEL_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "cm";
				orderByColumnAllowed = (!columns.get(0).equals("ENVELOPE"));
			}
			else if (CITYOBJECT_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "co";
				orderByColumnAllowed = (!columns.get(0).equals("ENVELOPE") && !columns.get(0).equals("XML_SOURCE"));
			}
			else if (CITYOBJECT_GENERICATTRIB_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "coga";
				orderByColumnAllowed = (!columns.get(0).equals("GEOMVAL") && !columns.get(0).equals("BLOBVAL"));
			}
			else if (CITYOBJECTGROUP_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "cog";
				orderByColumnAllowed = (!columns.get(0).equals("GEOMETRY"));
			}
			else if (CITYOBJECT_MEMBER_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "com";
			}
			else if (COLLECT_GEOM_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "cg";
			}
			else if (DATABASE_SRS_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "dbsrs";
			}
			else if (EXTERNAL_REFERENCE_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "er";
			}
			else if (GENERALIZATION_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "g";
			}
			else if (GENERIC_CITYOBJECT_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "gco";
				orderByColumnAllowed = (!columns.get(0).equals("LOD1_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD2_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD3_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD4_TERRAIN_INTERSECTION") &&
										!columns.get(0).equals("LOD1_IMPLICIT_REF_POINT") &&
										!columns.get(0).equals("LOD2_IMPLICIT_REF_POINT") &&
										!columns.get(0).equals("LOD3_IMPLICIT_REF_POINT") &&
										!columns.get(0).equals("LOD4_IMPLICIT_REF_POINT"));
			}
			else if (GROUP_TO_CITYOBJECT_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "g2co";
			}
			else if (LAND_USE_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "lu";
			}
			else if (OBJECTCLASS_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "oc";
			}
			else if (OPENING_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "o";
			}
			else if (OPENING_TO_THEM_SURFACE_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "o2ts";
			}
			else if (PLANT_COVER_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "pc";
			}
			else if (RASTER_RELIEF_IMP_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "rri";
				orderByColumnAllowed = (!columns.get(0).equals("RASTERPROPERTY") && !columns.get(0).equals("FOOTPRINT"));
			}
			else if (RELIEF_COMPONENT_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "rc";
				orderByColumnAllowed = (!columns.get(0).equals("EXTENT"));
			}
			else if (RELIEF_FEATURE_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "rf";
			}
			else if (ROOM_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "r";
			}
			else if (SOLITARY_VEGETAT_OBJECT_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "svo";
				orderByColumnAllowed = (!columns.get(0).equals("LOD1_IMPLICIT_REF_POINT") &&
										!columns.get(0).equals("LOD2_IMPLICIT_REF_POINT") &&
										!columns.get(0).equals("LOD3_IMPLICIT_REF_POINT") &&
										!columns.get(0).equals("LOD4_IMPLICIT_REF_POINT"));
			}
			else if (SURFACE_DATA_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "sd";
				orderByColumnAllowed = (!columns.get(0).equals("TEX_IMAGE") && !columns.get(0).equals("GT_REFERENCE_POINT"));
			}
			else if (SURFACE_GEOMETRY_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "sg";
				orderByColumnAllowed = (!columns.get(0).equals("GEOMETRY"));
			}
			else if (TEXTUREPARAM_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "tp";
			}
			else if (THEMATIC_SURFACE_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "ts";
			}
/*
			else if (TIN_RELIEF_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "tr";
				orderByColumnAllowed = (!columns.get(0).equals("STOP_LINES") &&
										!columns.get(0).equals("BREAK_LINES") &&
										!columns.get(0).equals("CONTROL_POINTS"));
			}
*/
			else if (TRAFFIC_AREA_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "ta";
			}
			else if (TRANSPORTATION_COMPLEX_TABLE.equalsIgnoreCase(tablename)) {
				orderByColumnAllowed = !columns.get(0).equals("LOD0_NETWORK");
				tableShortId = "tc";
			}
			else if (WATERBODY_TO_WATERBOUNDARY_SURFACE_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "wb2wbs";
			}
			else if (WATERBODY_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "wb";
				orderByColumnAllowed = (!columns.get(0).equals("LOD0_MULTI_CURVE") &&
										!columns.get(0).equals("LOD1_MULTI_CURVE"));
			}
			else if (WATERBOUNDARY_SURFACE_TABLE.equalsIgnoreCase(tablename)) {
				tableShortId = "wbs";
			}
			else {
				throw new Exception("Unsupported table \"" + tablename + "\" in statement \"" + rawStatement + "\"");
			}
		}

		private String getColumnsClause(String tablename,
										List<String> statementColumns) throws Exception {
			String columnsClause = "";
			Set<String> tableColumns = _3DCITYDB_TABLES_AND_COLUMNS.get(tablename);
			if (!tableColumns.containsAll(statementColumns)) {
				for (String column: statementColumns) {
					if (!tableColumns.contains(column)) {
						throw new Exception("Unsupported column \"" + column + "\" in statement \"" + rawStatement + "\"");
					}
				}
			}
			else {
//				columnsClause = "(";
				ListIterator<String> statementColumnIterator = statementColumns.listIterator();
				while (statementColumnIterator.hasNext()) {
					setTableShortIdAndOrderByColumnAllowed(tablename, statementColumns);
					columnsClause = columnsClause + tableShortId + "." + statementColumnIterator.next();
					if (statementColumnIterator.hasNext()) {
						columnsClause = columnsClause + ", ";
					}
				}
//				columnsClause = columnsClause + ")";
			}
			return columnsClause;
		}

		private String checkForSpecialKeywords(String keyword) throws Exception {
			String query = null;
			if (CENTROID_WGS84.equalsIgnoreCase(keyword)) {
				query = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().is3D() ?
						Queries.GET_CENTROID_IN_WGS84_3D_FROM_ID:
						Queries.GET_CENTROID_IN_WGS84_FROM_ID;
			}
			else if (CENTROID_WGS84_LAT.equalsIgnoreCase(keyword)) {
				query = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().is3D() ?
						Queries.GET_CENTROID_LAT_IN_WGS84_3D_FROM_ID:
						Queries.GET_CENTROID_LAT_IN_WGS84_FROM_ID;
			}
			else if (CENTROID_WGS84_LON.equalsIgnoreCase(keyword)) {
				query = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().is3D() ?
						Queries.GET_CENTROID_LON_IN_WGS84_3D_FROM_ID:
						Queries.GET_CENTROID_LON_IN_WGS84_FROM_ID;
			}
			else if (BBOX_WGS84_LAT_MIN.equalsIgnoreCase(keyword)) {
				query = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().is3D() ?
						Queries.GET_ENVELOPE_LAT_MIN_IN_WGS84_3D_FROM_ID:
						Queries.GET_ENVELOPE_LAT_MIN_IN_WGS84_FROM_ID;
			}
			else if (BBOX_WGS84_LAT_MAX.equalsIgnoreCase(keyword)) {
				query = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().is3D() ?
						Queries.GET_ENVELOPE_LAT_MAX_IN_WGS84_3D_FROM_ID:
						Queries.GET_ENVELOPE_LAT_MAX_IN_WGS84_FROM_ID;
			}
			else if (BBOX_WGS84_LON_MIN.equalsIgnoreCase(keyword)) {
				query = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().is3D() ?
						Queries.GET_ENVELOPE_LON_MIN_IN_WGS84_3D_FROM_ID:
						Queries.GET_ENVELOPE_LON_MIN_IN_WGS84_FROM_ID;
			}
			else if (BBOX_WGS84_LON_MAX.equalsIgnoreCase(keyword)) {
				query = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().is3D() ?
						Queries.GET_ENVELOPE_LON_MAX_IN_WGS84_3D_FROM_ID:
						Queries.GET_ENVELOPE_LON_MAX_IN_WGS84_FROM_ID;
			}
			else if (BBOX_WGS84_HEIGHT_MIN.equalsIgnoreCase(keyword)) {
				query = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().is3D() ?
						Queries.GET_ENVELOPE_HEIGHT_MIN_IN_WGS84_3D_FROM_ID:
						Queries.GET_ENVELOPE_HEIGHT_MIN_IN_WGS84_FROM_ID;
			}
			else if (BBOX_WGS84_HEIGHT_MAX.equalsIgnoreCase(keyword)) {
				query = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().is3D() ?
						Queries.GET_ENVELOPE_HEIGHT_MAX_IN_WGS84_3D_FROM_ID:
						Queries.GET_ENVELOPE_HEIGHT_MAX_IN_WGS84_FROM_ID;
			}
			else if (BBOX_WGS84_LAT_LON.equalsIgnoreCase(keyword)) {
				query = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().is3D() ?
						Queries.GET_ENVELOPE_LAT_MIN_IN_WGS84_3D_FROM_ID + " UNION ALL " +
						Queries.GET_ENVELOPE_LON_MIN_IN_WGS84_3D_FROM_ID + " UNION ALL " +
						Queries.GET_ENVELOPE_LAT_MAX_IN_WGS84_3D_FROM_ID + " UNION ALL " +
						Queries.GET_ENVELOPE_LON_MAX_IN_WGS84_3D_FROM_ID :
						Queries.GET_ENVELOPE_LAT_MIN_IN_WGS84_FROM_ID + " UNION ALL " +
						Queries.GET_ENVELOPE_LON_MIN_IN_WGS84_FROM_ID + " UNION ALL " +
						Queries.GET_ENVELOPE_LAT_MAX_IN_WGS84_FROM_ID + " UNION ALL " +
						Queries.GET_ENVELOPE_LON_MAX_IN_WGS84_FROM_ID;
			}
			else if (BBOX_WGS84_LON_LAT.equalsIgnoreCase(keyword)) {
				query = DatabaseConnectionPool.getInstance().getActiveConnectionMetaData().getReferenceSystem().is3D() ?
						Queries.GET_ENVELOPE_LON_MIN_IN_WGS84_3D_FROM_ID + " UNION ALL " +
						Queries.GET_ENVELOPE_LAT_MIN_IN_WGS84_3D_FROM_ID + " UNION ALL " +
						Queries.GET_ENVELOPE_LON_MAX_IN_WGS84_3D_FROM_ID + " UNION ALL " +
						Queries.GET_ENVELOPE_LAT_MAX_IN_WGS84_3D_FROM_ID :
						Queries.GET_ENVELOPE_LON_MIN_IN_WGS84_FROM_ID + " UNION ALL " +
						Queries.GET_ENVELOPE_LAT_MIN_IN_WGS84_FROM_ID + " UNION ALL " +
						Queries.GET_ENVELOPE_LON_MAX_IN_WGS84_FROM_ID + " UNION ALL " +
						Queries.GET_ENVELOPE_LAT_MAX_IN_WGS84_FROM_ID;
			}
			else {
				throw new Exception("Unsupported keyword \"" + keyword + "\" in statement \"" + rawStatement + "\"");
			}

			return query;
		}

	}
	
}
