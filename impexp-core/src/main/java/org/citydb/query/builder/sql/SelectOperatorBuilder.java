/*
 * 3D City Database - The Open Source CityGML Database
 * http://www.3dcitydb.org/
 *
 * Copyright 2013 - 2018
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

import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.selection.operator.sql.SelectOperator;
import org.citydb.sqlbuilder.expression.IntegerLiteral;
import org.citydb.sqlbuilder.expression.LiteralList;
import org.citydb.sqlbuilder.schema.Column;
import org.citydb.sqlbuilder.schema.Table;
import org.citydb.sqlbuilder.select.Select;
import org.citydb.sqlbuilder.select.join.JoinFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonFactory;
import org.citydb.sqlbuilder.select.operator.comparison.ComparisonName;
import org.citydb.sqlbuilder.select.operator.logical.LogicalOperationFactory;

import java.util.List;
import java.util.Set;

public class SelectOperatorBuilder {
    private final Set<Integer> objectClassIds;
    private final BuildProperties buildProperties;

    protected SelectOperatorBuilder(Set<Integer> objectclassIds, BuildProperties buildProperties) {
        this.objectClassIds = objectclassIds;
        this.buildProperties = buildProperties;
    }

    protected SQLQueryContext buildSelectOperator(SelectOperator operator, boolean negate) throws QueryBuildException {
        Table cityObject = new Table(MappingConstants.CITYOBJECT);
        Table selectAsTable = new Table("(" + operator.getSelect() + ")");

        Column id = cityObject.getColumn(MappingConstants.ID);
        Column objectClassId = cityObject.getColumn(MappingConstants.OBJECTCLASS_ID);
        Select select = new Select().addProjection(id, objectClassId);

        if (!negate)
            select.addJoin(JoinFactory.inner(selectAsTable, MappingConstants.ID, ComparisonName.EQUAL_TO, id));
        else {
            select.addJoin(JoinFactory.full(selectAsTable, MappingConstants.ID, ComparisonName.EQUAL_TO, id))
                    .addSelection(LogicalOperationFactory.OR(
                            ComparisonFactory.isNull(id),
                            ComparisonFactory.isNull(selectAsTable.getColumn(MappingConstants.ID))
                    ));
        }

        // check whether we shall add additional projection columns
        List<String> projectionColumns = buildProperties.getAdditionalProjectionColumns();
        if (!projectionColumns.isEmpty()) {
            for (String column : projectionColumns)
                select.addProjection(cityObject.getColumn(column));
        }

        // add objectclass_id predicate
        if (objectClassIds != null && !objectClassIds.isEmpty()) {
            if (objectClassIds.size() == 1)
                select.addSelection(ComparisonFactory.equalTo(objectClassId, new IntegerLiteral(objectClassIds.iterator().next())));
            else
                select.addSelection(ComparisonFactory.in(objectClassId, new LiteralList(objectClassIds.toArray(new Integer[0]))));
        }

        SQLQueryContext queryContext = new SQLQueryContext(select);
        queryContext.fromTable = queryContext.toTable = cityObject;

        return queryContext;
    }
}
