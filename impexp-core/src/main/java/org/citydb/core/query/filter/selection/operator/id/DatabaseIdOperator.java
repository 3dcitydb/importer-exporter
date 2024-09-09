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
package org.citydb.core.query.filter.selection.operator.id;

import org.citydb.core.query.filter.FilterException;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DatabaseIdOperator extends AbstractIdOperator {
    private final Set<Long> databaseIds;

    public DatabaseIdOperator() {
        databaseIds = new HashSet<>();
    }

    public DatabaseIdOperator(Collection<Long> databaseIds) throws FilterException {
        if (databaseIds == null)
            throw new FilterException("List of database ids may not be null.");

        this.databaseIds = new HashSet<>(databaseIds);
    }

    public DatabaseIdOperator(Long... databaseIds) throws FilterException {
        this(Arrays.asList(databaseIds));
    }

    public boolean isEmpty() {
        return databaseIds.isEmpty();
    }

    public void clear() {
        databaseIds.clear();
    }

    public int numberOfDatabaseIds() {
        return databaseIds.size();
    }

    public boolean addDatabaseId(Long databaseId) {
        return databaseIds.add(databaseId);
    }

    public Set<Long> getDatabaseIds() {
        return databaseIds;
    }

    @Override
    public IdOperationName getOperatorName() {
        return IdOperationName.DATABASE_ID;
    }

    @Override
    public DatabaseIdOperator copy() throws FilterException {
        return new DatabaseIdOperator(databaseIds);
    }

}
