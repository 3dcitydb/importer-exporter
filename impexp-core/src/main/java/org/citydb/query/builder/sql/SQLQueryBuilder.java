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
package org.citydb.query.builder.sql;

import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.lod.LodFilter;
import org.citydb.query.filter.lod.LodFilterMode;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citydb.sqlbuilder.expression.CommonTableExpression;
import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.OrderByToken;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.Join;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citygml4j.model.module.citygml.CoreModule;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SQLQueryBuilder {
	private final SchemaMapping schemaMapping;
	private final AbstractDatabaseAdapter databaseAdapter;
	private final String schemaName;

	private final BuildProperties buildProperties;

	public SQLQueryBuilder(SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter, BuildProperties buildProperties) {
		this.schemaMapping = schemaMapping;
		this.databaseAdapter = databaseAdapter;
		this.schemaName = databaseAdapter.getConnectionDetails().getSchema();
		this.buildProperties = buildProperties;
	}

	public SQLQueryBuilder(SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter) {
		this(schemaMapping, databaseAdapter, BuildProperties.defaults());
	}

	public Select buildQuery(Query query) throws QueryBuildException {
		return buildQuery(query, null);
	}

	public Select buildQuery(Query query, SQLQueryContext queryContext) throws QueryBuildException {
		// TODO: we need some consistency check for the query (possibly query.isValid())?

		FeatureTypeFilter typeFilter = query.getFeatureTypeFilter();
		if (typeFilter == null) {
			typeFilter = new FeatureTypeFilter();
			query.setFeatureTypeFilter(typeFilter);
		}

		if (typeFilter.isEmpty()) {
			try {
				typeFilter.addFeatureType(schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI()));
			} catch (FilterException e) {
				throw new QueryBuildException("Failed to build feature type filter.", e);
			}
		}

		SchemaPathBuilder builder = new SchemaPathBuilder(databaseAdapter.getSQLAdapter(), schemaName, buildProperties);
		FeatureType featureType = schemaMapping.getCommonSuperType(typeFilter.getFeatureTypes());
		if (queryContext == null) {
			queryContext = builder.createQueryContext(featureType);

			// feature type filter
			FeatureTypeFilterBuilder typeBuilder = new FeatureTypeFilterBuilder(builder);
			typeBuilder.buildFeatureTypeFilter(typeFilter, query.getTargetVersion(), queryContext);
		}

		// selection filter
		if (query.isSetSelection()) {
			Predicate predicate = query.getSelection().getPredicate();
			PredicateBuilder predicateBuilder = new PredicateBuilder(query, builder, schemaMapping, databaseAdapter, schemaName);
			predicateBuilder.buildPredicate(predicate, queryContext);
		}

		// sorting clause
		if (query.isSetSorting()) {
			SortingBuilder sortingBuilder = new SortingBuilder(builder);
			sortingBuilder.buildSorting(query.getSorting(), queryContext);
		}

		// remove unnecessary joins
		optimizeJoins(builder, queryContext);

		// lod filter
		if (query.isSetLodFilter()) {
			LodFilter lodFilter = query.getLodFilter();
			if (lodFilter.getFilterMode() != LodFilterMode.OR || !lodFilter.areAllEnabled()) {
				LodFilterBuilder lodFilterBuilder = new LodFilterBuilder(schemaMapping, schemaName);
				lodFilterBuilder.buildLodFilter(query.getLodFilter(), typeFilter, query.getTargetVersion(), queryContext);
			}
		}

		// add projection
		addProjection(builder, queryContext);

		// add row limit based on counter filter
		if (query.isSetCounterFilter()) {
			CounterFilterBuilder counterBuilder = new CounterFilterBuilder(builder, databaseAdapter);
			counterBuilder.buildCounterFilter(query.getCounterFilter(), queryContext);
		}

		// build distinct query if the query involves 1:n or n:m joins
		if (!buildProperties.isSuppressDistinct() && queryContext.getBuildContext().requiresDistinct())
			buildDistinctQuery(query, queryContext);

		return queryContext.getSelect();
	}

	public SQLQueryContext buildSchemaPath(SchemaPath schemaPath, boolean useLeftJoins, boolean optimizeJoins) throws QueryBuildException {
		SchemaPathBuilder builder = new SchemaPathBuilder(databaseAdapter.getSQLAdapter(), schemaName, buildProperties);
		SQLQueryContext queryContext = builder.createQueryContext(schemaPath.getFirstNode().getPathElement());

		FeatureTypeFilter typeFilter = new FeatureTypeFilter(false);
		try {
			typeFilter.addFeatureType(schemaPath.getFirstNode().getPathElement());
		} catch (FilterException e) {
			throw new QueryBuildException("Failed to build feature type filter.", e);
		}

		// feature type filter
		FeatureTypeFilterBuilder typeBuilder = new FeatureTypeFilterBuilder(builder);
		typeBuilder.buildFeatureTypeFilter(typeFilter, queryContext);

		// build path
		builder.addSchemaPath(schemaPath, queryContext, useLeftJoins);
		if (queryContext.hasPredicates())
			queryContext.applyPredicates();

		// remove unnecessary joins
		if (optimizeJoins)
			optimizeJoins(builder, queryContext);

		return queryContext;
	}

	public BuildProperties getBuildProperties() {
		return buildProperties;
	}

	private void optimizeJoins(SchemaPathBuilder builder, SQLQueryContext queryContext) throws QueryBuildException {
		Select select = queryContext.getSelect();
		Set<Table> from = new HashSet<>();
		boolean removedJoins = false;

		// collect tables participating in the from clause
		select.getSelection().forEach(t -> t.getInvolvedTables(from));
		select.getOrderBy().forEach(t -> t.getInvolvedTables(from));
		select.getHaving().forEach(t -> t.getInvolvedTables(from));

		// add the table of the target column to make sure we do not remove it
		if (queryContext.getTargetColumn() != null)
			from.add(queryContext.getTargetColumn().getTable());

		for (Join join : select.getJoins()) {
			Set<Table> tables = new HashSet<>(from);

			// add the tables referenced in the join conditions of all other joins
			for (Join other : select.getJoins()) {
				if (other != join) {
					for (PredicateToken condition : other.getConditions())
						condition.getInvolvedTables(tables);
				}
			}

			// remove the join if one of its tables is not contained in the collected tables
			if (!tables.contains(join.getFromColumn().getTable()) || !tables.contains(join.getToColumn().getTable())) {
				select.removeJoin(join);
				removedJoins = true;
			}
		}

		if (removedJoins && !select.getInvolvedTables().contains(queryContext.getFromTable())) {
			Table cityObject = builder.joinCityObjectTable(queryContext);
			if (queryContext.getToTable() == queryContext.getFromTable())
				queryContext.setToTable(cityObject);

			queryContext.setFromTable(cityObject);
		}
	}

	private void buildDistinctQuery(Query query, SQLQueryContext queryContext) {
		if (!query.isSetSorting())
			queryContext.getSelect().setDistinct(true);

		else {
			// when sorting is enabled, then adding the sort column as projection token
			// might lead to multiple rows per top-level feature even if distinct is used.
			// so we have to rewrite the query using the row_number() function to guarantee
			// that every top-level feature is only exported once.
			Select inner = queryContext.getSelect();
			List<ProjectionToken> projection = inner.getProjection();
			List<OrderByToken> orderBy = inner.getOrderBy();
			if (!query.isSetCounterFilter())
				inner.unsetOrderBy();

			// add all order by tokens to the projection clause. use aliases for the
			// column names since sorting may be requested for identical columns
			for (int i = 0; i < orderBy.size(); i++) {
				Column column = orderBy.get(i).getColumn();
				inner.addProjection(new Column(column.getTable(), column.getName(), "order" + i));
			}

			// add row_number() function over the sorted set. the row number is later
			// used to remove duplicates for the same top-level feature.
			inner.addProjection(new Function("row_number() over (" +
					"partition by " + queryContext.getFromTable().getColumn(MappingConstants.ID) + " " +
					"order by " + orderBy.stream().map(OrderByToken::toString).collect(Collectors.joining(", ")) +
					")", "rn_distinct", false));

			CommonTableExpression cte = new CommonTableExpression("cte", inner);
			Table withTable = cte.asTable();

			// create a new select statement that uses the previous select as CTE
			// and remove duplicates by selecting row numbers with value 1
			queryContext.setSelect(new Select()
					.addWith(cte)
					.addSelection(ComparisonFactory.equalTo(withTable.getColumn("rn_distinct"), new IntegerLiteral(1))));

			// re-add projection columns to new select
			for (ProjectionToken token : projection)
				queryContext.getSelect().addProjection(token instanceof Column ? withTable.getColumn(((Column) token).getName()) : token);

			// re-add order by tokens to new select
			for (int i = 0; i < orderBy.size(); i++)
				queryContext.getSelect().addOrderBy(new OrderByToken(withTable.getColumn("order" + i), orderBy.get(i).getSortOrder()));

			queryContext.setFromTable(withTable);
		}
	}

	private void addProjection(SchemaPathBuilder builder, SQLQueryContext queryContext) throws QueryBuildException {
		Select select = queryContext.getSelect();
		Table cityObject = builder.joinCityObjectTable(queryContext);

		select.addProjection(cityObject.getColumns(MappingConstants.ID, MappingConstants.OBJECTCLASS_ID));

		// check whether we shall add additional projection columns
		List<String> projectionColumns = buildProperties.getAdditionalProjectionColumns();
		if (!projectionColumns.isEmpty()) {
			for (String column : projectionColumns)
				select.addProjection(cityObject.getColumn(column));
		}
	}
}
