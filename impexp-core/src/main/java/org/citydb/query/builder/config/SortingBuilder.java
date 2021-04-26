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

package org.citydb.query.builder.config;

import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.builder.util.ValueReferenceBuilder;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.ValueReference;
import org.citydb.query.filter.sorting.SortOrder;
import org.citydb.query.filter.sorting.SortProperty;
import org.citydb.query.filter.sorting.Sorting;

public class SortingBuilder {
    private final ValueReferenceBuilder valueReferenceBuilder;

    protected SortingBuilder(ValueReferenceBuilder valueReferenceBuilder) {
        this.valueReferenceBuilder = valueReferenceBuilder;
    }

    protected Sorting buildSorting(org.citydb.config.project.query.filter.sorting.Sorting sortingConfig) throws QueryBuildException {
        if (!sortingConfig.hasSortProperties())
            throw new QueryBuildException("No valid sort properties provided.");

        Sorting sorting = new Sorting();
        try {
            for (org.citydb.config.project.query.filter.sorting.SortProperty sortBy : sortingConfig.getSortProperties()) {
                if (!sortBy.isSetValueReference())
                    throw new QueryBuildException("A sort property requires a value reference.");

                // build the value reference
                ValueReference valueReference = valueReferenceBuilder.buildValueReference(sortBy);
                if (valueReference.getTarget().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
                    throw new QueryBuildException("The value reference of a sorting property must point to a simple thematic attribute.");

                SortProperty sortProperty = new SortProperty(valueReference);
                switch (sortBy.getSortOrder()) {
                    case ASCENDING:
                        sortProperty.setSortOrder(SortOrder.ASCENDING);
                        break;
                    case DESCENDING:
                        sortProperty.setSortOrder(SortOrder.DESCENDING);
                        break;
                }

                sorting.addSortProperty(sortProperty);
            }
        } catch (FilterException e) {
            throw new QueryBuildException("Failed to build the sorting clause.", e);
        }

        return sorting;
    }
}
