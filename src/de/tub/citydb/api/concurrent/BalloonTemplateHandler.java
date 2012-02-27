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
package de.tub.citydb.api.concurrent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public interface BalloonTemplateHandler {

	public String getBalloonContent(String gmlId, int lod) throws Exception;	

	// Constants
	public static final String START_TAG = "<3DCityDB>";
	public static final String END_TAG = "</3DCityDB>";
	public static final String FOREACH_TAG = "FOREACH";
	public static final String END_FOREACH_TAG = "END FOREACH";
	
	public static final String ADDRESS_TABLE = "ADDRESS";
	public static final Set<String> ADDRESS_COLUMNS = new HashSet<String>() {{
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

	public static final String ADDRESS_TO_BUILDING_TABLE = "ADDRESS_TO_BUILDING";
	public static final Set<String> ADDRESS_TO_BUILDING_COLUMNS = new HashSet<String>() {{
		add("BUILDING_ID");
		add("ADDRESS_ID");
	}};

	public static final String APPEAR_TO_SURFACE_DATA_TABLE = "APPEAR_TO_SURFACE_DATA";
	public static final Set<String> APPEAR_TO_SURFACE_DATA_COLUMNS = new HashSet<String>() {{
		add("SURFACE_DATA_ID");
		add("APPEARANCE_ID");
	}};

	public static final String APPEARANCE_TABLE = "APPEARANCE";
	public static final Set<String> APPEARANCE_COLUMNS = new HashSet<String>() {{
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

	public static final String BUILDING_TABLE = "BUILDING";
	public static final Set<String> BUILDING_COLUMNS = new HashSet<String>() {{
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

	public static final String BUILDING_INSTALLATION_TABLE = "BUILDING_INSTALLATION";
	public static final Set<String> BUILDING_INSTALLATION_COLUMNS = new HashSet<String>() {{
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

	public static final String CITYMODEL_TABLE = "CITYMODEL";
	public static final Set<String> CITYMODEL_COLUMNS = new HashSet<String>() {{
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

	public static final String CITYOBJECT_TABLE = "CITYOBJECT";
	public static final Set<String> CITYOBJECT_COLUMNS = new HashSet<String>() {{
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

	public static final String CITYOBJECT_GENERICATTRIB_TABLE = "CITYOBJECT_GENERICATTRIB";
	public static final Set<String> CITYOBJECT_GENERICATTRIB_COLUMNS = new HashSet<String>() {{
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

	public static final String CITYOBJECTGROUP_TABLE = "CITYOBJECTGROUP";
	public static final Set<String> CITYOBJECTGROUP_COLUMNS = new HashSet<String>() {{
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

	public static final String CITYOBJECT_MEMBER_TABLE = "CITYOBJECT_MEMBER";
	public static final Set<String> CITYOBJECT_MEMBER_COLUMNS = new HashSet<String>() {{
		add("CITYMODEL_ID");
		add("CITYOBJECT_ID");
	}};

	public static final String COLLECT_GEOM_TABLE = "COLLECT_GEOM";
	public static final Set<String> COLLECT_GEOM_COLUMNS = new HashSet<String>() {{
		add("BUILDING_ID");
		add("GEOMETRY_ID");
		add("CITYOBJECT_ID");
	}};

	public static final String EXTERNAL_REFERENCE_TABLE = "EXTERNAL_REFERENCE";
	public static final Set<String> EXTERNAL_REFERENCE_COLUMNS = new HashSet<String>() {{
		add("ID");
		add("INFOSYS");
		add("NAME");
		add("URI");
		add("CITYOBJECT_ID");
	}};

	public static final String GENERALIZATION_TABLE = "GENERALIZATION";
	public static final Set<String> GENERALIZATION_COLUMNS = new HashSet<String>() {{
		add("CITYOBJECT_ID");
		add("GENERALIZES_TO_ID");
	}};

	public static final String GROUP_TO_CITYOBJECT_TABLE = "GROUP_TO_CITYOBJECT";
	public static final Set<String> GROUP_TO_CITYOBJECT_COLUMNS = new HashSet<String>() {{
		add("CITYOBJECT_ID");
		add("CITYOBJECTGROUP_ID");
		add("ROLE");
	}};

	public static final String OBJECTCLASS_TABLE = "OBJECTCLASS";
	public static final Set<String> OBJECTCLASS_COLUMNS = new HashSet<String>() {{
		add("ID");
		add("CLASSNAME");
		add("SUPERCLASS_ID");
	}};

	public static final String OPENING_TABLE = "OPENING";
	public static final Set<String> OPENING_COLUMNS = new HashSet<String>() {{
		add("ID");
		add("NAME");
		add("NAME_CODESPACE");
		add("DESCRIPTION");
		add("TYPE");
		add("ADDRESS_ID");
		add("LOD3_MULTI_SURFACE_ID");
		add("LOD4_MULTI_SURFACE_ID");
	}};

	public static final String OPENING_TO_THEM_SURFACE_TABLE = "OPENING_TO_THEM_SURFACE";
	public static final Set<String> OPENING_TO_THEM_SURFACE_COLUMNS = new HashSet<String>() {{
		add("OPENING_ID");
		add("THEMATIC_SURFACE_ID");
	}};

	public static final String ROOM_TABLE = "ROOM";
	public static final Set<String> ROOM_COLUMNS = new HashSet<String>() {{
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

	public static final String SURFACE_DATA_TABLE = "SURFACE_DATA";
	public static final Set<String> SURFACE_DATA_COLUMNS = new HashSet<String>() {{
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

	public static final String SURFACE_GEOMETRY_TABLE = "SURFACE_GEOMETRY";
	public static final Set<String> SURFACE_GEOMETRY_COLUMNS = new HashSet<String>() {{
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

	public static final String TEXTUREPARAM_TABLE = "TEXTUREPARAM";
	public static final Set<String> TEXTUREPARAM_COLUMNS = new HashSet<String>() {{
		add("SURFACE_GEOMETRY_ID");
		add("IS_TEXTURE_PARAMETRIZATION");
		add("WORLD_TO_TEXTURE");
		add("TEXTURE_COORDINATES");
		add("SURFACE_DATA_ID");
	}};

	public static final String THEMATIC_SURFACE_TABLE = "THEMATIC_SURFACE";
	public static final Set<String> THEMATIC_SURFACE_COLUMNS = new HashSet<String>() {{
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

	
	public static final String MAX = "MAX";
	public static final String MIN = "MIN";
	public static final String AVG = "AVG";
	public static final String COUNT = "COUNT";
	public static final String SUM = "SUM";
	public static final String FIRST = "FIRST";
	public static final String LAST = "LAST";

	public static final Set<String> AGGREGATION_FUNCTIONS = new HashSet<String>() {{
		add(MAX);
		add(MIN);
		add(AVG);
		add(COUNT);
		add(SUM);
		add(FIRST);
		add(LAST);
	}};

	public HashMap<String, Set<String>> _3DCITYDB_TABLES_AND_COLUMNS = new HashMap<String, Set<String>>() {{
		put(ADDRESS_TABLE, ADDRESS_COLUMNS);
		put(ADDRESS_TO_BUILDING_TABLE, ADDRESS_TO_BUILDING_COLUMNS);
		put(APPEAR_TO_SURFACE_DATA_TABLE,APPEAR_TO_SURFACE_DATA_COLUMNS );
		put(APPEARANCE_TABLE, APPEARANCE_COLUMNS);
		put(BUILDING_TABLE, BUILDING_COLUMNS);
		put(BUILDING_INSTALLATION_TABLE,BUILDING_INSTALLATION_COLUMNS );
		put(CITYMODEL_TABLE,CITYMODEL_COLUMNS );
		put(CITYOBJECT_TABLE, CITYOBJECT_COLUMNS);
		put(CITYOBJECT_GENERICATTRIB_TABLE,CITYOBJECT_GENERICATTRIB_COLUMNS );
		put(CITYOBJECTGROUP_TABLE, CITYOBJECTGROUP_COLUMNS);
		put(CITYOBJECT_MEMBER_TABLE,CITYOBJECT_MEMBER_COLUMNS );
		put(COLLECT_GEOM_TABLE, COLLECT_GEOM_COLUMNS);
		put(EXTERNAL_REFERENCE_TABLE,EXTERNAL_REFERENCE_COLUMNS );
		put(GENERALIZATION_TABLE,GENERALIZATION_COLUMNS );
		put(GROUP_TO_CITYOBJECT_TABLE, GROUP_TO_CITYOBJECT_COLUMNS);
		put(OBJECTCLASS_TABLE, OBJECTCLASS_COLUMNS);
		put(OPENING_TABLE, OPENING_COLUMNS);
		put(OPENING_TO_THEM_SURFACE_TABLE, OPENING_TO_THEM_SURFACE_COLUMNS);
		put(ROOM_TABLE, ROOM_COLUMNS);
		put(SURFACE_DATA_TABLE,SURFACE_DATA_COLUMNS );
		put(SURFACE_GEOMETRY_TABLE, SURFACE_GEOMETRY_COLUMNS);
		put(TEXTUREPARAM_TABLE,TEXTUREPARAM_COLUMNS );
		put(THEMATIC_SURFACE_TABLE,THEMATIC_SURFACE_COLUMNS );
	}};

}
