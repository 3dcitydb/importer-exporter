/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2020
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

import org.citydb.config.project.database.DatabaseType;
import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.counter.CounterFilter;
import org.citydb.sqlbuilder.expression.LongLiteral;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.FetchToken;
import org.citydb.sqlbuilder.select.OffsetToken;
import org.citydb.sqlbuilder.select.OrderByToken;
import org.citydb.sqlbuilder.select.ProjectionToken;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.projection.Function;

import java.util.List;
import java.util.stream.Collectors;

public class CounterFilterBuilder {
    private final SchemaPathBuilder builder;
    private final AbstractDatabaseAdapter databaseAdapter;
    private final boolean useFetchSyntax;

    CounterFilterBuilder(SchemaPathBuilder builder, AbstractDatabaseAdapter databaseAdapter) {
        this.builder = builder;
        this.databaseAdapter = databaseAdapter;
        useFetchSyntax = databaseAdapter.getSQLAdapter().supportsFetchFirstClause();
    }

    void buildCounterFilter(CounterFilter counterFilter, SQLQueryContext queryContext) throws QueryBuildException {
        if (useFetchSyntax)
            limitResultUsingFetch(counterFilter, queryContext);
        else
            limitResultUsingRowNumber(counterFilter, queryContext);
    }

    private void limitResultUsingFetch(CounterFilter counterFilter, SQLQueryContext queryContext) throws QueryBuildException {
        prepareSelect(queryContext)
                .withOffset(new OffsetToken(counterFilter.getLowerLimit() - 1))
                .withFetch(new FetchToken(counterFilter.getUpperLimit() - counterFilter.getLowerLimit() + 1));
    }

    private void limitResultUsingRowNumber(CounterFilter counterFilter, SQLQueryContext queryContext) throws QueryBuildException {
        Select select = prepareSelect(queryContext);

        List<ProjectionToken> projection = select.getProjection();
        List<OrderByToken> orderBy = select.getOrderBy();
        select.unsetOrderBy();

        select.addProjection(new Function("row_number() over (" +
                "order by " + orderBy.stream().map(OrderByToken::toString).collect(Collectors.joining(", ")) +
                ")", "rn_limit", false));

        // add first_rows hint for Oracle
        if (!useFetchSyntax && databaseAdapter.getConnectionDetails().getDatabaseType() == DatabaseType.ORACLE) {
            long firstRows = counterFilter.getUpperLimit() - counterFilter.getLowerLimit() + 1;
            select.addOptimizerHint("first_rows(" + firstRows + ")");
        }

        Table table = new Table(select);
        Select outer = new Select()
                .addSelection(ComparisonFactory.between(table.getColumn("rn_limit"),
                        new LongLiteral(counterFilter.getLowerLimit()),
                        new LongLiteral(counterFilter.getUpperLimit())))
                .addOrderBy(new OrderByToken(table.getColumn("rn_limit")));

        for (ProjectionToken token : projection)
            outer.addProjection(token instanceof Column ? table.getColumn(((Column) token).getName()) : token);

        queryContext.setSelect(outer);
        queryContext.setFromTable(table);
    }

    private Select prepareSelect(SQLQueryContext queryContext) throws QueryBuildException {
        Select select = queryContext.getSelect();
        Table cityObject = builder.joinCityObjectTable(queryContext);
        boolean found = false;

        // check if a sorting by the ID column is already defined
        for (OrderByToken token : select.getOrderBy()) {
            Column column = token.getColumn();
            if (column.getName().equalsIgnoreCase(MappingConstants.ID)
                    && (column.getTable().getName().equalsIgnoreCase(cityObject.getName())
                    || column.getTable().getName().equalsIgnoreCase(queryContext.getFromTable().getName()))) {
                found = true;
                break;
            }
        }

        // if not, then add unique sorting by ID column
        if (!found)
            select.addOrderBy(new OrderByToken(cityObject.getColumn(MappingConstants.ID)));

        return select;
    }
}
