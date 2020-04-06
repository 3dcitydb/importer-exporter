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
package org.citydb.query.builder.config;

import org.citydb.config.project.query.filter.selection.AbstractPredicate;
import org.citydb.config.project.query.filter.selection.comparison.AbstractComparisonOperator;
import org.citydb.config.project.query.filter.selection.id.ResourceIdOperator;
import org.citydb.config.project.query.filter.selection.logical.AbstractBinaryLogicalOperator;
import org.citydb.config.project.query.filter.selection.logical.AbstractLogicalOperator;
import org.citydb.config.project.query.filter.selection.logical.NotOperator;
import org.citydb.config.project.query.filter.selection.spatial.AbstractSpatialOperator;
import org.citydb.config.project.query.filter.selection.sql.SelectOperator;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.util.ValueReferenceBuilder;
import org.citydb.query.filter.selection.Predicate;
import org.citydb.query.filter.selection.operator.logical.BinaryLogicalOperator;
import org.citydb.query.filter.selection.operator.logical.LogicalOperationFactory;
import org.citydb.query.filter.selection.operator.logical.LogicalOperatorName;

public class PredicateBuilder {
	private final ComparisonOperatorBuilder comparisonBuilder;
	private final SpatialOperatorBuilder spatialBuilder;
	private final IdOperatorBuilder idBuilder;
	private final SelectOperatorBuilder selectBuilder;

	protected PredicateBuilder(ValueReferenceBuilder valueReferenceBuilder, AbstractDatabaseAdapter databaseAdapter) {
		comparisonBuilder = new ComparisonOperatorBuilder(valueReferenceBuilder);
		spatialBuilder = new SpatialOperatorBuilder(valueReferenceBuilder, databaseAdapter);
		idBuilder = new IdOperatorBuilder();
		selectBuilder = new SelectOperatorBuilder();
	}

	protected Predicate buildPredicate(AbstractPredicate predicateConfig) throws QueryBuildException {
		if (predicateConfig == null)
			throw new QueryBuildException("No valid filter predicate provided.");

		Predicate predicate = null;
		
		switch (predicateConfig.getPredicateName()) {
		case COMPARISON_OPERATOR:
			predicate = comparisonBuilder.buildComparisonOperator((AbstractComparisonOperator)predicateConfig);
			break;
		case SPATIAL_OPERATOR:
			predicate = spatialBuilder.buildSpatialOperator((AbstractSpatialOperator)predicateConfig);
			break;
		case LOGICAL_OPERATOR:
			predicate = buildLogicalOperator((AbstractLogicalOperator)predicateConfig);
			break;
		case ID_OPERATOR:
			predicate = idBuilder.buildResourceIdOperator((ResourceIdOperator)predicateConfig);
			break;
		case SQL_OPERATOR:
			predicate = selectBuilder.buildSelectOperator((SelectOperator)predicateConfig);
			break;
		}
		
		return predicate;
	}
	
	private Predicate buildLogicalOperator(AbstractLogicalOperator logicalOperatorConfig) throws QueryBuildException {
		if (logicalOperatorConfig.getOperatorName() == org.citydb.config.project.query.filter.selection.logical.LogicalOperatorName.NOT) {
			NotOperator not = (NotOperator)logicalOperatorConfig;
			return LogicalOperationFactory.NOT(buildPredicate(not.getOperand()));
		}

		else {
			AbstractBinaryLogicalOperator binaryOperatorConfig = (AbstractBinaryLogicalOperator)logicalOperatorConfig;
			if (binaryOperatorConfig.numberOfOperands() == 0)
				throw new QueryBuildException("No operand provided for the binary logical " + binaryOperatorConfig.getOperatorName() + " operator.");

			if (binaryOperatorConfig.numberOfOperands() == 1)
				return buildPredicate(binaryOperatorConfig.getOperands().get(0));
			
			BinaryLogicalOperator logicalOperator;
			switch (logicalOperatorConfig.getOperatorName()) {
			case AND:
				logicalOperator = new BinaryLogicalOperator(LogicalOperatorName.AND);
				break;
			case OR:
				logicalOperator = new BinaryLogicalOperator(LogicalOperatorName.OR);
				break;
			default:
				return null;
			}
			
			for (AbstractPredicate predicateConfig : binaryOperatorConfig.getOperands())
				logicalOperator.addOperand(buildPredicate(predicateConfig));
			
			return logicalOperator;
		}
	}
	
}
