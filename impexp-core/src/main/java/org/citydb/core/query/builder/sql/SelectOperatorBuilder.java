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

import org.citydb.ade.model.module.CityDBADE200Module;
import org.citydb.core.database.schema.mapping.FeatureType;
import org.citydb.core.database.schema.mapping.MappingConstants;
import org.citydb.core.database.schema.mapping.SchemaMapping;
import org.citydb.core.database.schema.path.InvalidSchemaPathException;
import org.citydb.core.database.schema.path.SchemaPath;
import org.citydb.core.query.Query;
import org.citydb.core.query.builder.QueryBuildException;
import org.citydb.core.query.filter.selection.operator.sql.SelectOperator;
import org.citydb.sqlbuilder.expression.LiteralSelectExpression;
import org.citydb.sqlbuilder.select.operator.comparison.InOperator;

public class SelectOperatorBuilder {
    private final Query query;
    private final SchemaPathBuilder schemaPathBuilder;
    private final SchemaMapping schemaMapping;

    protected SelectOperatorBuilder(Query query, SchemaPathBuilder schemaPathBuilder, SchemaMapping schemaMapping) {
        this.query = query;
        this.schemaPathBuilder = schemaPathBuilder;
        this.schemaMapping = schemaMapping;
    }

    protected void buildSelectOperator(SelectOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
        if (!operator.isSetSelect())
            throw new QueryBuildException("No select statement provided for the SQL operator.");

        SchemaPath schemaPath;
        try {
            FeatureType superType = schemaMapping.getCommonSuperType(query.getFeatureTypeFilter().getFeatureTypes());
            schemaPath = new SchemaPath(superType)
                    .appendChild(superType.getProperty(MappingConstants.ID, CityDBADE200Module.v3_0.getNamespaceURI(), true));
        } catch (InvalidSchemaPathException e) {
            throw new QueryBuildException(e.getMessage());
        }

        // build the value reference
        schemaPathBuilder.addSchemaPath(schemaPath, queryContext, useLeftJoins);
        LiteralSelectExpression subQuery = new LiteralSelectExpression(operator.getSelect());
        queryContext.addPredicate(new InOperator(queryContext.getTargetColumn(), subQuery, negate));
    }
}
