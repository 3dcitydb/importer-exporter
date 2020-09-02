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
package org.citydb.citygml.exporter.database.content;

import org.citydb.citygml.exporter.CityGMLExportException;
import org.citydb.database.schema.mapping.AbstractJoin;
import org.citydb.database.schema.mapping.AbstractObjectType;
import org.citydb.database.schema.mapping.AbstractTypeProperty;
import org.citydb.database.schema.mapping.FeatureProperty;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.Join;
import org.citydb.database.schema.mapping.JoinTable;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.expression.LiteralList;
import org.citydb.sqlbuilder.expression.LiteralSelectExpression;
import org.citydb.sqlbuilder.expression.PlaceHolder;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class AbstractFeatureExporter<T extends AbstractFeature> extends AbstractTypeExporter {
	protected final Connection connection;
	private final Class<T> featureClass;
	private final Map<String, PreparedStatement> statements;
		
	public AbstractFeatureExporter(Class<T> featureClass, Connection connection, CityGMLExportManager exporter) {
		super(exporter);
		this.featureClass = featureClass;
		this.connection = connection;
		statements = new HashMap<>();
	}
	
	protected abstract Collection<T> doExport(long id, T root, FeatureType rootType, PreparedStatement ps) throws CityGMLExportException, SQLException;
	
	protected boolean doExport(T object, long id, FeatureType featureType) throws CityGMLExportException, SQLException {
		return !doExport(id, object, featureType, getOrCreateStatement("id")).isEmpty();
	}
	
	protected Collection<T> doExport(FeatureProperty featureProperty, long parentId) throws CityGMLExportException, SQLException {
		String key = Integer.toHexString(featureProperty.hashCode());
		PreparedStatement ps = statements.get(key);	
		if (ps == null) {
			Select select = createSelect(featureProperty);
			
			// add predicate on objectclass_id if required
			if (featureProperty.getType() != exporter.getAbstractObjectType(featureClass))
				select.addSelection(getFeatureTypeFilter(featureProperty.getType()));
			
			ps = connection.prepareStatement(select.toString());
			statements.put(key, ps);
		}
		
		return doExport(parentId, null, null, ps);
	}

	protected PreparedStatement getOrCreateBulkStatement(int batchSize) throws SQLException {
		PreparedStatement ps = statements.get("id_bulk");
		if (ps == null) {
			String placeHolders = String.join(",", Collections.nCopies(batchSize, "?"));
			Select select = new Select(this.select).addSelection(ComparisonFactory.in(table.getColumn("id"), new LiteralSelectExpression(placeHolders)));
			ps = connection.prepareStatement(select.toString());
			statements.put("id_bulk", ps);
		}

		return ps;
	}

	protected void prepareBulkStatement(PreparedStatement ps, Long[] ids, int batchSize) throws SQLException {
		for (int i = 0; i < batchSize; i++)
			ps.setLong(i + 1, i < ids.length ? ids[i] : 0);
	}
	
	protected PreparedStatement getOrCreateStatement(String columnName) throws SQLException {
		PreparedStatement ps = statements.get(columnName);
		if (ps == null) {
			Select select = new Select(this.select).addSelection(ComparisonFactory.equalTo(table.getColumn(columnName), new PlaceHolder<>()));
			ps = connection.prepareStatement(select.toString());
			statements.put(columnName, ps);
		}
		
		return ps;
	}
	
	protected PreparedStatement getOrCreateStatement(String columnName, Class<? extends AbstractFeature> filterClass) throws SQLException {
		String key = columnName + filterClass.getName();
		PreparedStatement ps = statements.get(key);
		if (ps == null) {
			Select select = new Select(this.select)
					.addSelection(ComparisonFactory.equalTo(table.getColumn(columnName), new PlaceHolder<>()))
					.addSelection(getFeatureTypeFilter(exporter.getFeatureType(filterClass)));	
			
			ps = connection.prepareStatement(select.toString());
			statements.put(key, ps);
		}
		
		return ps;
	}
	
	private Select createSelect(AbstractTypeProperty<?> typeProperty) throws CityGMLExportException {
		if (!typeProperty.isSetJoin())
			throw new CityGMLExportException("The type property '" + exporter.getPropertyName(typeProperty) + "' is expected to join the table '" + table.getName() + "'.");

		Select result = new Select(select);
		AbstractJoin abstractJoin = typeProperty.getJoin();

		if (abstractJoin instanceof Join) {
			Join join = (Join)abstractJoin;
			
			String toTable = join.getTable();
			if (!toTable.equals(MappingConstants.TARGET_TABLE_TOKEN)
					&& !table.getName().equalsIgnoreCase(join.getTable()))
				throw new CityGMLExportException("The type property '" + exporter.getPropertyName(typeProperty) + "' is expected to join the table '" + table.getName() + "'.");

			result.addSelection(ComparisonFactory.equalTo(table.getColumn(join.getToColumn()), new PlaceHolder<>()));
		} 

		else if (abstractJoin instanceof JoinTable) {
			JoinTable joinTable = (JoinTable)abstractJoin;
			Table intermediateTable = new Table(joinTable.getTable(), exporter.getDatabaseAdapter().getConnectionDetails().getSchema());
			Table fromTable = new Table(joinTable.getJoin().getTable(), exporter.getDatabaseAdapter().getConnectionDetails().getSchema());

			String toTable = joinTable.getInverseJoin().getTable();
			if (!toTable.equals(MappingConstants.TARGET_TABLE_TOKEN)
					&& !table.getName().equalsIgnoreCase(toTable))
				throw new CityGMLExportException("The type property '" + exporter.getPropertyName(typeProperty) + "' is expected to join the table '" + table.getName() + "'.");

			result.addJoin(JoinFactory.inner(intermediateTable, joinTable.getInverseJoin().getFromColumn(), ComparisonName.EQUAL_TO, table.getColumn(joinTable.getInverseJoin().getToColumn())))
			.addJoin(JoinFactory.inner(fromTable, joinTable.getJoin().getToColumn(), ComparisonName.EQUAL_TO, intermediateTable.getColumn(joinTable.getJoin().getFromColumn())))
			.addSelection(ComparisonFactory.equalTo(fromTable.getColumn(MappingConstants.ID), new PlaceHolder<>()));
		} 

		else
			throw new CityGMLExportException("The type property '" + exporter.getPropertyName(typeProperty) + "' is expected to join the table '" + table.getName() + "'.");

		return result;
	}
	
	private PredicateToken getFeatureTypeFilter(AbstractObjectType<?> type) {		
		Set<Integer> objectClassIds = new HashSet<>();

		if (!type.isAbstract())
			objectClassIds.add(type.getObjectClassId());
		else {
			for (AbstractObjectType<?> subType : type.listSubTypes(true))
				objectClassIds.add(subType.getObjectClassId());
		}

		if (objectClassIds.size() == 1)
			return ComparisonFactory.equalTo(table.getColumn("objectclass_id"), new IntegerLiteral(objectClassIds.iterator().next()));
		else
			return ComparisonFactory.in(table.getColumn("objectclass_id"), new LiteralList(objectClassIds.toArray(new Integer[0])));
	}

	@Override
	public void close() throws SQLException {
		for (PreparedStatement ps : statements.values())
			ps.close();
	}
	
}
