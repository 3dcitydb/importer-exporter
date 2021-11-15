/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2021
 * Chair of Geoinformatics
 * Technical University of Munich, Germany
 * https://www.lrg.tum.de/gis/
 *
 * The 3D City Database is jointly developed with the following
 * cooperation partners:
 *
 * Virtual City Systems, Berlin <https://vc.systems/>
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
package org.citydb.core.database.adapter;

import org.citydb.config.geometry.GeometryObject;
import org.citydb.core.database.version.DatabaseVersion;
import org.citydb.core.query.filter.selection.operator.spatial.SpatialOperatorName;
import org.citydb.sqlbuilder.SQLStatement;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.projection.Function;

import java.sql.Date;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	public abstract String getCreateUnloggedTableAsSelect(String tableName, String select);
	public abstract String getUnloggedIndexProperty();

	public abstract boolean requiresPseudoTableInSelect();
	public abstract String getPseudoTableName();
	public abstract boolean spatialPredicateRequiresNoIndexHint();
	public abstract boolean supportsFetchFirstClause();

	public abstract String getHierarchicalGeometryQuery();
	public abstract String getNextSequenceValue(String sequence);
	public abstract String getCurrentSequenceValue(String sequence);
	public abstract String getNextSequenceValuesQuery(String sequence);
	public abstract int getMaximumNumberOfItemsForInOperator();

	public abstract PredicateToken getBinarySpatialPredicate(SpatialOperatorName operator, Column targetColumn, GeometryObject geometry, boolean negate);	
	public abstract PredicateToken getDistancePredicate(SpatialOperatorName operator, Column targetColumn, GeometryObject geometry, double distance, boolean negate);
	public abstract Function getAggregateExtentFunction(Column envelope);
	
	public abstract BlobImportAdapter getBlobImportAdapter(Connection connection, BlobType type) throws SQLException;
	public abstract BlobExportAdapter getBlobExportAdapter(Connection connection, BlobType type);

	public String resolveDatabaseOperationName(String key) {
		if (databaseOperations == null) {
			try {
				databaseOperations = new Properties();
				databaseOperations.load(getClass().getResourceAsStream("operations.properties"));
			} catch (Exception e) {
				throw new IllegalStateException("Failed to load database operations property file.", e);
			}
		}

		String result = databaseOperations.getProperty(key);

		// check whether we have operations for different database versions
		if (result != null && result.contains(",")) {
			String[] values = result.split(",");
			result = null;

		    if (databaseAdapter.getConnectionMetaData() != null) {
				DatabaseVersion dbVersion = databaseAdapter.getConnectionMetaData().getCityDBVersion();
				Matcher matcher = Pattern.compile("^v(\\d+)(?:\\.(\\d+))?$").matcher("");
				Map<DatabaseVersion, String> operations = new TreeMap<>(Collections.reverseOrder());

                for (String value : values) {
                    String[] items = value.split("=");

                    if (items.length == 2) {
                    	matcher.reset(items[0]);
                    	if (matcher.matches()) {
							String major = matcher.group(1);
							String minor = matcher.group(2);

							DatabaseVersion version = new DatabaseVersion(Integer.parseInt(major), minor != null ? Integer.parseInt(minor) : 0, 0);
							operations.put(version, items[1]);
						} else
							throw new IllegalStateException("Failed to parse versions for database operation key '" + key + "'.");
                    } else
						throw new IllegalStateException("Failed to parse versions for database operation key '" + key + "'.");
                }

                DatabaseVersion upperBound = null;
				for (Map.Entry<DatabaseVersion, String> entry : operations.entrySet()) {
                	DatabaseVersion lowerBound = entry.getKey();

                	if (dbVersion.compareTo(lowerBound) >= 0
						&& (upperBound == null || dbVersion.compareTo(upperBound) < 0)) {
                		result = entry.getValue();
                		break;
					}

                	upperBound = lowerBound;
				}
            }
		}

        if (result == null)
            throw new IllegalArgumentException("No mapping found for database operation key '" + key + "'.");

        // replace tokens
		if (result.contains("${schema}"))
			result = result.replace("${schema}", databaseAdapter.getConnectionDetails().getSchema());

		return result;
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

			if (value instanceof String) {
				preparedStatement.setString(i + 1, (String) value);
			} else if (value instanceof GeometryObject) {
				preparedStatement.setObject(i + 1, databaseAdapter.getGeometryConverter().getDatabaseObject((GeometryObject) value, connection));
			} else if (value instanceof Date) {
				preparedStatement.setDate(i + 1, (Date) value);
			} else if (value instanceof Timestamp) {
				preparedStatement.setTimestamp(i + 1, (Timestamp) value);
			} else if (value instanceof Boolean) {
				preparedStatement.setInt(i + 1, ((Boolean) value) ? 1 : 0);
			} else if (value instanceof Double) {
				preparedStatement.setDouble(i + 1, (Double) value);
			} else if (value instanceof Integer) {
				preparedStatement.setInt(i + 1, (Integer) value);
			} else if (value instanceof Long) {
				preparedStatement.setLong(i + 1, (Long) value);
			}
		}
	}
	
}
