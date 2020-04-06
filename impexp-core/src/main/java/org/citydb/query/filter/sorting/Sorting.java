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

import org.citydb.query.filter.FilterException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sorting {
    private List<SortProperty> sortProperties;

    public Sorting() {
        sortProperties = new ArrayList<>();
    }

    public Sorting(List<SortProperty> sortProperties) throws FilterException {
        if (sortProperties == null)
            throw new FilterException("Sort properties may not be null.");

        this.sortProperties = sortProperties;
    }

    public Sorting(SortProperty... sortProperties) throws FilterException {
        this(Arrays.asList(sortProperties));
    }

    public boolean hasSortProperties() {
        return sortProperties != null && !sortProperties.isEmpty();
    }

    public void addSortProperty(SortProperty sortProperty) {
        sortProperties.add(sortProperty);
    }

    public boolean removeSortProperty(SortProperty sortProperty) {
        return sortProperties.remove(sortProperty);
    }

    public List<SortProperty> getSortProperties() {
        return sortProperties;
    }

}
