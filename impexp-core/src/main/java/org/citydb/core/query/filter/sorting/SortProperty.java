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

package org.citydb.core.query.filter.sorting;

import org.citydb.core.database.schema.mapping.PathElementType;
import org.citydb.core.query.filter.FilterException;
import org.citydb.core.query.filter.selection.expression.ValueReference;

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
