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
package org.citydb.database.adapter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.query.filter.selection.operator.spatial.SpatialOperatorName;

import vcs.sqlbuilder.SQLStatement;
import vcs.sqlbuilder.expression.PlaceHolder;
import vcs.sqlbuilder.schema.Column;
import vcs.sqlbuilder.select.PredicateToken;
import vcs.sqlbuilder.select.projection.Function;

public abstract class AbstractSQLAdapter {
	protected final AbstractDatabaseAdapter databaseAdapter;
	protected Properties databaseOperations;
	
	protected AbstractSQLAdapter(AbstractDatabaseAdapter databaseAdapter) {
		this.databaseAdapter = databaseAdapter;
	}

	public abstract String getInteger();
	public abstract String getSmallInt();
	public abstract String getBigInt();
	public abstract String getNumeric();
	public abstract String getNumeric(int precision);	
	public abstract String getNumeric(int precision, int scale);
	public abstract String getReal();
	public abstract String getDoublePrecision();
	public abstract String getCharacter(int nrOfChars);
	public abstract String getCharacterVarying(int nrOfChars);
	public abstract String getPolygon2D();
	public abstract String getCreateUnloggedTable(String tableName, String columns);
	public abstract String getCreateUnloggedTableAsSelectFrom(String targetTableName, String sourceTableName);
	public abstract String getUnloggedIndexProperty();

	public abstract boolean requiresPseudoTableInSelect();
	public abstract String getPseudoTableName();
	public abstract boolean spatialPredicateRequiresNoIndexHint();
	public abstract String getHierarchicalGeometryQuery(String schema);
	public abstract String getNextSequenceValue(String sequence, String schema);
	public abstract String getCurrentSequenceValue(String sequence, String schema);
	public abstract String getNextSequenceValuesQuery(String sequence);
	public abstract int getMaximumNumberOfItemsForInOperator();

	public abstract PredicateToken getBinarySpatialPredicate(SpatialOperatorName operator, Column targetColumn, GeometryObject geometry, boolean negate);	
	public abstract PredicateToken getDistancePredicate(SpatialOperatorName operator, Column targetColumn, GeometryObject geometry, double distance, boolean negate);
	public abstract Function getAggregateExtentFunction(Column envelope);
	
	public abstract BlobImportAdapter getBlobImportAdapter(Connection connection, BlobType type, String schema) throws SQLException;
	public abstract BlobExportAdapter getBlobExportAdapter(Connection connection, BlobType type);

	public String resolveDatabaseOperationName(String operation) {
		if (databaseOperations == null) {
			try {
				databaseOperations = new Properties();
				databaseOperations.load(getClass().getResourceAsStream("operations.properties"));
			} catch (IOException e) {
				throw new IllegalStateException("Failed to load operations properties file.", e);
			}
		}

		return databaseOperations.getProperty(operation);
	}
	
	public PreparedStatement prepareStatement(SQLStatement statement, Connection connection) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(statement.toString());
		fillPlaceHolders(statement, preparedStatement, connection);
		
		return preparedStatement;
	}
	
	public void fillPlaceHolders(SQLStatement statement, PreparedStatement preparedStatement, Connection connection) throws SQLException {
		List<PlaceHolder<?>> placeHolders = statement.getInvolvedPlaceHolders();
		
		for (int i = 0; i < placeHolders.size(); i++) {
			Object value = placeHolders.get(i).getValue();

			if (value instanceof String)
				preparedStatement.setString(i + 1, (String)value);
			else if (value instanceof GeometryObject)
				preparedStatement.setObject(i + 1, databaseAdapter.getGeometryConverter().getDatabaseObject((GeometryObject)value, connection));
			else if (value instanceof Date)
				preparedStatement.setDate(i + 1, (Date)value);
			else if (value instanceof Timestamp)
				preparedStatement.setTimestamp(i + 1, (Timestamp)value);
			else if (value instanceof Boolean)
				preparedStatement.setBoolean(i + 1, (Boolean)value);
			else if (value instanceof Double)
				preparedStatement.setDouble(i + 1, (Double)value);
			else if (value instanceof Integer)
				preparedStatement.setInt(i + 1, (Integer)value);
			else if (value instanceof Long)
				preparedStatement.setLong(i + 1, (Long)value);
		}
	}
	
}
