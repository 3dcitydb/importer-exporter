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

package org.citydb.query.filter.projection;

import org.citydb.database.schema.mapping.AbstractObjectType;

import java.util.concurrent.ConcurrentHashMap;

public class Projection {
    private ConcurrentHashMap<Integer, ProjectionFilter> projectionFilters;

    public Projection() {
        projectionFilters = new ConcurrentHashMap<>();
    }

    public void addProjectionFilter(ProjectionFilter filter) {
        if (filter != null) {
            projectionFilters.put(filter.getObjectType().getObjectClassId(), filter);

            if (filter.getObjectType().isSetExtension()) {
                ProjectionFilter parentFilter = projectionFilters.get(filter.getObjectType().getExtension().getBase().getObjectClassId());
                if (parentFilter != null && filter.getMode() == parentFilter.getMode()) {
                    filter.addProperties(parentFilter.getProperties());
                    filter.addGenericAttributes(parentFilter.getGenericAttributes());
                }
            }

            for (AbstractObjectType<?> child : filter.getObjectType().listSubTypes(false)) {
                ProjectionFilter childFilter = projectionFilters.get(child.getObjectClassId());
                if (childFilter == null) {
                    childFilter = new ProjectionFilter(child, filter.getMode(), filter.getProperties(), filter.getGenericAttributes());
                    projectionFilters.put(child.getObjectClassId(), childFilter);
                } else if (childFilter.getMode() == filter.getMode()) {
                    childFilter.addProperties(filter.getProperties());
                    childFilter.addGenericAttributes(filter.getGenericAttributes());
                }
            }
        }
    }

    public ProjectionFilter getProjectionFilter(AbstractObjectType<?> objectType) {
        return projectionFilters.computeIfAbsent(objectType.getObjectClassId(), v -> new ProjectionFilter(objectType));
    }
}
