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

package org.citydb.citygml.deleter.util;

import org.citydb.database.adapter.AbstractDatabaseAdapter;
import org.citydb.database.schema.mapping.SchemaMapping;
import org.citydb.query.Query;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.SelectionFilter;
import org.citydb.query.filter.selection.operator.id.AbstractIdOperator;
import org.citydb.query.filter.selection.operator.id.DatabaseIdOperator;
import org.citydb.query.filter.selection.operator.id.ResourceIdOperator;
import org.citydb.query.filter.type.FeatureTypeFilter;
import org.citygml4j.model.module.citygml.CoreModule;

import java.io.IOException;
import java.util.Iterator;

public class QueryIterator implements Iterator<Query> {
    private final DeleteListParser parser;
    private final SchemaMapping schemaMapping;
    private final int maxIdsPerQuery;

    QueryIterator(DeleteListParser parser, SchemaMapping schemaMapping, AbstractDatabaseAdapter databaseAdapter) {
        this.parser = parser;
        this.schemaMapping = schemaMapping;
        maxIdsPerQuery = databaseAdapter.getSQLAdapter().getMaximumNumberOfItemsForInOperator();
    }

    @Override
    public boolean hasNext() {
        try {
            return parser.hasNext();
        } catch (IOException e) {
            throw new DeleteListException("Failed to parse the delete list.", e);
        }
    }

    @Override
    public Query next() {
        if (hasNext()) {
            try {
                AbstractIdOperator operator = parser.getIdType() == DeleteListParser.IdType.DATABASE_ID ?
                        new DatabaseIdOperator() :
                        new ResourceIdOperator();

                int i = 1;
                do {
                    String id = parser.nextId();
                    if (operator instanceof DatabaseIdOperator) {
                        try {
                            ((DatabaseIdOperator) operator).addDatabaseId(Long.parseLong(id));
                        } catch (NumberFormatException e) {
                            throw new DeleteListException("Invalid database id in delete list: '" + id + "' " +
                                    "(line " + parser.getCurrentLineNumber() + ").", e);
                        }
                    } else {
                        ((ResourceIdOperator) operator).addResourceId(id);
                    }
                } while (parser.hasNext() && i++ < maxIdsPerQuery);

                Query query = new Query();
                query.setFeatureTypeFilter(new FeatureTypeFilter(schemaMapping.getFeatureType("_CityObject", CoreModule.v2_0_0.getNamespaceURI())));
                query.setSelection(new SelectionFilter(operator));
                return query;
            } catch (IOException e) {
                throw new DeleteListException("Failed to parse the delete list.", e);
            } catch (FilterException e) {
                throw new DeleteListException("Failed to create query expression from delete list.", e);
            }
        } else {
            throw new DeleteListException("The delete list has no more elements.");
        }
    }
}
