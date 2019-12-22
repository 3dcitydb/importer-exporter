/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2019
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

import org.citydb.config.project.kmlExporter.DisplayForm;
import org.citydb.config.project.kmlExporter.Lod0FootprintMode;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.SequenceEnum;
import org.citydb.log.Logger;
import org.citydb.modules.kml.ade.ADEKmlExportException;
import org.citydb.modules.kml.ade.ADEKmlExportManager;
import org.citydb.modules.kml.ade.ADEKmlExporter;
import org.citydb.util.Util;
import org.citygml4j.model.citygml.CityGMLClass;
import org.citygml4j.model.gml.base.AbstractGML;

public class Queries {
	private AbstractDatabaseAdapter databaseAdapter;
	private String schema;
	private String implicitGeometryNullColumns;
	private KmlExporterManager exportManager;

	private static int QUERY_POINT_AND_CURVE_GEOMETRY = 0;
	private static int QUERY_SURFACE_GEOMETRY = 1;
	private static int QUERY_SURFACE_GEOMETRY_REFERENCE_IDS = 2;

	public Queries(AbstractDatabaseAdapter databaseAdapter, String schema) {
		this(databaseAdapter, schema, null);
	}

	public Queries(AbstractDatabaseAdapter databaseAdapter, String schema, KmlExporterManager exporterManager) {
		this.exportManager = exporterManager;
		this.databaseAdapter = databaseAdapter;
		this.schema = schema;

		switch (databaseAdapter.getDatabaseType()) {
			case ORACLE:
				implicitGeometryNullColumns = "null as implicit_id, null as implicit_ref_point, null as implicit_transformation ";
				break;
			case POSTGIS:
				implicitGeometryNullColumns = "null::integer as implicit_id, null::geometry as implicit_ref_point, null::text as implicit_transformation ";
				break;
			default:
				implicitGeometryNullColumns = "";
		}
	}

	public String getImplicitGeometryNullColumns() {
		return implicitGeometryNullColumns;
	}

	// ----------------------------------------------------------------------
	// 	SURFACE GEOMETRIES AND APPEARANCE QUERIES
	// ----------------------------------------------------------------------

	public String getSurfaceGeometries(boolean exportAppearance, boolean isImplicit) {
		StringBuilder query = new StringBuilder()
		.append("select ")
		.append(isImplicit ? "sg.implicit_geometry" : "sg.geometry")
		.append(", sg.id, sg.parent_id, sg.root_id, sg.gmlid, sg.is_xlink ");

		if (exportAppearance) {
			query.append(", sd.x3d_shininess, sd.x3d_transparency, sd.x3d_ambient_intensity, ")
			.append("sd.x3d_specular_color, sd.x3d_diffuse_color, sd.x3d_emissive_color, sd.x3d_is_smooth, ")
			.append("sd.tex_image_id, ti.tex_image_uri, tp.texture_coordinates, coalesce(a.theme, '<unknown>') theme ");
		}

		query.append("FROM ").append(schema).append(".surface_geometry sg ");

		if (exportAppearance) {
			query.append("LEFT JOIN ").append(schema).append(".textureparam tp ON tp.surface_geometry_id = sg.id ")
			.append("LEFT JOIN ").append(schema).append(".surface_data sd ON sd.id = tp.surface_data_id ")
			.append("LEFT JOIN ").append(schema).append(".tex_image ti ON ti.id = sd.tex_image_id ")
			.append("LEFT JOIN ").append(schema).append(".appear_to_surface_data a2sd ON a2sd.surface_data_id = sd.id ")
			.append("LEFT JOIN ").append(schema).append(".appearance a ON a2sd.appearance_id = a.id ");
		}

		query.append("WHERE sg.root_id = ? ")
		.append("ORDER BY sg.id");

		return query.toString();
	}

	// ----------------------------------------------------------------------
	// 	GENERIC PURPOSE QUERIES
	// ----------------------------------------------------------------------

	public String getIds() {
		StringBuilder query = new StringBuilder()
		.append("SELECT co.id, co.gmlid, co.objectclass_id, co.envelope FROM ").append(schema).append(".CITYOBJECT co WHERE ");

		switch (databaseAdapter.getDatabaseType()) {
		case ORACLE:
			query.append("SDO_ANYINTERACT(co.envelope, ?) = 'TRUE'");
			break;
		case POSTGIS:
			query.append("co.envelope && ?");
			break;
		}		

		return query.toString();
	}

	public String getExtrusionHeight() {
		switch (databaseAdapter.getDatabaseType()) {
		case ORACLE:
			return new StringBuilder("SELECT ")
			.append("SDO_GEOM.SDO_MAX_MBR_ORDINATE(co.envelope, 3) - SDO_GEOM.SDO_MIN_MBR_ORDINATE(co.envelope, 3) AS envelope_measured_height ")
			.append("FROM ").append(schema).append(".CITYOBJECT co ")
			.append("WHERE co.id = ?").toString();
		case POSTGIS:
			return new StringBuilder("SELECT ")
			.append("ST_ZMax(Box3D(co.envelope)) - ST_ZMin(Box3D(co.envelope)) AS envelope_measured_height ")
			.append("FROM ").append(schema).append(".CITYOBJECT co ")
			.append("WHERE co.id = ?").toString();
		default:
			return null;
		}
	}

	public String getStringAttributeById() {
		return new StringBuilder("SELECT coga.strval ")
		.append("FROM ").append(schema).append(".CITYOBJECT_GENERICATTRIB coga ")
		.append("WHERE coga.cityobject_id = ? AND coga.attrname = ? ").toString();
	}

	public String getIdByGmlId() {
		return new StringBuilder("SELECT id FROM ").append(schema).append(".CITYOBJECT WHERE gmlid = ?").toString();
	}

	public String getIdAndObjectClassByGmlId() {
		return new StringBuilder("SELECT id, objectclass_id FROM ").append(schema).append(".CITYOBJECT WHERE gmlid = ?").toString();
	}

	public String getGmlIdAndObjectClassById() {
		return new StringBuilder("SELECT gmlid, objectclass_id FROM ").append(schema).append(".CITYOBJECT WHERE id = ?").toString();
	}

	public String insertGEOffset() {
		return new StringBuilder("INSERT INTO ").append(schema).append(".CITYOBJECT_GENERICATTRIB (ID, ATTRNAME, DATATYPE, STRVAL, CITYOBJECT_ID) ")
				.append("VALUES (").append(databaseAdapter.getSQLAdapter().getNextSequenceValue(SequenceEnum.CITYOBJECT_GENERICATTRIB_ID_SEQ.getName())).append(", ?, 1, ?, ?)").toString();
	}
	
	public String transformGeometryToDBSrs() {
		StringBuilder query = new StringBuilder("SELECT ").append(databaseAdapter.getSQLAdapter().resolveDatabaseOperationName("geom_transform")).append("(?, ?)");
		if (databaseAdapter.getSQLAdapter().requiresPseudoTableInSelect())
			query.append(" FROM ").append(databaseAdapter.getSQLAdapter().getPseudoTableName());

		return query.toString();
	}

	// ----------------------------------------------------------------------
	// 	BUILDING QUERIES
	// ----------------------------------------------------------------------

	public String getBuildingPartsFromBuilding() {
		return new StringBuilder("SELECT id FROM ").append(schema).append(".BUILDING WHERE building_root_id = ?").toString();
	}

	private String getBuildingPartFootprint(int lod, Lod0FootprintMode lod0FootprintMode) {
		StringBuilder query = new StringBuilder();

		if (lod == 0) {
			if (lod0FootprintMode == Lod0FootprintMode.FOOTPRINT) {
				query.append("SELECT sg.geometry FROM ").append(schema).append(".surface_geometry sg, ")
				.append(schema).append(".building b ")
				.append("WHERE b.lod0_footprint_id = sg.root_id ")
				.append("AND sg.geometry IS NOT NULL AND b.id = ?");
			}

			else if (lod0FootprintMode == Lod0FootprintMode.ROOFPRINT || lod0FootprintMode == Lod0FootprintMode.ROOFPRINT_PRIOR_FOOTPRINT) {
				query.append("SELECT sg.geometry FROM ").append(schema).append(".surface_geometry sg, ")
				.append(schema).append(".building b ")
				.append("WHERE b.lod0_roofprint_id = sg.root_id ")
				.append("AND sg.geometry IS NOT NULL AND b.id = ?");
			}
		} 

		else if (lod == 1) {
			switch (databaseAdapter.getDatabaseType()) {
			case ORACLE:
				return getBuildingPartAggregateGeometriesForLOD1().replace("<TOLERANCE>", "0.001")
						.replace("<2D_SRID>", "(SELECT SRID FROM " + schema + ".DATABASE_SRS)")
						.replace("<LoD>", "1")
						.replace("<GROUP_BY_1>", "256")
						.replace("<GROUP_BY_2>", "64")
						.replace("<GROUP_BY_3>", "16");
			case POSTGIS:		
				return getBuildingPartAggregateGeometriesForLOD1().replace("<TOLERANCE>", "0.001")
						.replace("<LoD>", "1");			
			default:
				return null;
			}
		}

		else {
			query.append("SELECT sg.geometry FROM ").append(schema).append(".surface_geometry sg, ")
			.append(schema).append(".thematic_surface ts ")
			.append("WHERE ts.building_id = ? AND ts.objectclass_id = 35 ")
			.append("AND sg.root_id = ts.lod").append(lod).append("_multi_surface_id AND sg.geometry IS NOT NULL ");
		}

		return query.toString();
	}

	private String getBuildingPartGeometry(int lod, Lod0FootprintMode lod0FootprintMode, boolean lodCheckOnly) {
		StringBuilder query = new StringBuilder().append("SELECT sub.* FROM (");

		if (lod > 1) {
			// exterior thematic surfaces
			query.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".thematic_surface ts ")
			.append("WHERE ts.building_id = ? ")
			.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
			.append(") tmp) ");

			if (!lodCheckOnly) {
				// thematic surfaces of exterior building installations
				query.append("UNION ALL ")
				.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
				.append(implicitGeometryNullColumns)
				.append("FROM ").append(schema).append(".thematic_surface ts ")
				.append("JOIN ").append(schema).append(".building_installation bi ON ts.building_installation_id = bi.id ")
				.append("WHERE bi.building_id = ? ")
				.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
				.append(") tmp) ")
				// exterior building installations
				.append("UNION ALL ")
				.append("(SELECT tmp.* FROM (SELECT bi.lod").append(lod).append("_brep_id, bi.objectclass_id, ")
				.append("ig.relative_brep_id, bi.lod").append(lod).append("_implicit_ref_point, ").append("bi.lod").append(lod).append("_implicit_transformation ")
				.append("FROM ").append(schema).append(".building_installation bi ")
				.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = bi.lod").append(lod).append("_implicit_rep_id ")
				.append("WHERE bi.building_id = ? ")
				.append("AND (bi.lod").append(lod).append("_brep_id is not null ")
				.append("OR ig.relative_brep_id is not null) ")
				.append(") tmp) ");
			}
		}

		if (lod > 2 && !lodCheckOnly) {
			// openings in exterior thematic surfaces
			query.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".opening o ")
			.append("JOIN ").append(schema).append(".opening_to_them_surface o2ts ON o2ts.opening_id = o.id ")
			.append("JOIN ").append(schema).append(".thematic_surface ts ON ts.id = o2ts.thematic_surface_id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE ts.building_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// openings in exterior thematic surfaces of building installations
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".opening o ")
			.append("JOIN ").append(schema).append(".opening_to_them_surface o2ts ON o2ts.opening_id = o.id ")
			.append("JOIN ").append(schema).append(".thematic_surface ts ON ts.id = o2ts.thematic_surface_id ")
			.append("JOIN ").append(schema).append(".building_installation bi ON ts.building_installation_id = bi.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE bi.building_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ");
		}

		if (lod > 3 && !lodCheckOnly) {
			// interior thematic surfaces of building installations
			query.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".thematic_surface ts ")
			.append("JOIN ").append(schema).append(".building_installation bi ON ts.building_installation_id = bi.id ")
			.append("JOIN ").append(schema).append(".room r ON bi.room_id = r.id ")
			.append("WHERE r.building_id = ? ")
			.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
			.append(") tmp) ")
			// interior thematic surfaces of rooms
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".thematic_surface ts ")
			.append("JOIN ").append(schema).append(".room r ON ts.room_id = r.id ")
			.append("WHERE r.building_id = ? ")
			.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
			.append(") tmp) ")
			// openings of interior thematic surfaces of building installations
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".opening o ")
			.append("JOIN ").append(schema).append(".opening_to_them_surface o2ts ON o2ts.opening_id = o.id ")
			.append("JOIN ").append(schema).append(".thematic_surface ts ON ts.id = o2ts.thematic_surface_id ")
			.append("JOIN ").append(schema).append(".building_installation bi ON ts.building_installation_id = bi.id ")
			.append("JOIN ").append(schema).append(".room r ON bi.room_id = r.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE r.building_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// openings of interior thematic surfaces of rooms
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".opening o ")
			.append("JOIN ").append(schema).append(".opening_to_them_surface o2ts ON o2ts.opening_id = o.id ")
			.append("JOIN ").append(schema).append(".thematic_surface ts ON ts.id = o2ts.thematic_surface_id ")
			.append("JOIN ").append(schema).append(".room r ON ts.room_id = r.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE r.building_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// building furniture
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT bf.lod4_brep_id, bf.objectclass_id, ")
			.append("ig.relative_brep_id, bf.lod4_implicit_ref_point, ").append("bf.lod4_implicit_transformation ")
			.append("FROM ").append(schema).append(".building_furniture bf ")
			.append("JOIN ").append(schema).append(".room r ON bf.room_id = r.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = bf.lod4_implicit_rep_id ")
			.append("WHERE r.building_id = ? ")
			.append("AND (bf.lod4_brep_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// rooms
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT r.lod4_solid_id, r.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".room r ")
			.append("WHERE r.building_id = ? ")
			.append("AND r.lod4_solid_id is not null ")
			.append(") tmp) ")
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT r.lod4_multi_surface_id, r.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".room r ")
			.append("WHERE r.building_id = ? ")
			.append("AND r.lod4_multi_surface_id is not null ")
			.append(") tmp) ")
			// interior building installations
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT bi.lod").append(lod).append("_brep_id, bi.objectclass_id, ")
			.append("ig.relative_brep_id, bi.lod").append(lod).append("_implicit_ref_point, ").append("bi.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".building_installation bi ")
			.append("JOIN ").append(schema).append(".room r ON bi.room_id = r.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = bi.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE r.building_id = ? ")
			.append("AND (bi.lod").append(lod).append("_brep_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ");
		}

		if (lod > 1)
			query.append("UNION ALL ");

		// building geometry
		query.append("(SELECT tmp.* FROM (SELECT b.lod").append(lod).append("_solid_id, 0 as objectclass_id, ")
		.append(implicitGeometryNullColumns)
		.append("FROM ").append(schema).append(".building b ")
		.append("WHERE b.id = ? ")
		.append("AND b.lod").append(lod).append("_solid_id is not null ")
		.append(") tmp) ")
		.append("UNION ALL ")
		.append("(SELECT tmp.* FROM (SELECT b.lod").append(lod).append("_multi_surface_id, 0 as objectclass_id, ")
		.append(implicitGeometryNullColumns)
		.append("FROM ").append(schema).append(".building b ")
		.append("WHERE b.id = ? ")
		.append("AND b.lod").append(lod).append("_multi_surface_id is not null ")
		.append(") tmp) ")
		.append(") sub ");

		return query.toString();
	}

	private String getBuildingPartAggregateGeometriesForLOD2OrHigher() {
		switch (databaseAdapter.getDatabaseType()) {
		case ORACLE:
			return new StringBuilder("SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT ").append(schema).append(".citydb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT geom.gid FROM (SELECT b.lod<LoD>_multi_surface_id as gid ")
			.append("FROM ").append(schema).append(".BUILDING b ")
			.append("WHERE ")
			.append("b.id = ? ")
			.append("AND b.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT b.lod<LoD>_solid_id as gid ")
			.append("FROM ").append(schema).append(".BUILDING b ")
			.append("WHERE ")
			.append("b.id = ? ")
			.append("AND b.lod<LoD>_solid_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT ts.lod<LoD>_multi_surface_id as gid ")
			.append("FROM ").append(schema).append(".THEMATIC_SURFACE ts ")
			.append("WHERE ")
			.append("ts.building_id = ? ")
			.append("AND ts.lod<LoD>_multi_surface_id IS NOT NULL) geom ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL")
			.append(") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'")
			.append(") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>")
			.append(") ")
			.append("GROUP BY mod(rownum, <GROUP_BY_1>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_2>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_3>) ")
			.append(")").toString();
		case POSTGIS:
			return new StringBuilder("SELECT ST_Union(get_valid_area.simple_geom) ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT ST_Force2D(sg.geometry) AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT b.lod<LoD>_multi_surface_id ")
			.append("FROM ").append(schema).append(".BUILDING b ")
			.append("WHERE b.id = ? ")
			.append("AND b.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT b.lod<LoD>_solid_id ")
			.append("FROM ").append(schema).append(".BUILDING b ")
			.append("WHERE b.id = ? ")
			.append("AND b.lod<LoD>_solid_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT ts.lod<LoD>_multi_surface_id ")
			.append("FROM ").append(schema).append(".THEMATIC_SURFACE ts ")
			.append("WHERE ts.building_id = ? ")
			.append("AND ts.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL) AS get_geoms ")
			.append("WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms ")
			// ST_Area for WGS84 only works correctly if the geometry is a geography data type
			.append("WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area").toString();
		default:
			return null;
		}
	}

	private String getBuildingPartAggregateGeometriesForLOD1() {
		switch (databaseAdapter.getDatabaseType()) {
		case ORACLE:
			return new StringBuilder("SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT ").append(schema).append(".citydb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT geom.gid FROM (SELECT b.lod<LoD>_multi_surface_id as gid ")
			.append("FROM ").append(schema).append(".BUILDING b ")
			.append("WHERE ")
			.append("b.id = ? ")
			.append("AND b.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT b.lod<LoD>_solid_id as gid ")
			.append("FROM ").append(schema).append(".BUILDING b ")
			.append("WHERE ")
			.append("b.id = ? ")
			.append("AND b.lod<LoD>_solid_id IS NOT NULL) geom ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL")
			.append(") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'")
			.append(") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>")
			.append(") ")
			.append("GROUP BY mod(rownum, <GROUP_BY_1>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_2>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_3>) ")
			.append(")").toString();
		case POSTGIS:
			return new StringBuilder("SELECT ST_Union(get_valid_area.simple_geom) ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT ST_Force2D(sg.geometry) AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT b.lod<LoD>_multi_surface_id ")
			.append("FROM ").append(schema).append(".BUILDING b ")
			.append("WHERE b.id = ? ")
			.append("AND b.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT b.lod<LoD>_solid_id ")
			.append("FROM ").append(schema).append(".BUILDING b ")
			.append("WHERE b.id = ? ")
			.append("AND b.lod<LoD>_solid_id IS NOT NULL ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL) AS get_geoms ")
			.append("WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms ")
			// ST_Area for WGS84 only works correctly if the geometry is a geography data type
			.append("WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area").toString();
		default:
			return null;
		}
	}

	private String getBuildingPartAggregateGeometriesForLOD0() {
		switch (databaseAdapter.getDatabaseType()) {
		case ORACLE:
			return new StringBuilder("SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT sg.geometry AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT geom.gid FROM (SELECT b.lod0_footprint_id as gid ")
			.append("FROM ").append(schema).append(".BUILDING b ")
			.append("WHERE ")
			.append("b.id = ? ")
			.append("AND b.lod0_footprint_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT b.lod0_roofprint_id as gid ")
			.append("FROM ").append(schema).append(".BUILDING b ")
			.append("WHERE ")
			.append("b.id = ? ")
			.append("AND b.lod0_roofprint_id IS NOT NULL) geom ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL")
			.append(") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'")
			.append(") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>")
			.append(") ")
			.append("GROUP BY mod(rownum, <GROUP_BY_1>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_2>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_3>) ")
			.append(")").toString();
		case POSTGIS:
			return new StringBuilder("SELECT ST_Union(get_valid_area.simple_geom) ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT sg.geometry AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT b.lod0_footprint_id ")
			.append("FROM ").append(schema).append(".BUILDING b ")
			.append("WHERE b.id = ? ")
			.append("AND b.lod0_footprint_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT b.lod0_roofprint_id ")
			.append("FROM ").append(schema).append(".BUILDING b ")
			.append("WHERE b.id = ? ")
			.append("AND b.lod0_roofprint_id IS NOT NULL ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL) AS get_geoms ")
			.append("WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms ")
			// ST_Area for WGS84 only works correctly if the geometry is a geography data type
			.append("WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area").toString();
		default:
			return null;
		}
	}

	public String getBuildingPartAggregateGeometries(double tolerance, int srid2D, int lodToExportFrom, double groupBy1, double groupBy2, double groupBy3, int objectClassId) {
		String query;
		if (lodToExportFrom > 1) {
			query = getBuildingPartAggregateGeometriesForLOD2OrHigher().replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<2D_SRID>", String.valueOf(srid2D))
					.replace("<LoD>", String.valueOf(lodToExportFrom))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));
		}
		else if (lodToExportFrom == 1){
			query = getBuildingPartAggregateGeometriesForLOD1().replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<2D_SRID>", String.valueOf(srid2D))
					.replace("<LoD>", String.valueOf(lodToExportFrom))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));			
		}
		else {
			query = getBuildingPartAggregateGeometriesForLOD0().replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));					
		}
		return unionADEQueries(QUERY_SURFACE_GEOMETRY, query,  lodToExportFrom, objectClassId);
	}

	public String getBuildingPartQuery(int lodToExportFrom, Lod0FootprintMode lod0FootprintMode, DisplayForm displayForm, boolean lodCheckOnly, int objectClassId) {
		String query = null;

		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
			query = getBuildingPartFootprint(lodToExportFrom, lod0FootprintMode);
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lodToExportFrom, objectClassId);
			break;
		default:
			query = getBuildingPartGeometry(lodToExportFrom, lod0FootprintMode, lodCheckOnly);
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY_REFERENCE_IDS, query, lodToExportFrom, objectClassId);
		}

		return query;
	}

	// ----------------------------------------------------------------------
	// 	Bridge QUERIES
	// ----------------------------------------------------------------------

	public String getBridgePartsFromBridge() { 
		return new StringBuilder("SELECT id FROM ").append(schema).append(".BRIDGE WHERE bridge_root_id = ?").toString();
	}

	private String getBridgePartFootprint(int lod) {
		StringBuilder query = new StringBuilder();

		if (lod == 1) {
			switch (databaseAdapter.getDatabaseType()) {
			case ORACLE:
				return getBridgePartAggregateGeometriesForLOD1().replace("<TOLERANCE>", "0.001")
						.replace("<2D_SRID>", "(SELECT SRID FROM " + schema + ".DATABASE_SRS)")
						.replace("<LoD>", "1")
						.replace("<GROUP_BY_1>", "256")
						.replace("<GROUP_BY_2>", "64")
						.replace("<GROUP_BY_3>", "16");
			case POSTGIS:		
				return getBridgePartAggregateGeometriesForLOD1().replace("<TOLERANCE>", "0.001")
						.replace("<LoD>", "1");			
			default:
				return null;
			}
		}

		else {
			query.append("SELECT sg.geometry FROM ").append(schema).append(".surface_geometry sg, ")
			.append(schema).append(".bridge_thematic_surface ts ")
			.append("WHERE ts.bridge_id = ? AND ts.objectclass_id = 73 ")
			.append("AND sg.root_id = ts.lod").append(lod).append("_multi_surface_id AND sg.geometry IS NOT NULL ");
		}

		return query.toString();
	}

	private String getBridgePartGeometry(int lod, boolean lodCheckOnly) {
		StringBuilder query = new StringBuilder().append("SELECT sub.* FROM (");

		if (lod > 1) {
			// exterior thematic surfaces
			query.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".bridge_thematic_surface ts ")
			.append("WHERE ts.bridge_id = ? ")
			.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
			.append(") tmp) ");

			if (!lodCheckOnly) {
				// thematic surfaces of bridge construction elements
				query.append("UNION ALL ")
				.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
				.append(implicitGeometryNullColumns)
				.append("FROM ").append(schema).append(".bridge_thematic_surface ts ")
				.append("JOIN ").append(schema).append(".bridge_constr_element bc ON ts.bridge_constr_element_id = bc.id ")
				.append("WHERE bc.bridge_id = ? ")
				.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
				.append(") tmp) ")				
				// thematic surfaces of exterior bridge installations
				.append("UNION ALL ")
				.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
				.append(implicitGeometryNullColumns)
				.append("FROM ").append(schema).append(".bridge_thematic_surface ts ")
				.append("JOIN ").append(schema).append(".bridge_installation bi ON ts.bridge_installation_id = bi.id ")
				.append("WHERE bi.bridge_id = ? ")
				.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
				.append(") tmp) ")
				// exterior bridge installations
				.append("UNION ALL ")
				.append("(SELECT tmp.* FROM (SELECT bi.lod").append(lod).append("_brep_id, bi.objectclass_id, ")
				.append("ig.relative_brep_id, bi.lod").append(lod).append("_implicit_ref_point, ").append("bi.lod").append(lod).append("_implicit_transformation ")
				.append("FROM ").append(schema).append(".bridge_installation bi ")
				.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = bi.lod").append(lod).append("_implicit_rep_id ")
				.append("WHERE bi.bridge_id = ? ")
				.append("AND (bi.lod").append(lod).append("_brep_id is not null ")
				.append("OR ig.relative_brep_id is not null) ")
				.append(") tmp) ");
			}
		}

		if (lod > 2 && !lodCheckOnly) {
			// openings in exterior thematic surfaces
			query.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".bridge_opening o ")
			.append("JOIN ").append(schema).append(".bridge_open_to_them_srf o2ts ON o2ts.bridge_opening_id = o.id ")
			.append("JOIN ").append(schema).append(".bridge_thematic_surface ts ON ts.id = o2ts.bridge_thematic_surface_id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE ts.bridge_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// openings in exterior thematic surfaces of bridge construction elements		
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".bridge_opening o ")
			.append("JOIN ").append(schema).append(".bridge_open_to_them_srf o2ts ON o2ts.bridge_opening_id = o.id ")
			.append("JOIN ").append(schema).append(".bridge_thematic_surface ts ON ts.id = o2ts.bridge_thematic_surface_id ")
			.append("JOIN ").append(schema).append(".bridge_constr_element bc ON ts.bridge_constr_element_id = bc.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE bc.bridge_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// openings in exterior thematic surfaces of building installations
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".bridge_opening o ")
			.append("JOIN ").append(schema).append(".bridge_open_to_them_srf o2ts ON o2ts.bridge_opening_id = o.id ")
			.append("JOIN ").append(schema).append(".bridge_thematic_surface ts ON ts.id = o2ts.bridge_thematic_surface_id ")
			.append("JOIN ").append(schema).append(".bridge_installation bi ON ts.bridge_installation_id = bi.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE bi.bridge_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ");
		}

		if (lod > 3 && !lodCheckOnly) {
			// interior thematic surfaces of bridge installations
			query.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".bridge_thematic_surface ts ")
			.append("JOIN ").append(schema).append(".bridge_installation bi ON ts.bridge_installation_id = bi.id ")
			.append("JOIN ").append(schema).append(".bridge_room r ON bi.bridge_room_id = r.id ")
			.append("WHERE r.bridge_id = ? ")
			.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
			.append(") tmp) ")
			// interior thematic surfaces of rooms
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".bridge_thematic_surface ts ")
			.append("JOIN ").append(schema).append(".bridge_room r ON ts.bridge_room_id = r.id ")
			.append("WHERE r.bridge_id = ? ")
			.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
			.append(") tmp) ")
			// openings of interior thematic surfaces of bridge installations
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".bridge_opening o ")
			.append("JOIN ").append(schema).append(".bridge_open_to_them_srf o2ts ON o2ts.bridge_opening_id = o.id ")
			.append("JOIN ").append(schema).append(".bridge_thematic_surface ts ON ts.id = o2ts.bridge_thematic_surface_id ")
			.append("JOIN ").append(schema).append(".bridge_installation bi ON ts.bridge_installation_id = bi.id ")
			.append("JOIN ").append(schema).append(".bridge_room r ON bi.bridge_room_id = r.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE r.bridge_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// openings of interior thematic surfaces of bridge rooms
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".bridge_opening o ")
			.append("JOIN ").append(schema).append(".bridge_open_to_them_srf o2ts ON o2ts.bridge_opening_id = o.id ")
			.append("JOIN ").append(schema).append(".bridge_thematic_surface ts ON ts.id = o2ts.bridge_thematic_surface_id ")
			.append("JOIN ").append(schema).append(".bridge_room r ON ts.bridge_room_id = r.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE r.bridge_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// bridge furniture
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT bf.lod4_brep_id, bf.objectclass_id, ")
			.append("ig.relative_brep_id, bf.lod4_implicit_ref_point, ").append("bf.lod4_implicit_transformation ")
			.append("FROM ").append(schema).append(".bridge_furniture bf ")
			.append("JOIN ").append(schema).append(".bridge_room r ON bf.bridge_room_id = r.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = bf.lod4_implicit_rep_id ")
			.append("WHERE r.bridge_id = ? ")
			.append("AND (bf.lod4_brep_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// bridge rooms
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT r.lod4_solid_id, r.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".bridge_room r ")
			.append("WHERE r.bridge_id = ? ")
			.append("AND r.lod4_solid_id is not null ")
			.append(") tmp) ")
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT r.lod4_multi_surface_id, r.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".bridge_room r ")
			.append("WHERE r.bridge_id = ? ")
			.append("AND r.lod4_multi_surface_id is not null ")
			.append(") tmp) ")
			// interior bridge installations
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT bi.lod").append(lod).append("_brep_id, bi.objectclass_id, ")
			.append("ig.relative_brep_id, bi.lod").append(lod).append("_implicit_ref_point, ").append("bi.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".bridge_installation bi ")
			.append("JOIN ").append(schema).append(".bridge_room r ON bi.bridge_room_id = r.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = bi.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE r.bridge_id = ? ")
			.append("AND (bi.lod").append(lod).append("_brep_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ");
		}

		if (lod > 1)
			query.append("UNION ALL ");

		// bridge geometry
		query.append("(SELECT tmp.* FROM (SELECT b.lod").append(lod).append("_solid_id, 0 as objectclass_id, ")
		.append(implicitGeometryNullColumns)
		.append("FROM ").append(schema).append(".bridge b ")
		.append("WHERE b.id = ? ")
		.append("AND b.lod").append(lod).append("_solid_id is not null ")
		.append(") tmp) ")
		.append("UNION ALL ")
		.append("(SELECT tmp.* FROM (SELECT b.lod").append(lod).append("_multi_surface_id, 0 as objectclass_id, ")
		.append(implicitGeometryNullColumns)
		.append("FROM ").append(schema).append(".bridge b ")
		.append("WHERE b.id = ? ")
		.append("AND b.lod").append(lod).append("_multi_surface_id is not null ")
		.append(") tmp) ");

		if (!lodCheckOnly) {
			// exterior bridge construction elements
			query.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT bc.lod").append(lod).append("_brep_id, bc.objectclass_id, ")
			.append("ig.relative_brep_id, bc.lod").append(lod).append("_implicit_ref_point, ").append("bc.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".bridge_constr_element bc ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = bc.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE bc.bridge_id = ? ")
			.append("AND (bc.lod").append(lod).append("_brep_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ");
		}

		query.append(") sub ");
		return query.toString();
	}

	private String getBridgePartAggregateGeometriesForLOD2OrHigher() {
		switch (databaseAdapter.getDatabaseType()) {
		case ORACLE:
			return new StringBuilder("SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT ").append(schema).append(".citydb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT geom.gid FROM (SELECT b.lod<LoD>_multi_surface_id as gid ")
			.append("FROM ").append(schema).append(".BRIDGE b ")
			.append("WHERE ")
			.append("b.id = ? ")
			.append("AND b.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT b.lod<LoD>_solid_id as gid ")
			.append("FROM ").append(schema).append(".BRIDGE b ")
			.append("WHERE ")
			.append("b.id = ? ")
			.append("AND b.lod<LoD>_solid_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT ts.lod<LoD>_multi_surface_id as gid ")
			.append("FROM ").append(schema).append(".bridge_thematic_surface ts ")
			.append("WHERE ")
			.append("ts.bridge_id = ? ")
			.append("AND ts.lod<LoD>_multi_surface_id IS NOT NULL) geom ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL")
			.append(") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'")
			.append(") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>")
			.append(") ")
			.append("GROUP BY mod(rownum, <GROUP_BY_1>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_2>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_3>) ")
			.append(")").toString();
		case POSTGIS:
			return new StringBuilder("SELECT ST_Union(get_valid_area.simple_geom) ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT ST_Force2D(sg.geometry) AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT b.lod<LoD>_multi_surface_id ")
			.append("FROM ").append(schema).append(".bridge b ")
			.append("WHERE b.id = ? ")
			.append("AND b.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT b.lod<LoD>_solid_id ")
			.append("FROM ").append(schema).append(".bridge b ")
			.append("WHERE b.id = ? ")
			.append("AND b.lod<LoD>_solid_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT ts.lod<LoD>_multi_surface_id ")
			.append("FROM ").append(schema).append(".bridge_thematic_surface ts ")
			.append("WHERE ts.bridge_id = ? ")
			.append("AND ts.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL) AS get_geoms ")
			.append("WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms ")
			// ST_Area for WGS84 only works correctly if the geometry is a geography data type
			.append("WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area").toString();
		default:
			return null;
		}
	}

	private String getBridgePartAggregateGeometriesForLOD1() {
		switch (databaseAdapter.getDatabaseType()) {
		case ORACLE:
			return new StringBuilder("SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT ").append(schema).append(".citydb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT geom.gid FROM (SELECT b.lod<LoD>_multi_surface_id as gid ")
			.append("FROM ").append(schema).append(".bridge b ")
			.append("WHERE ")
			.append("b.id = ? ")
			.append("AND b.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT b.lod<LoD>_solid_id as gid ")
			.append("FROM ").append(schema).append(".bridge b ")
			.append("WHERE ")
			.append("b.id = ? ")
			.append("AND b.lod<LoD>_solid_id IS NOT NULL) geom ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL")
			.append(") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'")
			.append(") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>")
			.append(") ")
			.append("GROUP BY mod(rownum, <GROUP_BY_1>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_2>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_3>) ")
			.append(")").toString();
		case POSTGIS:
			return new StringBuilder("SELECT ST_Union(get_valid_area.simple_geom) ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT ST_Force2D(sg.geometry) AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT b.lod<LoD>_multi_surface_id ")
			.append("FROM ").append(schema).append(".bridge b ")
			.append("WHERE b.id = ? ")
			.append("AND b.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT b.lod<LoD>_solid_id ")
			.append("FROM ").append(schema).append(".bridge b ")
			.append("WHERE b.id = ? ")
			.append("AND b.lod<LoD>_solid_id IS NOT NULL ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL) AS get_geoms ")
			.append("WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms ")
			// ST_Area for WGS84 only works correctly if the geometry is a geography data type
			.append("WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area").toString();
		default:
			return null;
		}
	}

	public String getBridgePartAggregateGeometries (double tolerance, int srid2D, int lodToExportFrom, double groupBy1, double groupBy2, double groupBy3, int objectClassId) {
		String query;
		if (lodToExportFrom > 1) {
			query = getBridgePartAggregateGeometriesForLOD2OrHigher().replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<2D_SRID>", String.valueOf(srid2D))
					.replace("<LoD>", String.valueOf(lodToExportFrom))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));
		}
		else {
			query = getBridgePartAggregateGeometriesForLOD1().replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<2D_SRID>", String.valueOf(srid2D))
					.replace("<LoD>", String.valueOf(lodToExportFrom))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));			
		}
		return unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lodToExportFrom, objectClassId);
	}

	public String getBridgePartQuery(int lodToExportFrom, DisplayForm displayForm, boolean lodCheckOnly, int objectClassId) {
		String query = null;

		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
			query = getBridgePartFootprint(lodToExportFrom);
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lodToExportFrom, objectClassId);
			break;
		default:
			query = getBridgePartGeometry(lodToExportFrom, lodCheckOnly);
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY_REFERENCE_IDS, query, lodToExportFrom, objectClassId);
		}

		return query;
	}

	// ----------------------------------------------------------------------
	// 	Tunnel QUERIES
	// ----------------------------------------------------------------------

	public String getTunnelPartsFromTunnel() { 
		return new StringBuilder("SELECT id FROM ").append(schema).append(".TUNNEL WHERE tunnel_root_id = ?").toString();
	}

	private String getTunnelPartFootprint(int lod) {
		StringBuilder query = new StringBuilder();

		if (lod == 1) {
			switch (databaseAdapter.getDatabaseType()) {
			case ORACLE:
				return getTunnelPartAggregateGeometriesForLOD1().replace("<TOLERANCE>", "0.001")
						.replace("<2D_SRID>", "(SELECT SRID FROM " + schema + ".DATABASE_SRS)")
						.replace("<LoD>", "1")
						.replace("<GROUP_BY_1>", "256")
						.replace("<GROUP_BY_2>", "64")
						.replace("<GROUP_BY_3>", "16");
			case POSTGIS:		
				return getTunnelPartAggregateGeometriesForLOD1().replace("<TOLERANCE>", "0.001")
						.replace("<LoD>", "1");			
			default:
				return null;
			}
		}

		else {
			query.append("SELECT sg.geometry FROM ").append(schema).append(".surface_geometry sg, ")
			.append(schema).append(".tunnel_thematic_surface ts ")
			.append("WHERE ts.tunnel_id = ? AND ts.objectclass_id = 94 ")
			.append("AND sg.root_id = ts.lod").append(lod).append("_multi_surface_id AND sg.geometry IS NOT NULL ");
		}

		return query.toString();
	}

	private String getTunnelPartGeometry(int lod, boolean lodCheckOnly) {
		StringBuilder query = new StringBuilder().append("SELECT sub.* FROM (");

		if (lod > 1) {
			// exterior thematic surfaces
			query.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".tunnel_thematic_surface ts ")
			.append("WHERE ts.tunnel_id = ? ")
			.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
			.append(") tmp) ");

			if (!lodCheckOnly) {
				// thematic surfaces of exterior tunnel installations
				query.append("UNION ALL ")
				.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
				.append(implicitGeometryNullColumns)
				.append("FROM ").append(schema).append(".tunnel_thematic_surface ts ")
				.append("JOIN ").append(schema).append(".tunnel_installation tui ON ts.tunnel_installation_id = tui.id ")
				.append("WHERE tui.tunnel_id = ? ")
				.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
				.append(") tmp) ")
				// exterior tunnel installations
				.append("UNION ALL ")
				.append("(SELECT tmp.* FROM (SELECT tui.lod").append(lod).append("_brep_id, tui.objectclass_id, ")
				.append("ig.relative_brep_id, tui.lod").append(lod).append("_implicit_ref_point, ").append("tui.lod").append(lod).append("_implicit_transformation ")
				.append("FROM ").append(schema).append(".tunnel_installation tui ")
				.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = tui.lod").append(lod).append("_implicit_rep_id ")
				.append("WHERE tui.tunnel_id = ? ")
				.append("AND (tui.lod").append(lod).append("_brep_id is not null ")
				.append("OR ig.relative_brep_id is not null) ")
				.append(") tmp) ");
			}
		}

		if (lod > 2 && !lodCheckOnly) {
			// openings in exterior thematic surfaces
			query.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".tunnel_opening o ")
			.append("JOIN ").append(schema).append(".tunnel_open_to_them_srf o2ts ON o2ts.tunnel_opening_id = o.id ")
			.append("JOIN ").append(schema).append(".tunnel_thematic_surface ts ON ts.id = o2ts.tunnel_thematic_surface_id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE ts.tunnel_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// openings in exterior thematic surfaces of tunnel installations
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".tunnel_opening o ")
			.append("JOIN ").append(schema).append(".tunnel_open_to_them_srf o2ts ON o2ts.tunnel_opening_id = o.id ")
			.append("JOIN ").append(schema).append(".tunnel_thematic_surface ts ON ts.id = o2ts.tunnel_thematic_surface_id ")
			.append("JOIN ").append(schema).append(".tunnel_installation tui ON ts.tunnel_installation_id = tui.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE tui.tunnel_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ");
		}

		if (lod > 3 && !lodCheckOnly) {
			// interior thematic surfaces of tunnel installations
			query.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".tunnel_thematic_surface ts ")
			.append("JOIN ").append(schema).append(".tunnel_installation tui ON ts.tunnel_installation_id = tui.id ")
			.append("JOIN ").append(schema).append(".tunnel_hollow_space hs ON tui.tunnel_hollow_space_id = hs.id ")
			.append("WHERE hs.tunnel_id = ? ")
			.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
			.append(") tmp) ")
			// interior thematic surfaces of hollow spaces
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT ts.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".tunnel_thematic_surface ts ")
			.append("JOIN ").append(schema).append(".tunnel_hollow_space hs ON ts.tunnel_hollow_space_id = hs.id ")
			.append("WHERE hs.tunnel_id = ? ")
			.append("AND ts.lod").append(lod).append("_multi_surface_id is not null ")
			.append(") tmp) ")
			// openings of interior thematic surfaces of tunnel installations
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".tunnel_opening o ")
			.append("JOIN ").append(schema).append(".tunnel_open_to_them_srf o2ts ON o2ts.tunnel_opening_id = o.id ")
			.append("JOIN ").append(schema).append(".tunnel_thematic_surface ts ON ts.id = o2ts.tunnel_thematic_surface_id ")
			.append("JOIN ").append(schema).append(".tunnel_installation tui ON ts.tunnel_installation_id = tui.id ")
			.append("JOIN ").append(schema).append(".tunnel_hollow_space hs ON tui.tunnel_hollow_space_id = hs.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE hs.tunnel_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// openings of interior thematic surfaces of hollow spaces
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT o.lod").append(lod).append("_multi_surface_id, ts.objectclass_id, ")
			.append("ig.relative_brep_id, o.lod").append(lod).append("_implicit_ref_point, ").append("o.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".tunnel_opening o ")
			.append("JOIN ").append(schema).append(".tunnel_open_to_them_srf o2ts ON o2ts.tunnel_opening_id = o.id ")
			.append("JOIN ").append(schema).append(".tunnel_thematic_surface ts ON ts.id = o2ts.tunnel_thematic_surface_id ")
			.append("JOIN ").append(schema).append(".tunnel_hollow_space hs ON ts.tunnel_hollow_space_id = hs.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = o.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE hs.tunnel_id = ? ")
			.append("AND (o.lod").append(lod).append("_multi_surface_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// tunnel furniture
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT tf.lod4_brep_id, tf.objectclass_id, ")
			.append("ig.relative_brep_id, tf.lod4_implicit_ref_point, ").append("tf.lod4_implicit_transformation ")
			.append("FROM ").append(schema).append(".tunnel_furniture tf ")
			.append("JOIN ").append(schema).append(".tunnel_hollow_space hs ON tbf.tunnel_hollow_space_id = hs.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = tf.lod4_implicit_rep_id ")
			.append("WHERE hs.tunnel_id = ? ")
			.append("AND (tf.lod4_brep_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ")
			// hollow spaces
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT hs.lod4_solid_id, hs.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".tunnel_hollow_space hs ")
			.append("WHERE hs.tunnel_id = ? ")
			.append("AND hs.lod4_solid_id is not null ")
			.append(") tmp) ")
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT r.lod4_multi_surface_id, hs.objectclass_id, ")
			.append(implicitGeometryNullColumns)
			.append("FROM ").append(schema).append(".tunnel_hollow_space hs ")
			.append("WHERE hs.tunnel_id = ? ")
			.append("AND r.lod4_multi_surface_id is not null ")
			.append(") tmp) ")
			// interior tunnel installations
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT tui.lod").append(lod).append("_brep_id, tui.objectclass_id, ")
			.append("ig.relative_brep_id, tui.lod").append(lod).append("_implicit_ref_point, ").append("tui.lod").append(lod).append("_implicit_transformation ")
			.append("FROM ").append(schema).append(".tunnel_installation tui ")
			.append("JOIN ").append(schema).append(".tunnel_hollow_space hs ON tui.tunnel_hollow_space_id = hs.id ")
			.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = tui.lod").append(lod).append("_implicit_rep_id ")
			.append("WHERE hs.tunnel_id = ? ")
			.append("AND (tui.lod").append(lod).append("_brep_id is not null ")
			.append("OR ig.relative_brep_id is not null) ")
			.append(") tmp) ");
		}

		if (lod > 1)
			query.append("UNION ALL ");

		// tunnel geometry
		query.append("(SELECT tmp.* FROM (SELECT lod").append(lod).append("_solid_id, 0 as objectclass_id, ")
		.append(implicitGeometryNullColumns)
		.append("FROM ").append(schema).append(".tunnel t ")
		.append("WHERE t.id = ? ")
		.append("AND lod").append(lod).append("_solid_id is not null ")
		.append(") tmp) ")
		.append("UNION ALL ")
		.append("(SELECT tmp.* FROM (SELECT t.lod").append(lod).append("_multi_surface_id, 0 as objectclass_id, ")
		.append(implicitGeometryNullColumns)
		.append("FROM ").append(schema).append(".tunnel t ")
		.append("WHERE t.id = ? ")
		.append("AND t.lod").append(lod).append("_multi_surface_id is not null ")
		.append(") tmp) ")
		.append(") sub ");

		return query.toString();
	}

	private String getTunnelPartAggregateGeometriesForLOD2OrHigher() {
		switch (databaseAdapter.getDatabaseType()) {
		case ORACLE:
			return new StringBuilder("SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT ").append(schema).append(".citydb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT geom.gid FROM (SELECT t.lod<LoD>_multi_surface_id as gid ")
			.append("FROM ").append(schema).append(".TUNNEL t ")
			.append("WHERE ")
			.append("t.id = ? ")
			.append("AND t.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT t.lod<LoD>_solid_id as gid ")
			.append("FROM ").append(schema).append(".TUNNEL t ")
			.append("WHERE ")
			.append("t.id = ? ")
			.append("AND t.lod<LoD>_solid_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT ts.lod<LoD>_multi_surface_id as gid ")
			.append("FROM ").append(schema).append(".TUNNEL_THEMATIC_SURFACE ts ")
			.append("WHERE ")
			.append("ts.tunnel_id = ? ")
			.append("AND ts.lod<LoD>_multi_surface_id IS NOT NULL) geom ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL")
			.append(") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'")
			.append(") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>")
			.append(") ")
			.append("GROUP BY mod(rownum, <GROUP_BY_1>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_2>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_3>) ")
			.append(")").toString();
		case POSTGIS:
			return new StringBuilder("SELECT ST_Union(get_valid_area.simple_geom) ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT ST_Force2D(sg.geometry) AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT t.lod<LoD>_multi_surface_id ")
			.append("FROM ").append(schema).append(".TUNNEL t ")
			.append("WHERE t.id = ? ")
			.append("AND t.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT t.lod<LoD>_solid_id ")
			.append("FROM ").append(schema).append(".TUNNEL t ")
			.append("WHERE t.id = ? ")
			.append("AND t.lod<LoD>_solid_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT ts.lod<LoD>_multi_surface_id ")
			.append("FROM ").append(schema).append(".TUNNEL_THEMATIC_SURFACE ts ")
			.append("WHERE ts.tunnel_id = ? ")
			.append("AND ts.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL) AS get_geoms ")
			.append("WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms ")
			// ST_Area for WGS84 only works correctly if the geometry is a geography data type
			.append("WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area").toString();
		default:
			return null;
		}
	}

	private String getTunnelPartAggregateGeometriesForLOD1() {
		switch (databaseAdapter.getDatabaseType()) {
		case ORACLE:
			return new StringBuilder("SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(aggr_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (SELECT sdo_aggr_union(mdsys.sdoaggrtype(simple_geom, <TOLERANCE>)) aggr_geom ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT ").append(schema).append(".citydb_util.to_2d(sg.geometry, <2D_SRID>) AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT geom.gid FROM (SELECT t.lod<LoD>_multi_surface_id as gid ")
			.append("FROM ").append(schema).append(".TUNNEL t ")
			.append("WHERE ")
			.append("t.id = ? ")
			.append("AND t.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT t.lod<LoD>_solid_id as gid ")
			.append("FROM ").append(schema).append(".TUNNEL t ")
			.append("WHERE ")
			.append("t.id = ? ")
			.append("AND t.lod<LoD>_solid_id IS NOT NULL) geom ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL")
			.append(") WHERE sdo_geom.validate_geometry(simple_geom, <TOLERANCE>) = 'TRUE'")
			.append(") WHERE sdo_geom.sdo_area(simple_geom, <TOLERANCE>) > <TOLERANCE>")
			.append(") ")
			.append("GROUP BY mod(rownum, <GROUP_BY_1>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_2>) ")
			.append(") ")
			.append("GROUP BY mod (rownum, <GROUP_BY_3>) ")
			.append(")").toString();
		case POSTGIS:
			return new StringBuilder("SELECT ST_Union(get_valid_area.simple_geom) ")
			.append("FROM (")
			.append("SELECT * FROM (")
			.append("SELECT * FROM (")
			.append("SELECT ST_Force2D(sg.geometry) AS simple_geom ")
			.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
			.append("WHERE ")
			.append("sg.root_id IN( ")
			.append("SELECT t.lod<LoD>_multi_surface_id ")
			.append("FROM ").append(schema).append(".TUNNEL t ")
			.append("WHERE t.id = ? ")
			.append("AND t.lod<LoD>_multi_surface_id IS NOT NULL ")
			.append("UNION ")
			.append("SELECT t.lod<LoD>_solid_id ")
			.append("FROM ").append(schema).append(".TUNNEL t ")
			.append("WHERE t.id = ? ")
			.append("AND t.lod<LoD>_solid_id IS NOT NULL ")
			.append(") ")
			.append("AND sg.geometry IS NOT NULL) AS get_geoms ")
			.append("WHERE ST_IsValid(get_geoms.simple_geom) = 'TRUE') AS get_valid_geoms ")
			// ST_Area for WGS84 only works correctly if the geometry is a geography data type
			.append("WHERE ST_Area(ST_Transform(get_valid_geoms.simple_geom,4326)::geography, true) > <TOLERANCE>) AS get_valid_area").toString();
		default:
			return null;
		}
	}

	public String getTunnelPartAggregateGeometries(double tolerance, int srid2D, int lodToExportFrom, double groupBy1, double groupBy2, double groupBy3, int objectClassId) {
		String query;
		if (lodToExportFrom > 1) {
			query = getTunnelPartAggregateGeometriesForLOD2OrHigher().replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<2D_SRID>", String.valueOf(srid2D))
					.replace("<LoD>", String.valueOf(lodToExportFrom))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));
		}
		else {
			query = getTunnelPartAggregateGeometriesForLOD1().replace("<TOLERANCE>", String.valueOf(tolerance))
					.replace("<2D_SRID>", String.valueOf(srid2D))
					.replace("<LoD>", String.valueOf(lodToExportFrom))
					.replace("<GROUP_BY_1>", String.valueOf(groupBy1))
					.replace("<GROUP_BY_2>", String.valueOf(groupBy2))
					.replace("<GROUP_BY_3>", String.valueOf(groupBy3));			
		}
		return unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lodToExportFrom, objectClassId);
	}

	public String getTunnelPartQuery(int lodToExportFrom, DisplayForm displayForm, boolean lodCheckOnly, int objectClassId) {
		String query = null;

		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
			query = getTunnelPartFootprint(lodToExportFrom);
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lodToExportFrom, objectClassId);
			break;
		default:
			query = getTunnelPartGeometry(lodToExportFrom, lodCheckOnly);
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY_REFERENCE_IDS, query, lodToExportFrom, objectClassId);
		}
		
		return query;
	}

	// ----------------------------------------------------------------------
	// CITY OBJECT GROUP QUERIES
	// ----------------------------------------------------------------------
	public String getCityObjectGroupFootprint(int objectClassId) {
		String query = new StringBuilder("SELECT sg.geometry ")
		.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg, ")
		.append(schema).append(".CITYOBJECTGROUP cog ")
		.append("WHERE ")
		.append("cog.id = ? ")
		.append("AND sg.root_id = cog.brep_id ")
		.append("AND sg.geometry IS NOT NULL ").toString();
		return unionADEQueries(QUERY_SURFACE_GEOMETRY, query, -1, objectClassId);
	}

/*	public String getCityObjectGroupMembers() {
		return new StringBuilder("SELECT co.id, co.gmlid, co.envelope, co.objectclass_id ")
		.append("FROM ").append(schema).append(".CITYOBJECT co ")
		.append("WHERE co.ID IN (SELECT g2co.cityobject_id ")
		.append("FROM ").append(schema).append(".GROUP_TO_CITYOBJECT g2co ")
		.append("WHERE g2co.cityobjectgroup_id = ?) ")
		.append("ORDER BY co.objectclass_id").toString();
	}

	public String getCityObjectGroupMembersInBBOX() {
		StringBuilder query = new StringBuilder()
				.append("SELECT co.id, co.gmlid, co.objectclass_id, co.envelope ")
				.append("FROM ").append(schema).append(".CITYOBJECT co ")
				.append("WHERE co.ID IN (SELECT g2co.cityobject_id ")
				.append("FROM ").append(schema).append(".GROUP_TO_CITYOBJECT g2co ")
				.append("WHERE g2co.cityobjectgroup_id = ?) ")
				.append("AND ");

		switch (databaseAdapter.getDatabaseType()) {
			case ORACLE:
				query.append("SDO_ANYINTERACT(co.envelope, ?) = 'TRUE'");
				break;
			case POSTGIS:
				query.append("co.envelope && ?");
				break;
		}

		return query.toString();
	}*/

	// ----------------------------------------------------------------------
	// SOLITARY VEGETATION OBJECT QUERIES
	// ----------------------------------------------------------------------

	public String getSolitaryVegetationObjectQuery(int lodToExportFrom, DisplayForm displayForm, int  objectClassId) {
		String query = null;

		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
			query = new StringBuilder("SELECT sg.geometry, ")
					.append(implicitGeometryNullColumns)
					.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg, ")
					.append(schema).append(".SOLITARY_VEGETAT_OBJECT svo ")
					.append("WHERE ")
					.append("sg.cityobject_id = ? ")
					.append("AND sg.geometry IS NOT NULL ")
					.append("UNION ALL ")
					.append("SELECT sg.implicit_geometry, svo.lod").append(lodToExportFrom).append("_implicit_rep_id, svo.lod").append(lodToExportFrom).append("_implicit_ref_point, svo.lod").append(lodToExportFrom).append("_implicit_transformation ")
					.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
					.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON sg.root_id = ig.relative_brep_id ")
					.append("LEFT JOIN ").append(schema).append(".SOLITARY_VEGETAT_OBJECT svo ON ig.id = svo.lod").append(lodToExportFrom).append("_implicit_rep_id ")
					.append("WHERE svo.id = ? ")
					.append("AND sg.implicit_geometry IS NOT NULL").toString();
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lodToExportFrom, objectClassId);
			break;
		default:
			query = new StringBuilder("SELECT svo.lod").append(lodToExportFrom).append("_brep_id, '7' as objectclass_id, ")
					.append("ig.relative_brep_id, svo.lod").append(lodToExportFrom).append("_implicit_ref_point, svo.lod").append(lodToExportFrom).append("_implicit_transformation ")
					.append("FROM ").append(schema).append(".SOLITARY_VEGETAT_OBJECT svo ")
					.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = svo.lod").append(lodToExportFrom).append("_implicit_rep_id ")
					.append("WHERE svo.id = ?").toString();
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY_REFERENCE_IDS, query, lodToExportFrom, objectClassId);
		}

		return query;
	}

	// ----------------------------------------------------------------------
	// PLANT COVER QUERIES
	// ----------------------------------------------------------------------

	public String getPlantCoverQuery(int lodToExportFrom, DisplayForm displayForm, int objectClassId) {
		String query;
		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
			query = new StringBuilder("select sub.* from (").append("(SELECT tmp.* FROM (SELECT sg.geometry ")
			.append("FROM ").append(schema).append(".surface_geometry sg ")
			.append("JOIN ").append(schema).append(".PLANT_COVER pc ON sg.root_id = pc.lod").append(lodToExportFrom).append("_multi_surface_id ")
			.append("WHERE pc.id = ? ")
			.append("AND sg.geometry IS NOT NULL ")
			.append(") tmp) ")
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT sg.geometry ")
			.append("FROM ").append(schema).append(".surface_geometry sg ")
			.append("JOIN ").append(schema).append(".PLANT_COVER pc ON sg.root_id = pc.lod").append(lodToExportFrom).append("_multi_solid_id ")
			.append("WHERE pc.id = ?")
			.append("AND sg.geometry IS NOT NULL ")
			.append(") tmp) ")
			.append(") sub ").toString();
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lodToExportFrom, objectClassId);
			break;
		default:
			query = new StringBuilder("select sub.* from (").append("(SELECT tmp.* FROM (SELECT pc.lod").append(lodToExportFrom).append("_multi_surface_id, '8' as objectclass_id ")
			.append("FROM ").append(schema).append(".PLANT_COVER pc ")
			.append("WHERE pc.id = ? ")
			.append("AND pc.lod").append(lodToExportFrom).append("_multi_surface_id is not null")
			.append(") tmp) ")
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT pc.lod").append(lodToExportFrom).append("_multi_solid_id, '8' as objectclass_id ")
			.append("FROM ").append(schema).append(".PLANT_COVER pc ")
			.append("WHERE pc.id = ? ")
			.append("AND pc.lod").append(lodToExportFrom).append("_multi_solid_id is not null")
			.append(") tmp) ")
			.append(") sub ").toString();
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY_REFERENCE_IDS, query, lodToExportFrom, objectClassId);
		}

		return query;
	}

	// ----------------------------------------------------------------------
	// GENERIC CITY OBJECT QUERIES
	// ----------------------------------------------------------------------

	public String getGenericCityObjectQuery(int lodToExportFrom, DisplayForm displayForm, int objectClassId) {
		String query = null;

		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
			query = new StringBuilder("SELECT sg.geometry, ")
					.append(implicitGeometryNullColumns)
					.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg, ")
					.append(schema).append(".GENERIC_CITYOBJECT gco ")
					.append("WHERE ")
					.append("sg.cityobject_id = ? ")
					.append("AND sg.geometry IS NOT NULL ")
					.append("UNION ALL ")
					.append("SELECT sg.implicit_geometry, gco.lod").append(lodToExportFrom).append("_implicit_rep_id, gco.lod").append(lodToExportFrom).append("_implicit_ref_point, gco.lod").append(lodToExportFrom).append("_implicit_transformation ")
					.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
					.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON sg.root_id = ig.relative_brep_id ")
					.append("LEFT JOIN ").append(schema).append(".GENERIC_CITYOBJECT gco ON ig.id = gco.lod").append(lodToExportFrom).append("_implicit_rep_id ")
					.append("WHERE gco.id = ? ")
					.append("AND sg.implicit_geometry IS NOT NULL").toString();
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lodToExportFrom, objectClassId);
			break;
		default:
			query = new StringBuilder("SELECT gco.lod").append(lodToExportFrom).append("_brep_id, '5' as objectclass_id, ")
					.append("ig.relative_brep_id, gco.lod").append(lodToExportFrom).append("_implicit_ref_point, gco.lod").append(lodToExportFrom).append("_implicit_transformation ")
					.append("FROM ").append(schema).append(".GENERIC_CITYOBJECT gco ")
					.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = gco.lod").append(lodToExportFrom).append("_implicit_rep_id ")
					.append("WHERE gco.id = ?").toString();
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY_REFERENCE_IDS, query, lodToExportFrom, objectClassId);
		}

		return query;
	}

	public String getGenericCityObjectPointAndCurveQuery(int lodToExportFrom, int objectClassId) {
		String query;

		query = new StringBuilder().append("SELECT gco.lod").append(lodToExportFrom).append("_other_geom ")
		.append("FROM ").append(schema).append(".generic_cityobject gco ")
		.append("WHERE gco.id = ? ")
		.append("AND gco.lod").append(lodToExportFrom).append("_other_geom IS NOT NULL").toString();

		query = unionADEQueries(QUERY_POINT_AND_CURVE_GEOMETRY, query, lodToExportFrom, objectClassId);
		return query;
	}

	// ----------------------------------------------------------------------
	// CITY FURNITURE QUERIES
	// ----------------------------------------------------------------------

	public String getCityFurnitureQuery(int lodToExportFrom, DisplayForm displayForm, int objectClassId) {
		String query = null;

		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
			query = new StringBuilder("SELECT sg.geometry, ")
					.append(implicitGeometryNullColumns)
					.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg, ")
					.append(schema).append(".CITY_FURNITURE cf ")
					.append("WHERE ")
					.append("sg.cityobject_id = ? ")
					.append("AND sg.geometry IS NOT NULL ")
					.append("UNION ALL ")
					.append("SELECT sg.implicit_geometry, cf.lod").append(lodToExportFrom).append("_implicit_rep_id, cf.lod").append(lodToExportFrom).append("_implicit_ref_point, cf.lod").append(lodToExportFrom).append("_implicit_transformation ")
					.append("FROM ").append(schema).append(".SURFACE_GEOMETRY sg ")
					.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON sg.root_id = ig.relative_brep_id ")
					.append("LEFT JOIN ").append(schema).append(".CITY_FURNITURE cf ON ig.id = cf.lod").append(lodToExportFrom).append("_implicit_rep_id ")
					.append("WHERE cf.id = ? ")
					.append("AND sg.implicit_geometry IS NOT NULL").toString();
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lodToExportFrom, objectClassId);
			break;
		default:
			query = new StringBuilder("SELECT cf.lod").append(lodToExportFrom).append("_brep_id, '21' as objectclass_id, ")
					.append("ig.relative_brep_id, cf.lod").append(lodToExportFrom).append("_implicit_ref_point, cf.lod").append(lodToExportFrom).append("_implicit_transformation ")
					.append("FROM ").append(schema).append(".CITY_FURNITURE cf ")
					.append("LEFT JOIN ").append(schema).append(".IMPLICIT_GEOMETRY ig ON ig.id = cf.lod").append(lodToExportFrom).append("_implicit_rep_id ")
					.append("WHERE cf.id = ?").toString();
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY_REFERENCE_IDS, query, lodToExportFrom, objectClassId);
		}

		return query;
	}

	// ----------------------------------------------------------------------
	// WATER BODY QUERIES
	// ----------------------------------------------------------------------

	private String getWaterBodyFootprint(int lod) {
		StringBuilder query = new StringBuilder();

		if (lod == 0) {
			query.append("SELECT sg.geometry FROM ").append(schema).append(".surface_geometry sg, ")
			.append(schema).append(".waterbody wb ")
			.append("WHERE wb.lod0_multi_surface_id = sg.root_id ")
			.append("AND sg.geometry IS NOT NULL AND wb.id = ?");
		} 

		else if (lod == 1) {
			query.append("SELECT sg.geometry FROM ").append(schema).append(".surface_geometry sg, ")
			.append(schema).append(".waterbody wb ")
			.append("WHERE wb.lod1_multi_surface_id = sg.root_id ")
			.append("AND sg.geometry IS NOT NULL AND wb.id = ? ")
			.append("UNION ")
			.append("SELECT sg.geometry FROM ").append(schema).append(".surface_geometry sg, ")
			.append(schema).append(".waterbody wb ")
			.append("WHERE wb.lod1_solid_id = sg.root_id ")
			.append("AND sg.geometry IS NOT NULL AND wb.id = ? ");
		}

		else {
			query.append("SELECT sg.geometry FROM ").append(schema).append(".surface_geometry sg, ")
			.append(schema).append(".waterbod_to_waterbnd_srf wb2wbs, ")
			.append(schema).append(".waterboundary_surface wbs ")
			.append("WHERE wb2wbs.waterbody_id = ? ")
			.append("AND wbs.id = wb2wbs.waterboundary_surface_id ")
			.append("AND wbs.objectclass_id = 12 ")
			.append("AND sg.root_id = wbs.lod").append(lod).append("_surface_id ")
			.append("AND sg.geometry IS NOT NULL ");
		}

		return query.toString();
	}

	private String getWaterBodyGeometry(int lod) {
		StringBuilder query = new StringBuilder();

		query.append("select sub.* from (");

		if (lod == 1) {
			query.append("(SELECT tmp.* FROM (SELECT wb.lod1_multi_surface_id, 0 as objectclass_id ")
			.append("FROM ").append(schema).append(".WATERBODY wb ")
			.append("WHERE wb.id = ? ")
			.append("AND wb.lod1_multi_surface_id is not null ")
			.append(") tmp) ")
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT wb.lod1_solid_id, 0 as objectclass_id ")
			.append("FROM ").append(schema).append(".WATERBODY wb ")
			.append("WHERE wb.id = ? ")
			.append("AND wb.lod1_solid_id is not null ")
			.append(") tmp) ");			
		}

		else {
			query.append("(SELECT tmp.* FROM (SELECT wbs.lod").append(lod).append("_surface_id, wbs.objectclass_id ")
			.append("FROM ").append(schema).append(".waterboundary_surface wbs ")
			.append("JOIN ").append(schema).append(".waterbod_to_waterbnd_srf wb2wbs ON wb2wbs.waterboundary_surface_id = wbs.id ")
			.append("WHERE wb2wbs.waterbody_id = ? ")
			.append("AND wbs.lod").append(lod).append("_surface_id is not null ")
			.append(") tmp) ")
			.append("UNION ALL ")
			.append("(SELECT tmp.* FROM (SELECT wb.lod").append(lod).append("_solid_id, 0 as objectclass_id ")
			.append("FROM ").append(schema).append(".WATERBODY wb ")
			.append("WHERE wb.id = ? ")
			.append("AND wb.lod").append(lod).append("_solid_id is not null ")
			.append(") tmp) ");	
		}

		query.append(") sub ");
		return query.toString();
	}

	public String getWaterBodyQuery(int lodToExportFrom, DisplayForm displayForm, int objectClassId) {
		String query;
		
		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
			query = getWaterBodyFootprint(lodToExportFrom);
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lodToExportFrom, objectClassId);
			break;
		default:
			query = getWaterBodyGeometry(lodToExportFrom);
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY_REFERENCE_IDS, query, lodToExportFrom, objectClassId);
		}

		return query;
	}

	// ----------------------------------------------------------------------
	// LAND USE QUERIES
	// ----------------------------------------------------------------------

	public String getLandUseQuery (int lodToExportFrom, DisplayForm displayForm, int objectClassId) {
		String query;

		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
			query = new StringBuilder("SELECT sg.geometry ")
			.append("FROM ").append(schema).append(".surface_geometry sg ")
			.append("JOIN ").append(schema).append(".LAND_USE lu ON sg.root_id = lu.lod").append(lodToExportFrom).append("_multi_surface_id ")
			.append("WHERE lu.id = ? ")
			.append("AND sg.geometry IS NOT NULL").toString();
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lodToExportFrom, objectClassId);
			break;
		default:
			query = new StringBuilder("SELECT lu.lod").append(lodToExportFrom).append("_multi_surface_id, '4' as objectclass_id ")
			.append("FROM ").append(schema).append(".LAND_USE lu ")
			.append("WHERE lu.id = ? ")
			.append("AND lu.lod").append(lodToExportFrom).append("_multi_surface_id is not null").toString();
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY_REFERENCE_IDS, query, lodToExportFrom, objectClassId);
		}

		return query.toString();
	}

	// ----------------------------------------------------------------------
	// TRANSPORTATION QUERIES
	// ----------------------------------------------------------------------

	private String getTransportationFootprint(int lod) {
		StringBuilder query = new StringBuilder();

		query.append("select sub.* from (");

		if (lod == 0) {
			query.append("(SELECT tc.lod0_network FROM ").append(schema).append(".transportation_complex tc ")
			.append("WHERE tc.id = ? ")
			.append("AND tc.lod0_network IS NOT NULL")
			.append(") tmp) ");
		}

		else {
			if (lod > 1) {
				query.append("(SELECT tmp.* FROM (SELECT sg.geometry ")
				.append("FROM ").append(schema).append(".surface_geometry sg ")
				.append("JOIN ").append(schema).append(".traffic_area ta on sg.root_id = ta.lod").append(lod).append("_multi_surface_id ")
				.append("WHERE ta.transportation_complex_id = ? ")
				.append("AND sg.geometry IS NOT NULL ")
				.append(") tmp) ")
				.append("UNION ALL ");
			}

			query.append("(SELECT tmp.* FROM (SELECT sg.geometry ")
			.append("FROM ").append(schema).append(".surface_geometry sg ")
			.append("JOIN ").append(schema).append(".transportation_complex tc ON sg.root_id = tc.lod").append(lod).append("_multi_surface_id ")
			.append("WHERE tc.id = ? ")
			.append("AND sg.geometry IS NOT NULL ")
			.append(") tmp) ");
		}

		query.append(") sub ");
		return query.toString();
	}

	private String getTransportationGeometry(int lod) {
		StringBuilder query = new StringBuilder();

		query.append("select sub.* from (");

		if (lod > 1) {
			query.append("(SELECT tmp.* FROM (SELECT ta.lod").append(lod).append("_multi_surface_id, ta.objectclass_id ")
			.append("FROM ").append(schema).append(".traffic_area ta ")
			.append("WHERE ta.transportation_complex_id = ? ")
			.append("AND ta.lod").append(lod).append("_multi_surface_id is not null ")
			.append(") tmp) ")
			.append("UNION ALL ");
		}

		query.append("(SELECT tmp.* FROM (SELECT tc.lod").append(lod).append("_multi_surface_id, tc.objectclass_id ")
		.append("FROM ").append(schema).append(".transportation_complex tc ")
		.append("WHERE tc.id = ? ")
		.append("AND tc.lod").append(lod).append("_multi_surface_id is not null ")
		.append(") tmp) ");

		query.append(") sub ");
		return query.toString();
	}

	public String getTransportationQuery(int lod, DisplayForm displayForm, int objectClassId) {
		String query;

		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
			query = getTransportationFootprint(lod);
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lod, objectClassId);
			break;
		default:
			query = getTransportationGeometry(lod);
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY_REFERENCE_IDS, query, lod, objectClassId);
		}

		return query;		
	}

	// ----------------------------------------------------------------------
	// RELIEF QUERIES
	// ----------------------------------------------------------------------

	public String getReliefQuery(int lod, DisplayForm displayForm, int objectClassId) {
		String query;

		switch (displayForm.getForm()) {
		case DisplayForm.FOOTPRINT:
		case DisplayForm.EXTRUDED:
			query = new StringBuilder("SELECT sg.geometry ")
			.append("FROM ").append(schema).append(".surface_geometry sg ")
			.append("JOIN ").append(schema).append(".tin_relief tr ON sg.root_id = tr.surface_geometry_id ")
			.append("JOIN ").append(schema).append(".relief_feat_to_rel_comp rf2rc ON rf2rc.relief_component_id = tr.id ")
			.append("JOIN ").append(schema).append(".relief_feature rf ON rf.id = rf2rc.relief_feature_id ")
			.append("WHERE rf.id = ? ")
			.append("AND rf.lod = ").append(lod).append(" ")
			.append("AND sg.geometry IS NOT NULL").toString();
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY, query, lod, objectClassId);
			break;
		default:
			query = new StringBuilder("SELECT tr.surface_geometry_id, tr.objectclass_id ")
			.append("FROM ").append(schema).append(".tin_relief tr ")
			.append("JOIN ").append(schema).append(".relief_feat_to_rel_comp rf2rc ON rf2rc.relief_component_id = tr.id ")
			.append("JOIN ").append(schema).append(".relief_feature rf ON rf.id = rf2rc.relief_feature_id ")
			.append("WHERE rf.id = ? ")
			.append("AND rf.lod = ").append(lod).append(" ")
			.append("AND tr.surface_geometry_id is not null").toString();
			query = unionADEQueries(QUERY_SURFACE_GEOMETRY_REFERENCE_IDS, query, lod, objectClassId);
		}

		return query;
	}

	private String unionADEQueries(int queryType, String baseQuery, int lod, int objectClassId) {
		StringBuilder builder = new StringBuilder();

		if (exportManager != null) {
			for (ADEKmlExportManager adeKmlExportManager : exportManager.getADEKmlExportManagers()) {
				ADEKmlExporter exporter = null;
				try {
					exporter = adeKmlExportManager.getKmlExporter(objectClassId);
				} catch (ADEKmlExportException e) {
					Logger.getInstance().warn(e.getMessage());
				}
				if (exporter != null) {
					String adeQuery = null;
					if (queryType == QUERY_POINT_AND_CURVE_GEOMETRY)
						adeQuery = exporter.getQueries().getPointAndCurveQuery(lod);
					else if (queryType == QUERY_SURFACE_GEOMETRY)
						adeQuery = exporter.getQueries().getSurfaceGeometryQuery(lod);
					else if (queryType == QUERY_SURFACE_GEOMETRY_REFERENCE_IDS)
						adeQuery = exporter.getQueries().getSurfaceGeometryRefIdsQuery(lod);
					if (adeQuery != null)
						builder.append(adeQuery).append(" union all ");
				}
			}
		}

		return builder.append(baseQuery).toString();
	}

}
