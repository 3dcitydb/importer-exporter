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

import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.sorting.SortProperty;
import org.citydb.query.filter.sorting.Sorting;
import org.citydb.sqlbuilder.select.OrderByToken;
import org.citydb.sqlbuilder.select.orderBy.SortOrder;

public class SortingBuilder {

    protected SortingBuilder() {

    }

    protected void buildSorting(Sorting sorting, SchemaPathBuilder builder, SQLQueryContext queryContext) throws QueryBuildException {
        for (SortProperty sortProperty : sorting.getSortProperties()) {
            queryContext = builder.buildSchemaPath(sortProperty.getValueReference().getSchemaPath(), queryContext);

            SortOrder sortOrder = sortProperty.getSortOrder() == org.citydb.query.filter.sorting.SortOrder.DESCENDING ?
                    SortOrder.DESCENDING : SortOrder.ASCENDING;

            queryContext.select.addOrderBy(new OrderByToken(queryContext.targetColumn, sortOrder));
        }
    }
}
