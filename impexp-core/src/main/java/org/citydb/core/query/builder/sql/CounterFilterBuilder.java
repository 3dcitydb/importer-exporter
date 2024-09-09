/*
 * 3D City Database - The Open Source CityGML Database
 * https://www.3dcitydb.org/
 *
 * Copyright 2013 - 2024
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

import org.citydb.config.project.database.DatabaseType;
import org.citydb.core.database.adapter.AbstractDatabaseAdapter;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.filter.counter.CounterFilter;
import org.citydb.sqlbuilder.expression.LongLiteral;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.*;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.projection.Function;

import java.util.List;
import java.util.stream.Collectors;

public class CounterFilterBuilder {
    private final SchemaPathBuilder builder;
    private final AbstractDatabaseAdapter databaseAdapter;

    CounterFilterBuilder(SchemaPathBuilder builder, AbstractDatabaseAdapter databaseAdapter) {
        this.builder = builder;
        this.databaseAdapter = databaseAdapter;
    }

    void buildCounterFilter(CounterFilter counterFilter, SQLQueryContext queryContext) throws QueryBuildException {
        if (!counterFilter.isSetCount() && !counterFilter.isSetStartIndex() && !counterFilter.isSetStartId())
            throw new QueryBuildException("Invalid counter filter configuration.");

        if (databaseAdapter.getSQLAdapter().supportsFetchFirstClause())
            limitResultUsingFetch(counterFilter, queryContext);
        else
            limitResultUsingRowNumber(counterFilter, queryContext);

        // add first_rows hint for Oracle
        if (counterFilter.isSetCount() && databaseAdapter.getConnectionDetails().getDatabaseType() == DatabaseType.ORACLE)
            queryContext.getSelect().addOptimizerHint("first_rows(" + counterFilter.getCount() + ")");
    }

    private void limitResultUsingFetch(CounterFilter counterFilter, SQLQueryContext queryContext) throws QueryBuildException {
        Select select = prepareSelect(counterFilter, queryContext);

        if (counterFilter.isSetStartIndex())
            select.withOffset(new OffsetToken(counterFilter.getStartIndex()));

        if (counterFilter.isSetCount())
            select.withFetch(new FetchToken(counterFilter.getCount()));
    }

    private void limitResultUsingRowNumber(CounterFilter counterFilter, SQLQueryContext queryContext) throws QueryBuildException {
        Select select = prepareSelect(counterFilter, queryContext);

        List<ProjectionToken> projection = select.getProjection();
        List<OrderByToken> orderBy = select.getOrderBy();
        select.unsetOrderBy();

        select.addProjection(new Function("row_number() over (" +
                "order by " + orderBy.stream().map(OrderByToken::toString).collect(Collectors.joining(", ")) +
                ")", "rn_limit", false));

        Table table = new Table(select);
        Select outer = new Select().addOrderBy(new OrderByToken(table.getColumn("rn_limit")));

        long startIndex = counterFilter.isSetStartIndex() ? counterFilter.getStartIndex() : 0;
        if (startIndex > 0) {
            outer.addSelection(ComparisonFactory.greaterThan(table.getColumn("rn_limit"),
                    new LongLiteral(counterFilter.getStartIndex())));
        }

        if (counterFilter.isSetCount()) {
            outer.addSelection(ComparisonFactory.lessThanOrEqualTo(table.getColumn("rn_limit"),
                    new LongLiteral(startIndex + counterFilter.getCount())));
        }

        for (ProjectionToken token : projection)
            outer.addProjection(token instanceof Column ? table.getColumn(((Column) token).getName()) : token);

        queryContext.setSelect(outer);
        queryContext.setFromTable(table);
    }

    private Select prepareSelect(CounterFilter counterFilter, SQLQueryContext queryContext) throws QueryBuildException {
        Select select = queryContext.getSelect();
        Table cityObject = builder.joinCityObjectTable(queryContext);

        // add start id filter
        if (counterFilter.isSetStartId()) {
            Column column = cityObject.getColumn(MappingConstants.ID);
            LongLiteral literal = new LongLiteral(counterFilter.getStartId());

            PredicateToken predicate;
            switch (counterFilter.getStartIdComparisonOperator()) {
                case EQUAL_TO:
                    predicate = ComparisonFactory.equalTo(column, literal);
                    break;
                case NOT_EQUAL_TO:
                    predicate = ComparisonFactory.notEqualTo(column, literal);
                    break;
                case LESS_THAN:
                    predicate = ComparisonFactory.lessThan(column, literal);
                    break;
                case LESS_THAN_OR_EQUAL_TO:
                    predicate = ComparisonFactory.lessThanOrEqualTo(column, literal);
                    break;
                case GREATER_THAN_OR_EQUAL_TO:
                    predicate = ComparisonFactory.greaterThanOrEqualTo(column, literal);
                    break;
                default:
                    predicate = ComparisonFactory.greaterThan(column, literal);
            }

            select.addSelection(predicate);
        }

        // make sure we sort by ids
        boolean found = false;
        for (OrderByToken token : select.getOrderBy()) {
            Column column = token.getColumn();
            if (column.getName().equalsIgnoreCase(MappingConstants.ID)
                    && (column.getTable().getName().equalsIgnoreCase(cityObject.getName())
                    || column.getTable().getName().equalsIgnoreCase(queryContext.getFromTable().getName()))) {
                found = true;
                break;
            }
        }

        if (!found)
            select.addOrderBy(new OrderByToken(cityObject.getColumn(MappingConstants.ID)));

        return select;
    }
}
