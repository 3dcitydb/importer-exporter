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
package org.citydb.core.operation.importer.database.xlink.importer;

import org.citydb.core.operation.common.cache.CacheTable;
import org.citydb.core.operation.common.xlink.DBXlinkTextureParam;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class DBXlinkImporterTextureParam implements DBXlinkImporter {
    private final DBXlinkImporterManager xlinkImporterManager;

    private Connection connection;
    private PreparedStatement psXlink;
    private int batchCounter;

    public DBXlinkImporterTextureParam(CacheTable tempTable, DBXlinkImporterManager xlinkImporterManager) throws SQLException {
        this.xlinkImporterManager = xlinkImporterManager;

        connection = tempTable.getConnection();
        psXlink = connection.prepareStatement("insert into " + tempTable.getTableName() +
                " (ID, GMLID, TYPE, IS_TEXTURE_PARAMETERIZATION, TEXPARAM_GMLID, WORLD_TO_TEXTURE) values " +
                "(?, ?, ?, ?, ?, ?)");
    }

    public boolean insert(DBXlinkTextureParam xlinkEntry) throws SQLException {
        psXlink.setLong(1, xlinkEntry.getId());
        psXlink.setString(2, xlinkEntry.getGmlId());
        psXlink.setInt(3, xlinkEntry.getType().ordinal());
        psXlink.setInt(4, xlinkEntry.isTextureParameterization() ? 1 : 0);

        if (xlinkEntry.getTexParamGmlId() != null && xlinkEntry.getTexParamGmlId().length() != 0)
            psXlink.setString(5, xlinkEntry.getTexParamGmlId());
        else
            psXlink.setNull(5, Types.VARCHAR);

        if (xlinkEntry.getWorldToTexture() != null && xlinkEntry.getWorldToTexture().length() != 0)
            psXlink.setString(6, xlinkEntry.getWorldToTexture());
        else
            psXlink.setNull(6, Types.VARCHAR);

        psXlink.addBatch();
        if (++batchCounter == xlinkImporterManager.getCacheAdapter().getMaxBatchSize())
            executeBatch();

        return true;
    }

    @Override
    public void executeBatch() throws SQLException {
        psXlink.executeBatch();
        batchCounter = 0;
    }

    @Override
    public void close() throws SQLException {
        psXlink.close();
    }

    @Override
    public DBXlinkImporterEnum getDBXlinkImporterType() {
        return DBXlinkImporterEnum.XLINK_TEXTUREPARAM;
    }

}
