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

package org.citydb.query.filter.sorting;

import org.citydb.database.schema.mapping.PathElementType;
import org.citydb.query.builder.QueryBuildException;
import org.citydb.query.filter.FilterException;
import org.citydb.query.filter.selection.expression.ValueReference;

public class SortProperty {
    private final ValueReference valueReference;
    private SortOrder sortOrder;

    public SortProperty(ValueReference valueReference, SortOrder sortOrder) throws FilterException {
        if (valueReference == null)
            throw new FilterException("Value reference may not be null.");

        if (valueReference.getTarget().getElementType() != PathElementType.SIMPLE_ATTRIBUTE)
            throw new FilterException("The value reference of a sorting property must point to a simple thematic attribute.");

        this.valueReference = valueReference;
        this.sortOrder = sortOrder;
    }

    public SortProperty(ValueReference valueReference) throws FilterException {
        this(valueReference, SortOrder.ASCENDING);
    }

    public ValueReference getValueReference() {
        return valueReference;
    }

    public SortOrder getSortOrder() {
        return sortOrder != null ? sortOrder : SortOrder.ASCENDING;
    }

    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }
}
