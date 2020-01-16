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
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.operator.comparison.AbstractComparisonOperator;
import org.citydb.query.filter.selection.operator.id.AbstractIdOperator;
import org.citydb.query.filter.selection.operator.id.IdOperationName;
import org.citydb.query.filter.selection.operator.id.ResourceIdOperator;
import org.citydb.query.filter.selection.operator.logical.AbstractLogicalOperator;
import org.citydb.query.filter.selection.operator.logical.BinaryLogicalOperator;
import org.citydb.query.filter.selection.operator.logical.LogicalOperatorName;
import org.citydb.query.filter.selection.operator.logical.NotOperator;
import org.citydb.query.filter.selection.operator.spatial.AbstractSpatialOperator;
import org.citydb.query.filter.selection.operator.sql.SelectOperator;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationFactory;

import java.util.ArrayList;
import java.util.List;

public class PredicateBuilder {
	private final ComparisonOperatorBuilder comparisonBuilder;
	private final SpatialOperatorBuilder spatialBuilder;
	private final IdOperatorBuilder idBuilder;
	private final SelectOperatorBuilder selectBuilder;

	protected PredicateBuilder(Query query, SchemaPathBuilder schemaPathBuilder, SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter, String schemaName) {
		comparisonBuilder = new ComparisonOperatorBuilder(schemaPathBuilder, databaseAdapter.getSQLAdapter(), schemaName);
		spatialBuilder = new SpatialOperatorBuilder(query, schemaPathBuilder, schemaMapping, databaseAdapter, schemaName);
		idBuilder = new IdOperatorBuilder(query, schemaPathBuilder, schemaMapping, databaseAdapter.getSQLAdapter());
		selectBuilder = new SelectOperatorBuilder(query, schemaPathBuilder, schemaMapping);
	}

	protected void buildPredicate(Predicate predicate, SQLQueryContext queryContext) throws QueryBuildException {
		buildPredicate(predicate, queryContext, false, requiresLeftJoins(predicate));
		if (!queryContext.hasPredicates())
			throw new QueryBuildException("Failed to build selection predicates.");

		queryContext.applyPredicates();
	}

	private void buildPredicate(Predicate predicate, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
		switch (predicate.getPredicateName()) {
		case COMPARISON_OPERATOR:
			comparisonBuilder.buildComparisonOperator((AbstractComparisonOperator)predicate, queryContext, negate, useLeftJoins);
			break;
		case SPATIAL_OPERATOR:
			spatialBuilder.buildSpatialOperator((AbstractSpatialOperator)predicate, queryContext, negate, useLeftJoins);
			break;
		case LOGICAL_OPERATOR:
			buildLogicalOperator((AbstractLogicalOperator)predicate, queryContext, negate, useLeftJoins);
			break;
		case ID_OPERATOR:
			buildIdOperator((AbstractIdOperator)predicate, queryContext, negate, useLeftJoins);
			break;
		case SQL_OPERATOR:
			selectBuilder.buildSelectOperator((SelectOperator)predicate, queryContext, negate, useLeftJoins);
			break;
		}
	}

	private void buildIdOperator(AbstractIdOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
		if (operator.getOperatorName() == IdOperationName.RESOURCE_ID)
			idBuilder.buildResourceIdOperator((ResourceIdOperator) operator, queryContext, negate, useLeftJoins);
	}

	private void buildLogicalOperator(AbstractLogicalOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
		if (operator.getOperatorName() == LogicalOperatorName.NOT) {
			NotOperator not = (NotOperator)operator;
			buildPredicate(not.getOperand(), queryContext, !negate, useLeftJoins);
		} else {
			BinaryLogicalOperator binaryOperator = (BinaryLogicalOperator) operator;
			if (binaryOperator.numberOfOperands() == 0)
				throw new QueryBuildException("No operand provided for the binary logical " + binaryOperator.getOperatorName() + " operator.");

			if (binaryOperator.numberOfOperands() == 1) {
				buildPredicate(binaryOperator.getOperands().get(0), queryContext, negate, useLeftJoins);
			} else {
				List<PredicateToken> predicates = new ArrayList<>();
				for (Predicate operand : binaryOperator.getOperands()) {
					buildPredicate(operand, queryContext, negate, useLeftJoins);
					if (!queryContext.hasPredicates())
						throw new QueryBuildException("Failed to build selection predicates.");

					if (binaryOperator.getOperatorName() == LogicalOperatorName.OR && queryContext.getPredicates().size() > 1)
						predicates.add(LogicalOperationFactory.AND(queryContext.getPredicates()));
					else
						predicates.addAll(queryContext.getPredicates());

					queryContext.unsetPredicates();
				}

				queryContext.addPredicate(binaryOperator.getOperatorName() == LogicalOperatorName.AND ?
						LogicalOperationFactory.AND(predicates) :
						LogicalOperationFactory.OR(predicates));
			}
		}
	}

	private boolean requiresLeftJoins(Predicate predicate) {
		if (predicate instanceof BinaryLogicalOperator) {
			BinaryLogicalOperator logicalOperator = (BinaryLogicalOperator) predicate;
			if (logicalOperator.getOperatorName() == LogicalOperatorName.OR)
				return true;

			for (Predicate operand : logicalOperator.getOperands()) {
				if (requiresLeftJoins(operand))
					return true;
			}
		}

		return false;
	}

}
