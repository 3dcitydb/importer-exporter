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

package org.citydb.core.query.builder.sql;

import org.citydb.core.database.schema.path.AbstractNode;
import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.filter.sorting.SortProperty;
import org.citydb.core.query.filter.sorting.Sorting;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.OrderByToken;
import org.citydb.sqlbuilder.select.PredicateToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.Join;
import org.citydb.sqlbuilder.select.operator.comparison.BinaryComparisonOperator;
import org.citydb.sqlbuilder.select.operator.logical.BinaryLogicalOperator;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationName;
import org.citydb.sqlbuilder.select.orderBy.SortOrder;

import java.util.HashSet;
import java.util.Set;

public class SortingBuilder {
    private final SchemaPathBuilder builder;

    protected SortingBuilder(SchemaPathBuilder builder) {
        this.builder = builder;
    }

    protected void buildSorting(Sorting sorting, SQLQueryContext queryContext) throws QueryBuildException {
        if (!sorting.hasSortProperties())
            throw new QueryBuildException("No valid sort properties provided.");

        Set<String> valueReferences = new HashSet<>();
        for (SortProperty sortProperty : sorting.getSortProperties()) {
            SchemaPath schemaPath = sortProperty.getValueReference().getSchemaPath();
            if (!valueReferences.add(schemaPath.toXPath()))
                throw new QueryBuildException("Duplicate value references pointing to the same sorting key are not allowed.");

            AbstractNode<?> node = schemaPath.getFirstNode();
            if (node.isSetPredicate())
                throw new QueryBuildException("Predicates on the root feature are not supported for value references of sort properties.");

            builder.addSchemaPath(schemaPath, queryContext, true, true);
            SortOrder sortOrder = sortProperty.getSortOrder() == org.citydb.core.query.filter.sorting.SortOrder.DESCENDING ?
                    SortOrder.DESCENDING : SortOrder.ASCENDING;

            queryContext.getSelect().addOrderBy(new OrderByToken(queryContext.getTargetColumn(), sortOrder));

            if (queryContext.hasPredicates()) {
                for (PredicateToken predicate : queryContext.getPredicates())
                    addJoinConditions(predicate, queryContext.getSelect());

                queryContext.unsetPredicates();
            }
        }
    }

    private void addJoinConditions(PredicateToken predicate, Select select) throws QueryBuildException {
        if (predicate instanceof BinaryLogicalOperator) {
            if (((BinaryLogicalOperator) predicate).getOperationName() == LogicalOperationName.OR)
                throw new QueryBuildException("Logical OR predicates are not supported for value references of sort properties.");

            for (PredicateToken operand : ((BinaryLogicalOperator) predicate).getOperands())
                addJoinConditions(operand, select);

        } else if (predicate instanceof BinaryComparisonOperator) {
            BinaryComparisonOperator operator = (BinaryComparisonOperator) predicate;
            if (!(operator.getLeftOperand() instanceof Column))
                throw new QueryBuildException("Found unexpected predicate operand in value reference of sort property.");

            Table table = ((Column) operator.getLeftOperand()).getTable();

            for (Join join : select.getJoins()) {
                if (join.getToColumn().getTable().equals(table)) {
                    join.addCondition(predicate);
                    return;
                }
            }

            throw new QueryBuildException("Failed to map predicates in value reference to join conditions.");
        } else
            throw new QueryBuildException("Failed to map predicates in value reference to join conditions.");
    }
}
