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
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.projection.Function;
import org.citygml4j.model.module.citygml.CoreModule;

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
		// TODO: we need some consistency check for the query (possibly query.isValid())?
		SchemaPathBuilder builder = new SchemaPathBuilder(databaseAdapter.getSQLAdapter(), schemaName, buildProperties);

		// feature type filter
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

		// map feature types to object class ids
		Set<Integer> objectclassIds = new FeatureTypeFilterBuilder().buildFeatureTypeFilter(typeFilter, query.getTargetVersion());

		// selection filter
		SQLQueryContext queryContext;
		if (query.isSetSelection()) {
			// TODO: we must check, whether the feature types announced by the feature type filter
			// are all subtypes of the root node of the schema path
			Predicate predicate = query.getSelection().getPredicate();
			PredicateBuilder predicateBuilder = new PredicateBuilder(query, builder, schemaMapping, databaseAdapter, schemaName, buildProperties);
			queryContext = predicateBuilder.buildPredicate(predicate);
		} else {
			FeatureType superType = schemaMapping.getCommonSuperType(typeFilter.getFeatureTypes());			
			SchemaPath schemaPath = new SchemaPath();
			schemaPath.setFirstNode(superType);
			queryContext = builder.buildSchemaPath(schemaPath, null, true, false);
		}

		// lod filter
		if (query.isSetLodFilter()) {
			LodFilter lodFilter = query.getLodFilter();
			if (lodFilter.getFilterMode() != LodFilterMode.OR || !lodFilter.areAllEnabled()) {
				LodFilterBuilder lodFilterBuilder = new LodFilterBuilder(schemaMapping, schemaName);
				lodFilterBuilder.buildLodFilter(query.getLodFilter(), typeFilter, query.getTargetVersion(), queryContext);
			}
		}

		// sorting clause
		if (query.isSetSorting()) {
			SortingBuilder sortingBuilder = new SortingBuilder();
			sortingBuilder.buildSorting(query.getSorting(), builder, queryContext);
		}

		// add projection and object class id filter
		builder.prepareStatement(queryContext, objectclassIds, true);

		// build distinct query if the query involves 1:n or n:m joins
		if (queryContext.buildContext.requiresDistinct())
			buildDistinctQuery(query, queryContext);

		return queryContext.select;
	}

	public SQLQueryContext buildSchemaPath(SchemaPath schemaPath, boolean addProjection, boolean useLeftJoins) throws QueryBuildException {
		SchemaPathBuilder builder = new SchemaPathBuilder(databaseAdapter.getSQLAdapter(), schemaName, buildProperties);

		FeatureTypeFilter typeFilter = new FeatureTypeFilter(false);
		try {
			typeFilter.addFeatureType(schemaPath.getFirstNode().getPathElement());
		} catch (FilterException e) {
			throw new QueryBuildException("Failed to build feature type filter.", e);
		}

		// map feature types to object class ids
		Set<Integer> objectclassIds = new FeatureTypeFilterBuilder().buildFeatureTypeFilter(typeFilter);

		SQLQueryContext queryContext = builder.buildSchemaPath(schemaPath, null, true, useLeftJoins);
		builder.prepareStatement(queryContext, objectclassIds, addProjection);

		if (queryContext.hasPredicates())
			queryContext.predicates.forEach(queryContext.select::addSelection);

		return queryContext;
	}

	public BuildProperties getBuildProperties() {
		return buildProperties;
	}

	private void buildDistinctQuery(Query query, SQLQueryContext queryContext) {
		if (!query.isSetSorting())
			queryContext.select.setDistinct(true);

		else {
			// when sorting is enabled, then adding the sort column as projection token
			// might lead to multiple rows per top-level feature even if distinct is used.
			// so we have to rewrite the query using the row_number() function to guarantee
			// that every top-level feature is only exported once.
			Select inner = queryContext.select;
			List<ProjectionToken> projection = inner.getProjection();
			List<OrderByToken> orderBy = inner.getOrderBy();
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
					"partition by " + queryContext.fromTable.getColumn(MappingConstants.ID) + " " +
					"order by " + orderBy.stream().map(OrderByToken::toString).collect(Collectors.joining(", ")) +
					")", "rn", false));

			CommonTableExpression cte = new CommonTableExpression("cte", inner);
			Table withTable = cte.asTable();

			// create a new select statement that uses the previous select as CTE
			// and remove duplicates by selecting row numbers with value 1
			queryContext.select = new Select()
					.addWith(cte)
					.addSelection(ComparisonFactory.equalTo(withTable.getColumn("rn"), new IntegerLiteral(1)));

			// re-add projection columns to new select
			for (ProjectionToken token : projection)
				queryContext.select.addProjection(token instanceof Column ? withTable.getColumn(((Column) token).getName()) : token);

			// re-add order by tokens to new select
			for (int i = 0; i < orderBy.size(); i++)
				queryContext.select.addOrderBy(new OrderByToken(withTable.getColumn("order" + i), orderBy.get(i).getSortOrder()));
		}
	}

}
