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
package org.citydb.database.adapter.postgis;

import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.api.geometry.BoundingBox;
import org.citydb.database.adapter.AbstractSQLAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.database.adapter.BlobExportAdapterImpl;
import org.citydb.database.adapter.BlobImportAdapter;
import org.citydb.database.adapter.BlobImportAdapterImpl;
import org.citydb.database.adapter.BlobType;
import org.citydb.modules.citygml.importer.database.content.DBSequencerEnum;

public class SQLAdapter extends AbstractSQLAdapter {

	@Override
	public String getInteger() {
		return "INTEGER";
	}

	@Override
	public String getSmallInt() {
		return "SMALLINT";
	}

	@Override
	public String getBigInt() {
		return "BIGINT";
	}

	@Override
	public String getNumeric() {
		return "NUMERIC";
	}

	@Override
	public String getNumeric(int precision) {
		return "NUMERIC(" + precision + ")";
	}

	@Override
	public String getNumeric(int precision, int scale) {
		return "NUMERIC(" + precision + "," + scale + ")";
	}

	@Override
	public String getReal() {
		return "REAL";
	}

	@Override
	public String getDoublePrecision() {
		return "DOUBLE PRECISION";
	}

	@Override
	public String getCharacter(int nrOfChars) {
		return "CHAR(" + nrOfChars + ")";
	}

	@Override
	public String getCharacterVarying(int nrOfChars) {
		return "VARCHAR(" + nrOfChars + ")";
	}

	@Override
	public String getPolygon2D() {
		return "GEOMETRY(POLYGON)";
	}

	@Override
	public String getCreateUnloggedTable(String tableName, String columns) {
		StringBuilder builder = new StringBuilder()
		.append("create unlogged table ")
		.append(tableName).append(" ")
		.append(columns);

		return builder.toString();
	}

	@Override
	public String getCreateUnloggedTableAsSelectFrom( String targetTableName, String sourceTableName) {
		StringBuilder builder = new StringBuilder()
		.append("create unlogged table ")
		.append(targetTableName).append(" ")
		.append("as select * from ")
		.append(sourceTableName);

		return builder.toString();
	}
	
	@Override
	public String getNextSequenceValue(DBSequencerEnum sequence) {
		return new StringBuilder("nextval('").append(getSequenceName(sequence)).append("')").toString();
	}
	
	@Override
	public String getCurrentSequenceValue(DBSequencerEnum sequence) {
		return new StringBuilder("currval('").append(getSequenceName(sequence)).append("')").toString();
	}
	
	@Override
	public String getNextSequenceValuesQuery(DBSequencerEnum sequence) {
		return new StringBuilder("select ")
		.append(resolveDatabaseOperationName("citydb_util.get_seq_values")).append("(")
		.append("'").append(getSequenceName(sequence)).append("'").append(",")
		.append("?").append(")").toString();
	}

	@Override
	public String getUnloggedIndexProperty() {
		return "";
	}
	
	@Override
	public boolean requiresPseudoTableInSelect() {
		return false;
	}

	@Override
	public String getPseudoTableName() {
		return "";
	}
	
	@Override
	public int getMaximumNumberOfItemsForInOperator() {
		return 1000;
	}

	@Override
	public String getBoundingBoxPredicate(String attributeName, BoundingBox bbox, boolean overlap) {
		StringBuilder geometry = new StringBuilder()
		.append("ST_GeomFromEWKT('SRID=").append(bbox.getSrs().getSrid())
		.append(";POLYGON((")
		.append(bbox.getLowerCorner().getX()).append(" ").append(bbox.getLowerCorner().getY()).append(",")
		.append(bbox.getLowerCorner().getX()).append(" ").append(bbox.getUpperCorner().getY()).append(",")
		.append(bbox.getUpperCorner().getX()).append(" ").append(bbox.getUpperCorner().getY()).append(",")
		.append(bbox.getUpperCorner().getX()).append(" ").append(bbox.getLowerCorner().getY()).append(",")
		.append(bbox.getLowerCorner().getX()).append(" ").append(bbox.getLowerCorner().getY()).append("))')");

		StringBuilder predicate = new StringBuilder();

		if (!overlap)
			predicate.append("ST_CoveredBy(").append(attributeName).append(", ").append(geometry).append(") = 'TRUE'");
		else
			predicate.append('(').append(attributeName).append(" && ").append(geometry).append(')');

		return predicate.toString();
	}

	@Override
	public boolean spatialPredicateRequiresNoIndexHint() {
		return false;
	}

	@Override
	public String getHierarchicalGeometryQuery() {
		// the current PostGIS JDBC driver lacks support for geometry objects of type PolyhedralSurface
		// thus, we have to use the database function ST_AsEWKT to load such geometries
		// TODO: rework as soon as the JDBC driver supports PolyhedralSurface
		StringBuilder query = new StringBuilder()
		.append("WITH RECURSIVE geometry_rec (id, gmlid, parent_id, root_id, is_solid, is_composite, is_triangulated, is_xlink, is_reverse, geometry, implicit_geometry, solid_geometry, cityobject_id, level) ")
		.append("AS (SELECT sg.id, sg.gmlid, sg.parent_id, sg.root_id, sg.is_solid, sg.is_composite, sg.is_triangulated, sg.is_xlink, sg.is_reverse, sg.geometry, sg.implicit_geometry, sg.solid_geometry, sg.cityobject_id, 1 AS level FROM surface_geometry sg WHERE sg.id=? UNION ALL ")
		.append("SELECT sg.id, sg.gmlid, sg.parent_id, sg.root_id, sg.is_solid, sg.is_composite, sg.is_triangulated, sg.is_xlink, sg.is_reverse, sg.geometry, sg.implicit_geometry, sg.solid_geometry, sg.cityobject_id, g.level + 1 AS level FROM surface_geometry sg, geometry_rec g WHERE sg.parent_id=g.id) ")
		.append("SELECT id, gmlid, parent_id, root_id, is_solid, is_composite, is_triangulated, is_xlink, is_reverse, geometry, implicit_geometry, ST_AsEWKT(solid_geometry) as solid_geometry, cityobject_id, level FROM geometry_rec");
		
		return query.toString();
	}

	@Override
	public BlobImportAdapter getBlobImportAdapter(Connection connection, BlobType type) throws SQLException {
		return new BlobImportAdapterImpl(connection, type);
	}

	@Override
	public BlobExportAdapter getBlobExportAdapter(Connection connection, BlobType type) {
		return new BlobExportAdapterImpl(connection, type);
	}

}
