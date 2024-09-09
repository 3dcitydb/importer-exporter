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

package org.citydb.core.operation.importer.filter.selection.id;

import org.citydb.config.project.importer.ImportList;
import org.citydb.config.project.importer.ImportListMode;
import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.query.filter.FilterException;
import org.citygml4j.model.gml.feature.AbstractFeature;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ImportListFilter {
    private final CacheTable importListCacheTable;
    private final ImportListMode mode;

    public ImportListFilter(ImportList importList, CacheTable importListCacheTable) throws FilterException {
        if (importList == null) {
            throw new FilterException("Import list config must not be null.");
        }

        this.importListCacheTable = importListCacheTable;
        mode = importList.getMode();
    }

    public boolean isSatisfiedBy(AbstractFeature feature) throws FilterException {
        if (feature.isSetId()) {
            try (PreparedStatement ps = importListCacheTable.getConnection().prepareStatement("select 1 from " +
                    importListCacheTable.getTableName() + " where gmlid = ?")) {
                ps.setString(1, feature.getId());
                try (ResultSet rs = ps.executeQuery()) {
                    return (mode == ImportListMode.IMPORT) == rs.next();
                }
            } catch (SQLException e) {
                throw new FilterException("Failed to query import list.", e);
            }
        }

        return true;
    }
}
