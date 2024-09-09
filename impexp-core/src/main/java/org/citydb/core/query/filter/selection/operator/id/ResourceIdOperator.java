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
package org.citydb.core.query.filter.selection.operator.id;

import org.citydb.core.query.filter.FilterException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ResourceIdOperator extends AbstractIdOperator {
    private final Set<String> resourceIds;

    public ResourceIdOperator() {
        resourceIds = new HashSet<>();
    }

    public ResourceIdOperator(Collection<String> resourceIds) throws FilterException {
        if (resourceIds == null)
            throw new FilterException("List of resource ids may not be null.");

        this.resourceIds = new HashSet<>(resourceIds);
    }

    public ResourceIdOperator(String... resourceIds) throws FilterException {
        this(Arrays.asList(resourceIds));
    }

    public boolean isEmpty() {
        return resourceIds.isEmpty();
    }

    public void clear() {
        resourceIds.clear();
    }

    public int numberOfResourceIds() {
        return resourceIds.size();
    }

    public boolean addResourceId(String resourceId) {
        return resourceIds.add(resourceId);
    }

    public Set<String> getResourceIds() {
        return resourceIds;
    }

    @Override
    public IdOperationName getOperatorName() {
        return IdOperationName.RESOURCE_ID;
    }

    @Override
    public ResourceIdOperator copy() throws FilterException {
        return new ResourceIdOperator(resourceIds);
    }

}
