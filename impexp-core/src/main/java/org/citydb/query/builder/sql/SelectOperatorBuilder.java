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

import org.citydb.ade.model.module.CityDBADE200Module;
import org.citydb.database.schema.mapping.FeatureType;
import org.citydb.database.schema.mapping.MappingConstants;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.database.schema.path.InvalidSchemaPathException;
import org.citydb.database.schema.path.SchemaPath;
import org.citydb.query.Query;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.selection.expression.ValueReference;
import org.citydb.query.filter.selection.operator.sql.SelectOperator;
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

    protected SQLQueryContext buildSelectOperator(SelectOperator operator, SQLQueryContext queryContext, boolean negate, boolean useLeftJoins) throws QueryBuildException {
        if (!operator.isSetSelect())
            throw new QueryBuildException("No select statement provided for the SQL operator.");

        FeatureType superType = schemaMapping.getCommonSuperType(query.getFeatureTypeFilter().getFeatureTypes());
        ValueReference valueReference;

        try {
            SchemaPath path = new SchemaPath(superType);
            path.appendChild(superType.getProperty(MappingConstants.ID, CityDBADE200Module.v3_0.getNamespaceURI(), true));
            valueReference = new ValueReference(path);
        } catch (InvalidSchemaPathException e) {
            throw new QueryBuildException(e.getMessage());
        }

        // build the value reference
        queryContext = schemaPathBuilder.buildSchemaPath(valueReference.getSchemaPath(), queryContext, useLeftJoins);
        LiteralSelectExpression subQuery = new LiteralSelectExpression(operator.getSelect());
        queryContext.addPredicate(new InOperator(queryContext.targetColumn, subQuery, negate));

        return queryContext;
    }
}
