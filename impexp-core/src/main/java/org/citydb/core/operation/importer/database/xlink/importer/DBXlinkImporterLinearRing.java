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
import org.citydb.core.operation.common.xlink.DBXlinkLinearRing;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBXlinkImporterLinearRing implements DBXlinkImporter {
    private final DBXlinkImporterManager xlinkImporterManager;
    private PreparedStatement psLinearRing;
    private int batchCounter;

    public DBXlinkImporterLinearRing(CacheTable tempTable, DBXlinkImporterManager xlinkImporterManager) throws SQLException {
        this.xlinkImporterManager = xlinkImporterManager;

        psLinearRing = tempTable.getConnection().prepareStatement(new StringBuilder()
                .append("insert into ").append(tempTable.getTableName())
                .append(" (GMLID, PARENT_ID, RING_NO, REVERSE) values (?, ?, ?, ?)").toString());
    }

    public boolean insert(DBXlinkLinearRing xlinkEntry) throws SQLException {
        psLinearRing.setString(1, xlinkEntry.getGmlId());
        psLinearRing.setLong(2, xlinkEntry.getParentId());
        psLinearRing.setInt(3, xlinkEntry.getRingNo());
        psLinearRing.setInt(4, xlinkEntry.isReverse() ? 1 : 0);

        psLinearRing.addBatch();
        if (++batchCounter == xlinkImporterManager.getCacheAdapter().getMaxBatchSize())
            executeBatch();

        return true;
    }

    @Override
    public void executeBatch() throws SQLException {
        psLinearRing.executeBatch();
        batchCounter = 0;
    }

    @Override
    public void close() throws SQLException {
        psLinearRing.close();
    }

    @Override
    public DBXlinkImporterEnum getDBXlinkImporterType() {
        return DBXlinkImporterEnum.LINEAR_RING;
    }

}
