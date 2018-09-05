package org.citydb.database.schema.mapping;

import java.util.HashMap;

import org.citygml4j.model.module.citygml.CityGMLVersion;

public class MappingConstants {
	public static final String CITYDB_SCHEMA_NAMESPACE_URI = "http://www.3dcitydb.org/database/schema/1.0";
	
	public static final String CITYDB_ADE_NAMESPACE_URI = "http://www.3dcitydb.org/citygml-ade/3.0";
	public static final String CITYDB_ADE_NAMESPACE_PREFIX = "citydb";
	public static final HashMap<CityGMLVersion, String> CITYDB_ADE_SCHEMA_LOCATIONS = new HashMap<>(2, 1f);
	
	static {
		CITYDB_ADE_SCHEMA_LOCATIONS.put(CityGMLVersion.v2_0_0, "https://www.3dcitydb.org/3dcitydb/citygml-ade/3.0/3dcitydb-ade-citygml-2.0.xsd");
		CITYDB_ADE_SCHEMA_LOCATIONS.put(CityGMLVersion.v1_0_0, "https://www.3dcitydb.org/3dcitydb/citygml-ade/3.0/3dcitydb-ade-citygml-1.0.xsd");
	}
	
	public static final String TARGET_TABLE_TOKEN = "${target.table}";
	public static final String TARGET_OBJECTCLASS_ID_TOKEN = "${target.objectclass_id}";
	public static final String TARGET_ID_TOKEN = "${target.id}";
	
	public static final String ID = "id";
	public static final String ROOT_ID = "root_id";
	public static final String GMLID = "gmlid";
	public static final String ENVELOPE = "envelope";
	public static final String IS_XLINK = "is_xlink";
	public static final String OBJECTCLASS_ID = "objectclass_id";
	public static final String SURFACE_GEOMETRY = "surface_geometry";
	public static final String CITYOBJECT = "cityobject";
	public static final String GEOMETRY = "geometry";
	public static final String ADE_DEFAULT_XML_PREFIX = "ade";
	
	public static final String IMPLICIT_GEOMETRY_PATH = "ImplicitGeometry";
	public static final String IMPLICIT_GEOMETRY_TABLE = "implicit_geometry";
	public static final int IMPLICIT_GEOMETRY_OBJECTCLASS_ID = 59;
	public static final int SURFACE_GEOMETRY_OBJECTCLASS_ID = 0;
	public static final int APPEARANCE_OBJECTCLASS_ID = 50;
}
