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
package org.citydb.core.operation.common.cache.model;

import org.citydb.core.database.adapter.AbstractSQLAdapter;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CacheTableTextureAssociationTarget extends AbstractCacheTableModel {
    public static CacheTableTextureAssociationTarget instance = null;

    public synchronized static CacheTableTextureAssociationTarget getInstance() {
        if (instance == null)
            instance = new CacheTableTextureAssociationTarget();

        return instance;
    }

    @Override
    public void createIndexes(Connection conn, String tableName, String properties) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("create index idx_" + tableName + " on " + tableName + " (GMLID) " + properties);
        }
    }

    @Override
    public CacheTableModel getType() {
        return CacheTableModel.TEXTUREASSOCIATION_TARGET;
    }

    @Override
    protected String getColumns(AbstractSQLAdapter sqlAdapter) {
        return "(" +
                "SURFACE_DATA_ID " + sqlAdapter.getBigInt() + ", " +
                "SURFACE_GEOMETRY_ID " + sqlAdapter.getBigInt() + ", " +
                "GMLID " + sqlAdapter.getCharacterVarying(256) +
                ")";
    }
}
