/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 * 
 * Copyright 2013 - 2017
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
package org.citydb.database.adapter.oracle;

import java.sql.Connection;
import java.sql.SQLException;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.adapter.AbstractSQLAdapter;
import org.citydb.database.adapter.BlobExportAdapter;
import org.citydb.database.adapter.BlobImportAdapter;
import org.citydb.database.adapter.BlobType;
import org.citydb.query.filter.selection.operator.spatial.SpatialOperatorName;

import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.expression.StringLiteral;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.projection.Function;

public class SQLAdapter extends AbstractSQLAdapter {
	
	protected SQLAdapter(AbstractDatabaseAdapter databaseAdapter) {
		super(databaseAdapter);
	}

	@Override
	public String getInteger() {
		return "NUMBER";
	}

	@Override
	public String getSmallInt() {
		return "NUMBER";
	}

	@Override
	public String getBigInt() {
		return "NUMBER";
	}

	@Override
	public String getNumeric() {
		return "NUMBER";
	}

	@Override
	public String getNumeric(int precision) {
		return "NUMBER(" + precision + ")";
	}

	@Override
	public String getNumeric(int precision, int scale) {
		return "NUMBER(" + precision + "," + scale + ")";
	}

	@Override
	public String getReal() {
		return "BINARY_FLOAT";
	}

	@Override
	public String getDoublePrecision() {
		return "BINARY_DOUBLE";
	}

	@Override
	public String getCharacter(int nrOfChars) {
		return "CHAR(" + nrOfChars + ")";
	}

	@Override
	public String getCharacterVarying(int nrOfChars) {
		return "VARCHAR2(" + nrOfChars + ")";
	}

	@Override
	public String getPolygon2D() {
		return "MDSYS.SDO_GEOMETRY";
	}

	@Override
	public String getCreateUnloggedTable(String tableName, String columns) {
		StringBuilder builder = new StringBuilder()
		.append("create table ")
		.append(tableName).append(" ")
		.append(columns).append(" ")
		.append("nologging");

		return builder.toString();
	}

	@Override
	public String getCreateUnloggedTableAsSelectFrom( String targetTableName, String sourceTableName) {
		StringBuilder builder = new StringBuilder()
		.append("create table ")
		.append(targetTableName).append(" ")
		.append("nologging ")
		.append("as select * from ")
		.append(sourceTableName);

		return builder.toString();
	}

	@Override
	public String getNextSequenceValue(String sequence, String schema) {
		return new StringBuilder(schema).append(".").append(sequence).append(".nextval").toString();
	}

	@Override
	public String getCurrentSequenceValue(String sequence, String schema) {
		return new StringBuilder(schema).append(".").append(sequence).append(".currval").toString();
	}

	@Override
	public String getNextSequenceValuesQuery(String sequence, String schema) {
		return new StringBuilder("select * from table(")
		.append(resolveDatabaseOperationName("citydb_util.get_seq_values")).append("(")
		.append("'").append(schema).append(".").append(sequence).append("'").append(",")
		.append("?").append("))").toString();
	}

	@Override
	public String getUnloggedIndexProperty() {
		return "nologging";
	}

	@Override
	public boolean requiresPseudoTableInSelect() {
		return true;
	}

	@Override
	public String getPseudoTableName() {
		return "dual";
	}
	
	@Override
	public int getMaximumNumberOfItemsForInOperator() {
		return 1000;
	}

	@Override
	public boolean spatialPredicateRequiresNoIndexHint() {
		return databaseAdapter.getConnectionMetaData().getDatabaseMajorVersion() == 11;
	}

	@Override
	public String getHierarchicalGeometryQuery(String schema) {
		return new StringBuilder("select sg.*, LEVEL from ").append(schema).append(".SURFACE_GEOMETRY sg start with sg.ID=? connect by prior sg.ID=sg.PARENT_ID").toString();
	}

	@Override
	public BlobImportAdapter getBlobImportAdapter(Connection connection, BlobType type, String schema) throws SQLException {
		return new BlobImportAdapter(connection, type, schema);
	}

	@Override
	public BlobExportAdapter getBlobExportAdapter(Connection connection, BlobType type) {
		return new BlobExportAdapter(connection, type);
	}

	@Override
	public PredicateToken getBinarySpatialPredicate(SpatialOperatorName operator, Column targetColumn, GeometryObject geometry, boolean negate) {
		PlaceHolder<GeometryObject> geometryLiteral = new PlaceHolder<GeometryObject>(geometry);
		StringLiteral trueLiteral = new StringLiteral("TRUE");

		switch (operator) {
		case BBOX:
		case INTERSECTS:
			return ComparisonFactory.equalTo(new Function("SDO_ANYINTERACT", targetColumn, geometryLiteral), trueLiteral, negate);
		case EQUALS:
			return ComparisonFactory.equalTo(new Function("SDO_EQUALS", targetColumn, geometryLiteral), trueLiteral, negate);
		case DISJOINT:
			return ComparisonFactory.equalTo(new Function("SDO_ANYINTERACT", targetColumn, geometryLiteral), trueLiteral, !negate);
		case TOUCHES:
			return ComparisonFactory.equalTo(new Function("SDO_TOUCH", targetColumn, geometryLiteral), trueLiteral, !negate);
		case WITHIN:
			return ComparisonFactory.equalTo(new Function("SDO_RELATE", targetColumn, geometryLiteral, new StringLiteral("mask=inside+coveredby")), trueLiteral, negate);
		case OVERLAPS:
			return ComparisonFactory.equalTo(new Function("SDO_OVERLAPS", targetColumn, geometryLiteral), trueLiteral, negate);
		case CONTAINS:
			return ComparisonFactory.equalTo(new Function("SDO_RELATE", targetColumn, geometryLiteral, new StringLiteral("mask=contains+covers")), trueLiteral, negate);
		default:
			break;
		}

		return null;
	}

	@Override
	public PredicateToken getDistancePredicate(SpatialOperatorName operator, Column targetColumn, GeometryObject geometry, double distance, boolean negate) {
		PlaceHolder<GeometryObject> placeHolder = new PlaceHolder<GeometryObject>(geometry);
		StringLiteral trueLiteral = new StringLiteral("TRUE");
		StringLiteral distanceLiteral = new StringLiteral("distance = " + distance);
		
		switch (operator) {
		case DWITHIN:
			return ComparisonFactory.equalTo(new Function("SDO_WITHIN_DISTANCE", targetColumn, placeHolder, distanceLiteral), trueLiteral, negate);			
		case BEYOND:
			return ComparisonFactory.equalTo(new Function("SDO_WITHIN_DISTANCE", targetColumn, placeHolder, distanceLiteral), trueLiteral, !negate);
		default:
			break;
		}		
		
		return null;
	}
	
	@Override
	public Function getAggregateExtentFunction(Column envelope) {
		return new Function("sdo_aggr_mbr", 
				new Function("citydb_util.to_2d", envelope, new IntegerLiteral(databaseAdapter.getConnectionMetaData().getReferenceSystem().getSrid())));
	}

}
